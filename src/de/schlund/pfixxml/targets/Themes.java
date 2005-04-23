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

package de.schlund.pfixxml.targets;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Describe class Themes here.
 *
 *
 * Created: Fri Apr 22 13:57:33 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class Themes {
    String[] themes;
    String   id;
    
    /**
     * Creates a new <code>Themes</code> instance.
     *
     */
    public Themes(String[] themes) {
        if (themes == null) {
            throw new RuntimeException("Themes array must not be null");
        }
        if (themes.length == 0) {
            throw new RuntimeException("Themes array must not be empty");
        }
        this.themes = themes;
        StringBuffer themesstr = new StringBuffer("");
        for (int i = 0; i < themes.length; i++) {
            if (themesstr.length() > 0) {
                themesstr.append(" ");
            }
            themesstr.append(themes[i]);
        }
        id = themesstr.toString();
    }

    public Themes(String id) {
        if (id == null) {
            throw new RuntimeException("Themes id must not be null");
        }
        if (id.equals("")) {
            throw new RuntimeException("Themes id must not be empty");
        }
        this.id = id;
        StringTokenizer tok  = new StringTokenizer(id);
        ArrayList       list = new ArrayList();
        while (tok.hasMoreElements()) {
            String currtok = tok.nextToken();
            list.add(currtok);
        }
        themes = (String[]) list.toArray(new String[]{});
    }
    
    public String getId() {
        return id;
    }

    public String[] getThemesArr() {
        return themes;
    }
}
