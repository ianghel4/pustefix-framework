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
package de.schlund.pfixcore.generator;

import de.schlund.util.statuscodes.StatusCode;

/**
 * Describe class StatusCodeInfo here.
 *
 *
 * Created: Mon Jul 25 11:06:07 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class StatusCodeInfo {
    private StatusCode scode;
    private String[]   args;
    private String     level;
    
    /**
     * Creates a new <code>StatusCodeInfo</code> instance.
     *
     * @param scode a <code>StatusCode</code> value
     */
    public StatusCodeInfo(StatusCode scode, String[] args, String level) {
        this.scode = scode;
        this.args  = args;
        this.level = level;
    }

    public StatusCode getStatusCode() {
        return scode;
    }

    public String[] getArgs() {
        return args;
    }

    public String getLevel() {
        return level;
    }

    @Override
    public boolean equals(Object in) {
        if (!(in instanceof StatusCodeInfo)) {
            return false;
        }
        return this.hashCode() == ((StatusCodeInfo) in).hashCode();
    }

    @Override
    public String toString() {
        StringBuffer tmp = new StringBuffer();
        tmp.append(scode.getStatusCodeId() + "|");
        if (args != null) {
            for (int i = 0; i < args.length ; i++) {
                tmp.append(args[i]);
            }
        }
        tmp.append("|");
        if (level != null) {
            tmp.append(level);
        }
        return tmp.toString();
    }
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
