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

package de.schlund.pfixxml.targets;


import javax.xml.transform.TransformerException;

import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.Xml;

/**
 * XMLLeafTarget.java
 *
 *
 * Created: Mon Jul 23 21:53:06 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class XMLLeafTarget extends LeafTarget {

    public XMLLeafTarget(TargetType type, TargetGenerator gen, String key, Themes themes) throws Exception {
        this.type      = type;
        this.generator = gen;
        this.targetkey = key;
        this.themes    = themes;
        Resource targetpath = ResourceUtil.getResource(key);
        this.sharedleaf = generator.getSharedLeafFactory().getSharedLeaf(generator.getXsltVersion(),targetpath);
        // Create empty manager to avoid null pointer exceptions
        this.auxdepmanager = new AuxDependencyManager(this, gen.getAuxDependencyFactory(), gen.getTargetDependencyRelation());
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetImpl#getValueFromDiscCache()
     */
    @Override
    protected Object getValueFromDiscCache() throws TransformerException {
        Resource thefile = ResourceUtil.getResource(getTargetKey());
        if (thefile.exists() && thefile.isFile()) {
            return Xml.parse(generator.getXsltVersion(), thefile);
        } else {
            return null;
        }
    }

}// XMLLeafTarget
