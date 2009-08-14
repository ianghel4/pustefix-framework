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
package de.schlund.pfixcore.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.pustefixframework.resource.InputStreamResource;
import org.pustefixframework.resource.Resource;
import org.pustefixframework.xmlgenerator.targets.AuxDependency;
import org.pustefixframework.xmlgenerator.targets.AuxDependencyFactory;
import org.pustefixframework.xmlgenerator.targets.AuxDependencyFile;
import org.pustefixframework.xmlgenerator.targets.AuxDependencyImage;
import org.pustefixframework.xmlgenerator.targets.AuxDependencyInclude;
import org.pustefixframework.xmlgenerator.targets.DependencyType;
import org.pustefixframework.xmlgenerator.targets.TargetGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.schlund.pfixxml.util.Xml;

/**
 * CheckIncludes.java
 *
 *
 * Created: Tue Apr  8 15:42:11 2003
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version $Id$
 */

public class CheckIncludes {
    // private static final String XPATH = "/include_parts/part/theme";
    
    private HashMap<String, TargetGenerator> generators = new HashMap<String, TargetGenerator>();
    private TreeSet<Resource> includefilenames = new TreeSet<Resource>();
    private TreeSet<Resource> imagefilenames = new TreeSet<Resource>();
    private TreeSet<AuxDependency> unavail = new TreeSet<AuxDependency>();
    private TreeSet<AuxDependency> includes;
    private String  pwd;
    private String  outfile;

    static {
//        dbfac.setNamespaceAware(true);
    }
    
//    public CheckIncludes(String pwd, String outfile, File allprj, File allincs, File allimgs) throws Exception {
//        this.pwd     = pwd;
//        this.outfile = outfile;
//        String         line;
//        BufferedReader input;
//        
//        input = new BufferedReader(new FileReader(allincs));
//        while ((line = input.readLine()) != null) {
//            // line = new File(pwd + line).getCanonicalPath();
//            includefilenames.add(ResourceUtil.getFileResourceFromDocroot(line));
//        }
//        input.close();
//
//        input = new BufferedReader(new FileReader(allimgs));
//        while ((line = input.readLine()) != null) {
//            // line = new File(pwd + line).getCanonicalPath();
//            imagefilenames.add(ResourceUtil.getFileResourceFromDocroot(line));
//        }
//        input.close();
//        
//        input = new BufferedReader(new FileReader(allprj));
//        while ((line = input.readLine()) != null) {
//            // line = pwd + line;
//            TargetGenerator gen = new TargetGenerator(ResourceUtil.getFileResourceFromDocroot(line));
//            generators.put(pwd + line, gen);
//        }
//        input.close();
//
//        includes = AuxDependencyFactory.getInstance().getAllAuxDependencies();
//        for (Iterator<AuxDependency> i = includes.iterator(); i.hasNext();) {
//            AuxDependency aux = i.next();
//            unavail.add(aux);
//        }
//    }

    public void doCheck(AuxDependencyFactory auxDependencyFactory) throws Exception {
        Document doc         = Xml.createDocument();

        Element  check_root  = doc.createElement("checkresult");
        check_root.setAttribute("xmlns:ixsl","http://www.w3.org/1999/XSL/Transform");
        check_root.setAttribute("xmlns:pfx", "http://www.schlund.de/pustefix/core");
        check_root.setAttribute("xmlns:hlp", "http://www.schlund.de/pustefix/onlinehelp");
        Element  files_root  = doc.createElement("includefiles");
        Element  images_root = doc.createElement("images");
        Element  prj_root    = doc.createElement("projects");
        doc.appendChild(check_root);
        check_root.appendChild(files_root);
        check_root.appendChild(images_root);
        check_root.appendChild(prj_root);
        
        checkForUnusedIncludes(auxDependencyFactory, doc, files_root);
        checkForUnusedImages(auxDependencyFactory, doc, images_root);

        checkForUnavailableIncludes(auxDependencyFactory, doc, prj_root);
        
        Xml.serialize(doc, outfile, true, true);
    }
    
//    public static void main(String[] args) throws Exception {
//        String output    = args[0];
//        String allprjarg = args[1];
//        String allincarg = args[2];
//        String allimgarg = args[3];
//
//        String dir = new File(".").getCanonicalPath() + "/";
//        
//        Logging.configure("generator_quiet.xml");
//        GlobalConfigurator.setDocroot(dir);
//        
//        CheckIncludes instance = new CheckIncludes(dir, output, new File(allprjarg), new File(allincarg), new File(allimgarg));
//        instance.doCheck();
//
//    }

    private void checkForUnavailableIncludes(AuxDependencyFactory auxDependencyFactory, Document result, Element res_root) throws Exception {
        for (Iterator<String> i = generators.keySet().iterator(); i.hasNext();) {
            String depend = i.next();
            TargetGenerator gen = generators.get(depend);
            TreeSet<AuxDependency> deps = auxDependencyFactory.getTargetDependencyRelation().getProjectDependencies(gen);
            if (deps == null) {
                return;
            }

            for (Iterator<AuxDependency> j = deps.iterator(); j.hasNext();) {
                AuxDependency aux = j.next();
                if (!unavail.contains(aux) || aux.getType().equals(DependencyType.FILE) || aux.getType().equals(DependencyType.TARGET)) {
                    j.remove();
                }
            }
            
            if (!deps.isEmpty()) {
                Element prj_elem = result.createElement("project");
                String  name     = depend.substring(pwd.length());
                prj_elem.setAttribute("name", name);
                res_root.appendChild(prj_elem);
                
                for (Iterator<AuxDependency> j = deps.iterator(); j.hasNext(); ) {
                    AuxDependency aux = j.next();
                    Element elem = result.createElement("MISSING");
                    prj_elem.appendChild(elem);
                    elem.setAttribute("type", aux.getType().toString());
                    String path = ((AuxDependencyFile) aux).getPath().getURI().toString();
                    if (aux.getType().equals(DependencyType.TEXT)) {
                        AuxDependencyInclude a = (AuxDependencyInclude) aux;
                        elem.setAttribute("path", path);
                        elem.setAttribute("part", a.getPart());
                        elem.setAttribute("theme", a.getTheme());
                    } else if (aux.getType().equals(DependencyType.IMAGE)) {
                        AuxDependencyImage a = (AuxDependencyImage) aux;
                        elem.setAttribute("path", a.getPath().getURI().toString());
                    }
                }
            }
        }
    }

    private void checkForUnusedImages(AuxDependencyFactory auxDependencyFactory, Document result, Element res_root) throws Exception {
        for (Iterator<Resource> i = imagefilenames.iterator(); i.hasNext();) {
            Resource img = i.next();

            Element res_image = result.createElement("image");
            res_image.setAttribute("name", img.getURI().toString());
            
            res_root.appendChild(res_image);
            
            AuxDependency aux =  auxDependencyFactory.getAuxDependencyImage(img);
            if (!includes.contains(aux)) {
                res_image.setAttribute("UNUSED", "true");
                continue;
            } else {
                unavail.remove(aux);
            }
        }
    }
    
    private void checkForUnusedIncludes(AuxDependencyFactory auxDependencyFactory, Document result, Element res_root) throws Exception {
        for (Iterator<Resource> i = includefilenames.iterator(); i.hasNext();) {
            Resource path = i.next();
            Document doc;

            Element res_incfile = result.createElement("incfile");
            res_root.appendChild(res_incfile);
            res_incfile.setAttribute("name", path.getURI().toString());
            
            try {
                doc = Xml.parseMutable((InputStreamResource)path);
            } catch (Exception e) {
                Element error = result.createElement("ERROR");
                res_incfile.appendChild(error);
                error.setAttribute("cause", e.getMessage());
                continue;
            }
            
            Element root = doc.getDocumentElement();
            if (!root.getNodeName().equals("include_parts")) {
                Element error = result.createElement("ERROR");
                res_incfile.appendChild(error);
                error.setAttribute("cause", "not an include file (root != include_parts)");
                continue;
            }
            
            NodeList nl = root.getChildNodes();
            for (int j = 0; j < nl.getLength(); j++) {
                if (nl.item(j) instanceof Element) {
                    Element partelem = (Element) nl.item(j);
                    
                    if (!partelem.getNodeName().equals("part")) {
                        Element error = result.createElement("ERROR");
                        res_incfile.appendChild(error);
                        error.setAttribute("cause", "invalid node in include file (child of root != part): " + path.getURI().getPath().substring(1) + "/" + partelem.getNodeName());
                        continue;
                    }

                    Element res_part = result.createElement("part");
                    res_incfile.appendChild(res_part);
                    res_part.setAttribute("name", partelem.getAttribute("name"));
                    
                    NodeList prodchilds = partelem.getChildNodes();
                    for (int k = 0; k < prodchilds.getLength(); k++) {
                        if (prodchilds.item(k) instanceof Element) {
                            Element themeelem = (Element) prodchilds.item(k);
                            if (!themeelem.getNodeName().equals("theme")) {
                                Element error = result.createElement("ERROR");
                                res_part.appendChild(error);
                                error.setAttribute("cause", "invalid node in part (child of part != theme): " +
                                                   path.getURI().getPath().substring(1) + "/" + partelem.getNodeName() + "/" + themeelem.getNodeName());
                                continue;
                            }

                            String part    = partelem.getAttribute("name");
                            String theme = themeelem.getAttribute("name");

                            Element res_theme = result.createElement("theme");
                            res_part.appendChild(res_theme);
                            res_theme.setAttribute("name", theme);
                            
                            AuxDependency aux = auxDependencyFactory.getAuxDependencyInclude(path, part, theme);
                            if (!includes.contains(aux)) {
                                res_theme.setAttribute("UNUSED", "true");
                                continue;
                            } else {
                                unavail.remove(aux);
                            }
                            

                            NodeList langchilds = themeelem.getChildNodes();
                            for (int l = 0; l < langchilds.getLength(); l++) {
                                if (langchilds.item(l) instanceof Element) {
                                    Element langelem = (Element) langchilds.item(l);
                                    Node res_lang = result.importNode(langelem, true); 
                                    res_theme.appendChild(res_lang);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
} // CheckIncludes