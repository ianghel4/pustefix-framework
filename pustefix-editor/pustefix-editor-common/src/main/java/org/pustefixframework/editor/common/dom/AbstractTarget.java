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

package org.pustefixframework.editor.common.dom;

/**
 * Provides functionality common to all classes implementing Target
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class AbstractTarget implements Target {

    /* (non-Javadoc)
     * @see de.schlund.pfixcore.editor2.core.dom.Target#isLeafTarget()
     */
    public boolean isLeafTarget() {
        if ((this.getParentXML() == null) || (this.getParentXSL() == null)) {
            return true;
        } else {
            return false;
        }
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Target) {
            Target target = (Target) obj;
            return this.getName().equals(target.getName()) && ((this.getProject() == null) ? (target.getProject() == null) : this.getProject().equals(target.getProject()));
        } else {
            return false;
        }
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return ("TARGET: " + this.toString()).hashCode();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return (this.getProject() == null) ? "<null>" : this.getProject().getName() + "/" + this.getName();
    }
}
