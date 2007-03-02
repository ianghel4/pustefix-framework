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


import java.text.MessageFormat;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.resources.DocrootResource;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.TargetGeneratorFactory;
import de.schlund.pfixxml.targets.VirtualTarget;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XsltContext;

/**
 * IncludeDocumentExtension.java
 * 
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher </a>
 * @author <a href="mailto:haecker@schlund.de">Joerg Haecker </a>
 * 
 * This class is responsible to return the requested parts of an
 * {@link IncludeDocument}. It provides a static method which is called from
 * XSL via the extension mechanism (and from nowhere else!).
 */
public final class IncludeDocumentExtension {
    //~ Instance/static variables
    // ..................................................................
    private static Logger       LOG        = Logger.getLogger(IncludeDocumentExtension.class);
    private static final String NOTARGET   = "__NONE__";
    private static final String XPPARTNAME = "/include_parts/part[@name='";
    private static final String XTHEMENAME = "/theme[@name = '";
    private static final String XPNAMEEND  = "']";
    
    //~ Methods
    // ....................................................................................
    /**
     * Get the requested IncludeDocument from {@link IncludeDocumentFactory}
     * and retrieve desired information from it.</br> Note: The nested
     * document in the Includedocument is immutable, any attempts to modify it
     * will cause an exception.
     * 
     * @param path    the path to the Includedocument in the file system relative
     *                to docroot.
     * @param part    the part in the Includedocument.
     * @param docroot the document root in the file system
     * @param targetgen
     * @param targetkey
     * @param parent_path
     * @param parent_part
     * @param parent_theme
     * @return a list of nodes understood by the current transformer(currently saxon)
     * @throws Exception on all errors
     */
    public static final Object get(XsltContext context, String path_str, String part,
                                   String targetgen, String targetkey,
                                   String parent_part_in, String parent_theme_in, String computed_inc) throws Exception {

        String       parent_path_str = "";
        String       parent_part     = "";
        String       parent_theme    = "";
        FileResource tgen_path       = ResourceUtil.getFileResource(targetgen);
        
        if (computed_inc.equals("false") && isIncludeDocument(context)) {
            parent_path_str = makeSystemIdRelative(context);
            parent_part     = parent_part_in;
            parent_theme  = parent_theme_in;
        }

        if (path_str == null || path_str.equals("")) {
            throw new XMLException("Need href attribute for pfx:include or path of parent part must be deducible");
        }

        // EEEEK! this code is in need of some serious beautifying....
        
        try {
            DocrootResource    path        = ResourceUtil.getFileResourceFromDocroot(path_str);
            DocrootResource    parent_path = "".equals(parent_path_str) ? null : ResourceUtil.getFileResourceFromDocroot(parent_path_str);
            boolean            dolog       = !targetkey.equals(NOTARGET);
            int                length      = 0;
            IncludeDocument    iDoc        = null;
            Document           doc;

            TargetGenerator tgen = TargetGeneratorFactory.getInstance().createGenerator(tgen_path);
            VirtualTarget target = (VirtualTarget) tgen.getTarget(targetkey);

            String[] themes = tgen.getGlobalThemes().getThemesArr();
            if (!targetkey.equals(NOTARGET)) {
                themes = target.getThemes().getThemesArr();
            }
            if (themes == null) {
                XMLException ex = new XMLException("Target has a 'null' themes array!");
                target.setStoredException(ex);
                throw ex;
            }
            if (themes.length < 1) {
                XMLException ex = new XMLException("Target has an empty themes array!");
                target.setStoredException(ex);
                throw ex;
            }
            
            String DEF_THEME = themes[(themes.length -1)];

            if (!path.exists()) {
                if (dolog) {
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                               parent_path, parent_part, parent_theme, target);
                }
                return errorNode(context,DEF_THEME);
                //return new EmptyNodeSet();
            }
            // get the includedocument
            try {
                iDoc = IncludeDocumentFactory.getInstance().getIncludeDocument(context.getXsltVersion(), path, false);
            } catch (SAXException saxex) {
                if (dolog)
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                               parent_path, parent_part, parent_theme, target);
                target.setStoredException(saxex);
                throw saxex;
            }
            doc = iDoc.getDocument();
            // Get the part
            List ns;
            try {
                ns = XPath.select(doc, XPPARTNAME + part + XPNAMEEND);
            } catch (TransformerException e) {
                if (dolog)
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                               parent_path, parent_part, parent_theme, target);
                throw e;
            }
            length = ns.size();
            if (length == 0) {
                // part not found
                LOG.debug("*** Part '" + part + "' is 0 times defined.");
                if (dolog) {
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                               parent_path, parent_part, parent_theme, target);
                }
                return errorNode(context,DEF_THEME);
                //return new EmptyNodeSet();
            } else if (length > 1) {
                // too many parts. Error!
                if (dolog) {
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                               parent_path, parent_part, parent_theme, target);
                }
                XMLException ex = new XMLException("*** Part '" + part + "' is multiple times defined! Must be exactly 1");
                target.setStoredException(ex);
                throw ex;
            }

            // OK, we have found the part. Find the specfic theme branch matching the theme fallback list.
            LOG.debug("   => Found part '" +  part + "'");
            
            for (int i = 0; i < themes.length; i++) {

                String curr_theme = themes[i]; 
                LOG.debug("     => Trying to find theme branch for theme '" + curr_theme + "'");
                
                try {
                    ns = XPath.select(doc, XPPARTNAME + part + XPNAMEEND + XTHEMENAME + curr_theme + XPNAMEEND);
                } catch (TransformerException e) {
                    if (dolog)
                        DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                                   parent_path, parent_part, parent_theme, target);
                    throw e;
                }
                length = ns.size();
                if (length == 0) {
                    // Didn't find a theme part matching curr_theme, trying next in fallback line
                    if (i < (themes.length - 1)) {
                        LOG.debug("        Part '" + part + "' has no theme branch matching '" + curr_theme + "', trying next theme");
                    } else {
                        LOG.warn("        Part '" + part + "' has no theme branch matching '" + curr_theme + "', no more theme to try!");
                    }
                    continue;
                } else if (length == 1) {
                    LOG.debug("        Found theme branch '" + curr_theme + "' => STOP");
                    // specific theme found
                    boolean ok = true;
                    if (dolog) {
                        try {
                            DependencyTracker.logTyped("text", path, part, curr_theme,
                                                       parent_path, parent_part, parent_theme, target);
                        } catch (Exception e) {
                            // TODO
                            ok = false;
                        }
                    }
                    return ok? (Object) ns.get(0) : errorNode(context,curr_theme);
                    //return ok? (Object) ns.get(0) : new EmptyNodeSet();
                } else {
                    // too many specific themes found. Error!
                    if (dolog) {
                        DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                                   parent_path, parent_part, parent_theme, target);
                    }
                    XMLException ex = new XMLException("*** Theme branch '" + curr_theme +
                                                       "' is defined multiple times under part '" + part + "@" + path + "'");
                    target.setStoredException(ex);
                    throw ex;
                }
            }
            
            // We are only here if none of the themes produced a match:
            boolean ok = true;
            if (dolog) {
                try {
                    DependencyTracker.logTyped("text", path, part, DEF_THEME,
                                               parent_path, parent_part, parent_theme, target);
                } catch (Exception e) { // TODO
                    ok = false;
                }
            }
            return errorNode(context,DEF_THEME);
            //return new EmptyNodeSet();
            
        } catch (Exception e) {
            Object[] args = {path_str, part, targetgen, targetkey, 
                             parent_path_str, parent_part, parent_theme};
            String sb = MessageFormat.format("path={0}|part={1}|targetgen={2}|targetkey={3}|"+
                                             "parent_path={4}|parent_part={5}|parent_theme={6}", args);
            LOG.error("Caught exception in extension function! Params:\n"+ sb+"\n Stacktrace follows.");
            throw e;
        }
    }

    private static final Node errorNode(XsltContext context,String prodname) {
        Document retdoc  = Xml.createDocumentBuilder().newDocument();
        Element  retelem = retdoc.createElement("missing");
        retelem.setAttribute("name", prodname);
        retdoc.appendChild(retelem);
        retdoc = Xml.parse(context.getXsltVersion(),retdoc);
        return retdoc.getDocumentElement();  
    }
    
    
    public static final String makeSystemIdRelative(XsltContext context) {
        return makeSystemIdRelative(context, "dummy");
    }

    public static final String makeSystemIdRelative(XsltContext context, String dummy) {
        String   sysid   = context.getSystemId();
       
        if (sysid.startsWith("pfixroot://")) {
            sysid = sysid.substring(("pfixroot://").length());
        }
        String docroot_url = GlobalConfig.getDocrootAsURL().toString() + "/";
        if (sysid.startsWith(docroot_url)) {
            sysid = sysid.substring(docroot_url.length());
        }
        if (sysid.startsWith("/")) {
            sysid = sysid.substring(1);
        }
        return sysid;
    }

    public static boolean isIncludeDocument(XsltContext context) {
        return context.getDocumentElementName().equals("include_parts");
    }
}// end of class IncludeDocumentExtension
