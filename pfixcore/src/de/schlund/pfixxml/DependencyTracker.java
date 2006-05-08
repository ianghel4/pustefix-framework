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


import org.apache.log4j.Category;

import com.icl.saxon.Context;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.DependencyType;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.TargetGeneratorFactory;
import de.schlund.pfixxml.targets.VirtualTarget;

public class DependencyTracker {
    private static Category CAT = Category.getInstance(DependencyTracker.class.getName());
    
    /** xslt extension */
    public static String logImage(Context context, String path,
                                  String parent_part_in, String parent_theme_in,
                                  String targetGen, String targetKey, String type) throws Exception {

        if (targetKey.equals("__NONE__")) {
            return "0";
        }

        FileResource    tgen_path = ResourceUtil.getFileResource(targetGen);
        TargetGenerator gen       = TargetGeneratorFactory.getInstance().createGenerator(tgen_path);
        VirtualTarget   target    = (VirtualTarget) gen.getTarget(targetKey);

        String parent_path  = "";
        String parent_part  = "";
        String parent_theme = "";

        if (IncludeDocumentExtension.isIncludeDocument(context)) {
            parent_path  = IncludeDocumentExtension.makeSystemIdRelative(context);
            parent_part  = parent_part_in;
            parent_theme = parent_theme_in;
        }
        
        if (target == null) {
            CAT.error("Error adding Dependency: target not found (targetGen=" + targetGen + ", targetKey=" + targetKey + ")");
            return "1";
        }
        if (path.length() == 0) {
            CAT.error("Error adding Dependency: empty path"); 
            return "1"; 
        }
        FileResource relativePath   = ResourceUtil.getFileResourceFromDocroot(path);
        FileResource relativeParent = parent_path.equals("") ? null : ResourceUtil.getFileResourceFromDocroot(parent_path);
        try {
            logTyped(type, relativePath, "", "", relativeParent, parent_part, parent_theme, target);
            return "0";
        } catch (Exception e) {
            CAT.error("Error adding Dependency: ",e); 
            return "1"; 
        }
    }
    
    public static void logTyped(String type, FileResource path, String part, String theme,
                                FileResource parent_path, String parent_part, String parent_theme,
                                VirtualTarget target) {
        if (CAT.isDebugEnabled()) {
            String project = target.getTargetGenerator().getName();
            CAT.debug("Adding dependency to AuxdependencyManager :+\n"+
                      "Type       = " + type + "\n" +
                      "Path       = " + path.toURI().getPath().substring(1) + "\n" +
                      "Part       = " + part + "\n" +
                      "Theme      = " + theme + "\n" +
                      "ParentPath = " + ((parent_path == null)? "null" : parent_path.toURI().getPath().substring(1)) + "\n" +
                      "ParentPart = " + parent_part + "\n" +
                      "ParentProd = " + parent_theme + "\n" +
                      "Project    = " + project + "\n");
        }
        DependencyType  thetype   = DependencyType.getByTag(type);
        if (thetype == DependencyType.TEXT) {
            target.getAuxDependencyManager().addDependencyInclude(path, part, theme, parent_path, parent_part, parent_theme);
        } else if (thetype == DependencyType.IMAGE) {
            target.getAuxDependencyManager().addDependencyImage(path, parent_path, parent_part, parent_theme);
        } else {
            throw new RuntimeException("Unknown dependency type '" + type + "'!");
        }
    }
}
