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

package de.schlund.pfixxml;

import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;

import de.schlund.pfixcore.workflow.AuthContext;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixcore.workflow.DirectOutputPageMap;
import de.schlund.pfixcore.workflow.DirectOutputState;
import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixcore.workflow.PageRequestProperties;
import de.schlund.pfixxml.serverutil.ContainerUtil;


/**
 * The <code>DirectOutputServlet</code> is a servlet that hijacks the {@link de.schlund.pfixcore.workflow.Context} of a
 * comapnion ContextXMLServer that runs in the same servlet session.
 * It has no Context of it's own, but rather makes all the work itself, as
 * there is no PageFlow handling involved.
 * Instead of {@link de.schlund.pfixcore.workflow.State}s (as the Context of a ContextXMLServer
 * does) this kind of Servlet uses so called {@link
 * DirectOutputStates}. These can write their output directly to the HttpServletResponse
 * object. So there is also no XML/XSLT transformation involved.
 * You can use this construct whenever you need to make things like
 * images, pdfs available for download and you can not simply write out a static file.
 * Consider e.g. a autogenerated pdf that needs information that is saved in some ContextResource.
 * A DirectOutputState can do this, as it has the possibility to work with the Context of a
 * foreign ContextXMLServer servlet, getting all the information it needs from there, generating the
 * pdf and streaming it directly to the OutputStream of the HttpServletResponse.
 *
 * If the foreign Context is of type AuthContext, the servlet
 * additionally checks, if the Context is authenticated before trying to call any DirectOutputState.
 *
 * The servlet gets the foreign context by using the mandatory property
 * <code>foreigncontextservlet.foreignservletname</code>.
 * The value must be the servlet name of the ContextXMLServer whose Context you want to use.
 *  
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class DirectOutputServlet extends ServletManager {
    private static String                PROP_EXT_CONTEXT = "foreigncontextservlet.foreignservletname";
    private        Category              CAT = Category.getInstance(this.getClass());
    private        String                ext_cname = null;
    private        DirectOutputPageMap   pagemap   = null;
    private        PageRequestProperties preqprops = null;
    
    /**
     * The usual <code>needsSession</code> method. Is set to return
     * true, as any other value wouldn't make sense (You need to get a
     * Context from a running session after all)
     *
     * @return a <code>boolean</code> value
     */
    protected final boolean needsSession() {
        return true;
    }
   
    /**
     * <code>allowSessionCreate</code> returns false. If the session is being created
     * here, it will not have a saved Context anyway, so this makes no sense. 
     *
     * @return a <code>boolean</code> value
     */
    protected final boolean allowSessionCreate() {
        return false;
    }

    /**
     * <code>process</code> first tries to get the requested
     * Context. if the Context is of type AuthContext, it checks the
     * Authorization of the context first.  After that, it asks the
     * {@link de.schlund.pfixcore.workflow.DirectOutputPageMap} for a
     * {@link de.schlund.pfixcore.workflow.DirectOutputState}
     * that matches the current PageRequest (NOTE: this is <b>NOT</b> the
     * pagerequest that is returned from the foreign Context as the
     * current PageRequest!). The accessibility of the
     * DirectOutputState is checked, then the handleRequest(Context,
     * Properties, PfixServletRequest, HttpServletResponse) method of
     * the DirectOutputState is called. NOTE: The properties parameter
     * are the properties matching the current PageRequest. Again this
     * is <b>NOT</b> what the foreign context would return!
     *
     * @param preq a <code>PfixServletRequest</code> value
     * @param res a <code>HttpServletResponse</code> value
     * @exception Exception if an error occurs
     */
    protected void process(PfixServletRequest preq, HttpServletResponse res) throws Exception {
         HttpSession   session = preq.getSession(false);
         ContainerUtil conutil = getContainerUtil();
         String        name    = ext_cname + ContextXMLServer.CONTEXT_SUFFIX;
         Context       context = (Context) conutil.getSessionValue(session, name);
         ContextResourceManager crm = context.getContextResourceManager();
         
         if (context == null) {
             throw new RuntimeException("*** didn't find Context " + name + " in Session, maybe it's not yet initialized??? ***");
         }
         
         if (context instanceof AuthContext) {
             // check the authentification first....
             SPDocument spdoc = ((AuthContext) context).checkAuthorization(preq);
             if (spdoc != null) return;
         }
 
         PageRequest       page  = new PageRequest(preq);
         DirectOutputState state = pagemap.getDirectOutputState(page);
         Properties        props = preqprops.getPropertiesForPageRequest(page);
         if (state != null) {
             boolean allowed = state.isAccessible(crm, props, preq);
             if (allowed) {
                 state.handleRequest(crm, props, preq, res);
             } else {
                 throw new RuntimeException("*** Called DirectOutputState " +
                                            state.getClass().getName() + " for page " + page.getName() +
                                            " without being accessible ***");  
             }
         } else {
             throw new RuntimeException("*** No DirectOutputState for page " + page.getName() + " ***");  
         }
    }
    
    protected boolean tryReloadProperties(PfixServletRequest preq) throws ServletException {
        if (super.tryReloadProperties(preq)) {
            initValues();
            return true;
        } else {
            return false;
        }
    }

    private void initValues() throws ServletException {
        String cname = getProperties().getProperty(PROP_EXT_CONTEXT);
        if (cname != null && !cname.equals("")) {
            ext_cname = cname;
        } else {
            throw new ServletException ("*** Need property " + PROP_EXT_CONTEXT + " *****");
        }

        try {
            pagemap   = (DirectOutputPageMap) PropertyObjectManager.getInstance().
                getPropertyObject(getProperties(), "de.schlund.pfixcore.workflow.DirectOutputPageMap");
            preqprops = (PageRequestProperties) PropertyObjectManager.getInstance().
                getPropertyObject(getProperties(), "de.schlund.pfixcore.workflow.PageRequestProperties");
        } catch (Exception e) {
            CAT.warn("==================> XPTN " + e.getMessage());
            throw new ServletException(e.getMessage());
        }
    }
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        initValues();
    }

}
