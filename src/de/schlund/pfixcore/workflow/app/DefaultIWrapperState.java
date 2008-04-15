/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixcore.workflow.app;

import de.schlund.pfixcore.util.TokenManager;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.StateImpl;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PropertyObjectManager;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.config.PageRequestConfig;
import de.schlund.pfixxml.perflogging.PerfEvent;
import de.schlund.pfixxml.perflogging.PerfEventType;
import de.schlund.util.statuscodes.StatusCodeLib;

/**
 * DefaultIWrapperState.java
 * 
 * 
 * Created: Wed Oct 10 10:31:49 2001
 * 
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 */

public class DefaultIWrapperState extends StateImpl {

    public  final static String DEF_WRP_CONTAINER = "de.schlund.pfixcore.workflow.app.IWrapperSimpleContainer";
    private final static String DEF_FINALIZER     = "de.schlund.pfixcore.workflow.app.ResdocSimpleFinalizer";
    public  final static String PROP_CONTAINER    = "defaultiwrapperstate.iwrappercontainer";
    private final static String IHDL_CONT_MANAGER = "de.schlund.pfixcore.workflow.app.IHandlerContainerManager";

    /**
     * @see de.schlund.pfixcore.workflow.State#isAccessible(Context,
     *      PfixServletRequest)
     */
    @Override
    public boolean isAccessible(Context context, PfixServletRequest preq) throws Exception {
        IHandlerContainer container = getIHandlerContainer(context);
        return (container.isPageAccessible(context) && container.areHandlerActive(context));
    }

    /**
     * @see de.schlund.pfixcore.workflow.State#needsData(Context,
     *      PfixServletRequest)
     */
    @Override
    public boolean needsData(Context context, PfixServletRequest preq) throws Exception {
        CAT.debug(">>> [" + context.getCurrentPageRequest().getName() + "] Checking needsData()...");
        // IWrapperContainer container = getIWrapperContainer(context);
        // container.initIWrappers(context, preq, new ResultDocument());
        // boolean retval = container.needsData();
        IHandlerContainer container = getIHandlerContainer(context);
        boolean retval = container.needsData(context);

        if (retval) {
            CAT.debug("    TRUE! now going to retrieve the current status.");
        } else {
            CAT.debug("    FALSE! continue with pageflow check.");
        }
        return retval;
    }

    /**
     * @see de.schlund.pfixcore.workflow.State#getDocument(Context,
     *      PfixServletRequest)
     */
    @Override
    @SuppressWarnings("deprecation")
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {
        IWrapperContainer container = getIWrapperContainer(context);
        ResdocFinalizer rfinal = getResdocFinalizer(context);
        ResultDocument resdoc = new ResultDocument();

        CAT.debug("[[[[[ " + context.getCurrentPageRequest().getName() + " ]]]]]");

        PerfEvent pe = new PerfEvent(PerfEventType.PAGE_INITIWRAPPERS, context.getCurrentPageRequest().toString());

        pe.start();
        container.initIWrappers(context, preq, resdoc);
        pe.save();

        if (isSubmitTrigger(context, preq)) {
            CAT.debug(">>> In SubmitHandling...");

            boolean valid = true;
            RequestParam rp = preq.getRequestParam("__token");
            if (rp != null) {
                String token = rp.getValue();
                String[] tokenParts = token.split(":");
                if (tokenParts.length == 3) {
                    String tokenName = tokenParts[0];
                    String errorPage = tokenParts[1];
                    String tokenValue = tokenParts[2];
                    TokenManager tm = (TokenManager) context;
                    if (tm.isValidToken(tokenName, tokenValue)) {
                        tm.invalidateToken(tokenName);
                    } else {
                        context.addPageMessage(StatusCodeLib.PFIXCORE_GENERATOR_FORM_TOKEN_INVALID);
                        if (errorPage.equals("")) {
                            pe = new PerfEvent(PerfEventType.PAGE_RETRIEVECURRENTSTATUS, context.getCurrentPageRequest().toString());
                            pe.start();
                            container.retrieveCurrentStatus();
                            pe.save();
                            rfinal.onRetrieveStatus(container);
                            context.prohibitContinue();
                        } else {
                            context.setJumpToPage(errorPage);
                        }
                        valid = false;
                    }
                } else {
                    throw new IllegalArgumentException("Invalid token format: " + token);
                }
            } else {
                PageRequestConfig pageConf = context.getConfigForCurrentPageRequest();
                if (pageConf != null && pageConf.requiresToken()) {
                    context.addPageMessage(StatusCodeLib.PFIXCORE_GENERATOR_FORM_TOKEN_MISSING);
                    pe = new PerfEvent(PerfEventType.PAGE_RETRIEVECURRENTSTATUS, context.getCurrentPageRequest().toString());
                    pe.start();
                    container.retrieveCurrentStatus();
                    pe.save();
                    rfinal.onRetrieveStatus(container);
                    context.prohibitContinue();
                    valid = false;
                }
            }

            if (valid) {
                pe = new PerfEvent(PerfEventType.PAGE_HANDLESUBMITTEDDATA, context.getCurrentPageRequest().toString());
                pe.start();
                container.handleSubmittedData();
                pe.save();

                if (container.errorHappened()) {
                    CAT.debug("    => Can't continue, as errors happened during load/work.");
                    rfinal.onWorkError(container);
                    context.prohibitContinue();
                } else {
                    CAT.debug("    => No error happened during work ...");
                    if (container.getClass().getName().equals(DEF_WRP_CONTAINER)) {
                        // TODO: REMOVE THIS whole "if" branch we're in completely once we don't have to support the old behavior anymore
                        if (!context.isJumpToPageSet() && ((IWrapperSimpleContainer) container).stayAfterSubmit()) {
                            CAT.debug("... Container says he wants to stay on this page and no jumptopage is set: Setting prohibitcontinue=true");
                            CAT.warn("CONTAINERWOULDSTOP:" + preq.getServerName() + "|" + context.getCurrentPageRequest().getName());
                            context.prohibitContinue();
                        } else {
                            CAT.debug("... Container says he is ready.");
                        }
                    }

                    CAT.debug("    => end of submit reached successfully.");
                    CAT.debug("    => retrieving current status.");
                    pe = new PerfEvent(PerfEventType.PAGE_RETRIEVECURRENTSTATUS, context.getCurrentPageRequest().toString());
                    pe.start();
                    container.retrieveCurrentStatus();
                    pe.save();

                    rfinal.onSuccess(container);
                }
            }
        } else if (isDirectTrigger(context, preq) || context.finalPageIsRunning() || context.flowIsRunning()) {
            CAT.debug(">>> Retrieving current status...");

            pe = new PerfEvent(PerfEventType.PAGE_RETRIEVECURRENTSTATUS, context.getCurrentPageRequest().toString());
            pe.start();
            container.retrieveCurrentStatus();
            pe.save();
            if (CAT.isDebugEnabled()) {
                if (isDirectTrigger(context, preq)) {
                    CAT.debug("    => REASON: DirectTrigger");
                } else if (context.finalPageIsRunning()) {
                    CAT.debug("    => REASON: FinalPage");
                } else {
                    CAT.debug("    => REASON: WorkFlow");
                }
            }
            rfinal.onRetrieveStatus(container);
            context.prohibitContinue();
        } else {
            throw new XMLException("This should not happen: No submit trigger, no direct trigger, no final page and no workflow???");
        }

        // We want to optimize away the case where the context tells us that we
        // don't need to supply a full document as the context will - because of
        // the current state of
        // the context itself - not use the returned document for displaying the
        // page or any
        // further processing anyway. The context is responsible to only return
        // false when it can be 100% sure that the
        // document is not needed. Most notably this is NOT the case whenever
        // the current flow step has
        // pageflow actions attached, because those can possibly call
        // prohibitContinue() which in turn would force
        // the context to display the current page.
        // See the implementation of Context.stateMustSupplyFullDocument() for
        // details.
        if (context.stateMustSupplyFullDocument()) {
            container.addStringValues();
            container.addErrorCodes();

            container.addIWrapperStatus();
            renderContextResources(context, resdoc);
            addResponseHeadersAndType(context, resdoc);
        }
        return resdoc;
    }

    // Eeek, unfortunately we can't use a flyweight here... (somewhere we need
    // to store state after all)
    protected IWrapperContainer getIWrapperContainer(Context context) throws XMLException {
        IWrapperContainer obj = null;

        String classname = context.getContextConfig().getProperties().getProperty(PROP_CONTAINER);
        
        if (classname == null) {
            classname = DEF_WRP_CONTAINER;
        }

        try {
            obj = (IWrapperContainer) Class.forName(classname).newInstance();
        } catch (InstantiationException e) {
            throw new XMLException("unable to instantiate class [" + classname + "] :" + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new XMLException("unable access class [" + classname + "] :" + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new XMLException("unable to find class [" + classname + "] :" + e.getMessage());
        } catch (ClassCastException e) {
            throw new XMLException("class [" + classname + "] does not implement the interface IWrapperContainer :" + e.getMessage());
        }

        return obj;
    }

    // Remember, a ResdocFinalizer is a flyweight!!!
    protected ResdocFinalizer getResdocFinalizer(Context context) throws XMLException {
        PageRequestConfig config = context.getConfigForCurrentPageRequest();
        Class<? extends ResdocFinalizer> clazz = config.getFinalizer();
        String classname = DEF_FINALIZER;
        if (clazz != null) {
            classname = clazz.getName();
        }

        ResdocFinalizer fin = ResdocFinalizerFactory.getInstance().getResdocFinalizer(classname);

        if (fin == null) {
            throw new RuntimeException("No finalizer found: classname = " + classname);
        }

        return fin;
    }

    // Remember, a IHandlerContainer is a flyweight!!!
    protected IHandlerContainer getIHandlerContainer(Context context) throws Exception {
        // Use context config object as dummy configuration object to make sure
        // each context (server) has its own IHandlerContainerManager
        IHandlerContainerManager ihcm = (IHandlerContainerManager) PropertyObjectManager.getInstance().getConfigurableObject(context.getContextConfig(), IHDL_CONT_MANAGER);
        return ihcm.getIHandlerContainer(context);
    }

}// DefaultIWrapperState
