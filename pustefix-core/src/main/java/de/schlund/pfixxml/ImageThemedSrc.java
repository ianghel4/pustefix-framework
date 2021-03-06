/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixxml;
import java.net.URI;
import java.net.URLEncoder;

import javax.xml.transform.URIResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.schlund.pfixxml.resources.I18NIterator;
import de.schlund.pfixxml.resources.I18NResourceUtil;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.Target;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.VirtualTarget;
import de.schlund.pfixxml.util.XsltContext;
import de.schlund.pfixxml.util.Xslt.ResourceResolver;
    
/**
 * Describe class ImageThemedSrc here.
 *
 *
 * Created: Wed Mar 23 17:15:43 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class ImageThemedSrc {
    
    private final static Logger LOG = LoggerFactory.getLogger(ImageThemedSrc.class);

    public static String getSrc(XsltContext context, String src, String themed_path, String themed_img,
            String parent_part_in, String parent_product_in,
            TargetGenerator targetGen, String targetKey, String module, String search,
            String tenant, String language) throws Exception {
    	return getSrc(context, src, themed_path, themed_img, parent_part_in, parent_product_in, targetGen, targetKey, module, search, tenant, language, false);
    }
    
    /** xslt extension */
    public static String getSrc(XsltContext context, String src, String themed_path, String themed_img,
                                String parent_part_in, String parent_product_in,
                                TargetGenerator targetGen, String targetKey, String module, String search,
                                String tenant, String language, boolean i18n) throws Exception {
        
        String filter = FilterHelper.getFilter(tenant, language);
        
        boolean dynamic = false;
        if(search != null && !search.trim().equals("")) {
            if(search.equals("dynamic")) dynamic = true;
            else throw new XMLException("Unsupported include search argument: " + search);
        }
        
        if(module != null) {
            module = module.trim();
            if(module.equals("")) module = null;
            if(module == null) {
                if(!(src.startsWith("modules/") || src.startsWith("/modules/"))) {
                    String parentSystemId = context.getSystemId();
                    URI parentURI = new URI(parentSystemId);
                    if("module".equals(parentURI.getScheme())) module = parentURI.getAuthority();
                }
            } else if(module.equalsIgnoreCase("WEBAPP")) {
                module = null;
            }
        }
        
        boolean dolog = !targetKey.equals("__NONE__");
        String[] themes = null;
          
        VirtualTarget target = null;
        if (!targetKey.equals("__NONE__")) {
            target = (VirtualTarget) targetGen.getTarget(targetKey);
            themes               = target.getThemes().getThemesArr();
        } else {
            Target parentTarget = getParentTarget(context);
            if(parentTarget != null && parentTarget.getThemes() != null && !parentTarget.getThemes().isEmpty()) {
                themes = parentTarget.getThemes().getThemesArr();
            }
        }
        if (themes == null) {
            themes = targetGen.getGlobalThemes().getThemesArr();
        }
        
        if (isSimpleSrc(src, themed_path, themed_img)) {
            if (src.startsWith("/")) {
                src = src.substring(1);
            }
            Resource res = null;
            LOG.debug("  -> Register image src '" + src + "'");
            if(dynamic) {
                String uri =  "dynamic:/"+src;
                if(module != null) {
                    uri += "?module="+module;
                    if(filter!=null) uri += "&filter=" + URLEncoder.encode(filter, "UTF-8");
                }
                if(!i18n) {
                	res = ResourceUtil.getResource(uri);
	                URI resUri = res.toURI();
	                if("module".equals(resUri.getScheme())) {
	                    src = "modules/"+resUri.getAuthority()+"/"+src;
	                } else {
	                    src = resUri.getPath();
	                    if(src.startsWith("/")) src=src.substring(1);
	                }
                } else {
                	I18NIterator it = new I18NIterator(tenant, language, uri);
                	while(it.hasNext()) {
                		String i18nUri = it.next();
                		Resource i18nRes = ResourceUtil.getResource(i18nUri);
                		if(res == null && (i18nRes.exists() || !it.hasNext())) {
                			res = i18nRes;
                			src = I18NResourceUtil.getURLPath(i18nRes.toURI());
                		} else {
                			if(dolog) {
                                DependencyTracker.logImage(context, i18nRes, parent_part_in, parent_product_in, targetGen, targetKey, "image");
                            }
                		}
                	}
                }
            } else {
                String uri = null;
                if(module != null) {
                    uri = "module://" + module + "/" + src;
                    src = "modules/" + module + "/" + src;
                } else {
                    uri = "docroot:/" + src;
                }
                if(!i18n) {
                	res = ResourceUtil.getResource(uri);
                } else {
                	I18NIterator it = new I18NIterator(tenant, language, uri);
                	while(it.hasNext()) {
                		String i18nUri = it.next();
                		Resource i18nRes = ResourceUtil.getResource(i18nUri);
                		if(res == null && (i18nRes.exists() || !it.hasNext())) {
                			res = i18nRes;
                			src = I18NResourceUtil.getURLPath(i18nRes.toURI());
                		} else {
                			if(dolog) {
                                DependencyTracker.logImage(context, i18nRes, parent_part_in, parent_product_in, targetGen, targetKey, "image");
                            }
                		}
                	}
                }
            }
            if(dolog) {
                DependencyTracker.logImage(context, res, parent_part_in, parent_product_in, targetGen, targetKey, "image");
            }
            return src;
        } else if (isThemedSrc(src, themed_path, themed_img)) {
            if (themed_path.startsWith("/")) {
                themed_path = themed_path.substring(1);
            }

            String testsrc = null;
            
            if(dynamic) {
                String themeParam = "?themes=";
                for (int i = 0; i < themes.length; i++) {
                    themeParam += themes[i];
                    if(i<themes.length-1) themeParam += ",";
                }
                String uri =  "dynamic:/" + themed_path +"/THEME/" + themed_img;
                uri += themeParam;
                if(module != null) {
                    uri += "&module="+module;
                    if(filter!=null) uri += "&filter=" + URLEncoder.encode(filter, "UTF-8");
                }
                Resource res = ResourceUtil.getResource(uri);
                URI resUri = res.toURI();
                if("module".equals(resUri.getScheme()) && res.exists()) {
                    testsrc = "modules/"+resUri.getAuthority()+resUri.getPath();
                } else {
                    testsrc = resUri.getPath();
                    if(testsrc.startsWith("/")) testsrc=testsrc.substring(1);
                }
                String parent_path = IncludeDocumentExtension.getSystemId(context);
                Resource relativeParent = parent_path.equals("") ? null : ResourceUtil.getResource(parent_path);
                if(dolog) {
                    DependencyTracker.logTyped("image", res, "", "", relativeParent, parent_part_in, parent_product_in, target);
                }
            } else {
            
            for (int i = 0; i < themes.length; i++) {
                String currtheme = themes[i];
                testsrc = themed_path + "/" + currtheme + "/" + themed_img;
                String uri = null;
                if(module!=null) {
                    uri = "module://" + module + "/" + testsrc;
                    testsrc = "modules/" + module + "/" + testsrc;
                } else {
                    uri = "docroot:/" + testsrc;
                }
                Resource res = ResourceUtil.getResource(uri);
                LOG.info("  -> Trying to find image src '" + testsrc + "'");
                if (res.exists()) {
                    LOG.info("    -> Found src '" + testsrc + "'");
                    if(dolog) {
                        DependencyTracker.logImage(context, res, parent_part_in, parent_product_in, targetGen, targetKey, "image");
                    }
                    return testsrc;
                }
                if (i < (themes.length - 1)) {
                    // FIXME: the next commented line should be used sometime so we can discriminate between
                    // "real" missing and "missing, but we found a better version" -- but make sure editor copes with it.
                    //DependencyTracker.logImage(context, testsrc, parent_part_in, parent_product_in, targetGen, targetKey, "shadow");
                    if(dolog) {
                        DependencyTracker.logImage(context, res, parent_part_in, parent_product_in, targetGen, targetKey, "image");
                    }
                    LOG.info("    -> Image src '" + testsrc + "' not found, trying next theme");
                } else {
                    if(dolog) {
                        DependencyTracker.logImage(context, res, parent_part_in, parent_product_in, targetGen, targetKey, "image");
                    }
                    LOG.warn("    -> No themed image found!");
                }
            }
            
            }
            return testsrc;
        } else {
            throw new XMLException("Need to have one of 'src' XOR both 'themed-path' and 'themed-img' given!");
        }
    }


    private static boolean isSimpleSrc(String src, String path, String img) {
        return (src != null && !src.equals("") && (path == null || path.equals("")) && (img == null || img.equals("")));
    }

    private static boolean isThemedSrc(String src, String path, String img) {
        return ((src == null || src.equals("")) && path != null && !path.equals("") && img != null && !img.equals(""));
    }
    
    private static Target getParentTarget(XsltContext context) {
        URIResolver resolver = context.getURIResolver();
        if(resolver != null && resolver instanceof ResourceResolver) {
            ResourceResolver resResolver = (ResourceResolver)resolver;
            return resResolver.getParentTarget();
        }
        return null;
    }

}
