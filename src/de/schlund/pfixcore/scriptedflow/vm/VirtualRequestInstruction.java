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
 */

package de.schlund.pfixcore.scriptedflow.vm;

import java.util.List;
import java.util.Map;

import de.schlund.pfixcore.scriptedflow.vm.pvo.ParamValueObject;

public class VirtualRequestInstruction implements Instruction {
    private String pagename;
    private boolean reuseparams = false;
    private boolean dointeractive = false;
    
    private Map<String, List<ParamValueObject>> params;

    public VirtualRequestInstruction(String pagename,
            Map<String, List<ParamValueObject>> params, boolean reuseparams, boolean dointeractive) {
        this.pagename = pagename;
        this.params = params;
        this.reuseparams = reuseparams;
        this.dointeractive = dointeractive;
    }
    
    String getPagename() {
        return pagename;
    }
    
    boolean getDoInteractive() {
        return dointeractive;
    }
    
    boolean getReuseParamsForInteractive() {
        return reuseparams;
    }
    
    Map<String, List<ParamValueObject>> getParams() {
        return params;
    }
}