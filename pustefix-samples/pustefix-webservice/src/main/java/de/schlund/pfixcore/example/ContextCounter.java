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

package de.schlund.pfixcore.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ContextCounter.java
 *
 *
 * Created: Thu Oct 22 19:24:37 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class ContextCounter {
    private Boolean  showcounter = Boolean.FALSE;
    private int      counter     = 0;

    private final static Logger LOG = LoggerFactory.getLogger(ContextCounter.class);
    
    public void reset() {
        showcounter = Boolean.FALSE;
        counter     = 0;
    }

    public Boolean getShowCounter() { return showcounter; }

    public int getCounter() { return counter; }
    
    public void setShowCounter(Boolean showcounter) {
        this.showcounter = showcounter;
    }

    public void setCounter(int count) {
        counter = count;
    }

    public void addToCounter(int count) {
        counter += count;
    }

    public boolean needsData() {
        return false;
    }

    @Override
    public String toString() {
        LOG.debug("Doing ContextCounter...");
        return "[showcounter?: " + showcounter + "][counter?: " + counter + "]";
    }

}// ContextCounter
