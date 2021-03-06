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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import org.pustefixframework.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.beans.Exclude;

/**
 * ContextAdultInfo.java
 *
 *
 * Created: Thu Oct 22 19:24:37 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class ContextAdultInfo implements Serializable {
    
    private static final long serialVersionUID = -7113285838799932518L;
    
    private transient Logger LOG = LoggerFactory.getLogger(ContextAdultInfo.class);
    
    private Boolean adult = null;
    private Date date = null;
    private HashMap<String, String> test  = new HashMap<String, String>();
    
    @Autowired
    EventBus eventBus;
    
    public void setIndexedTest(HashMap<String, String> inmap) {
        test = inmap;
    }

    @Exclude
    public HashMap<String, String> getIndexedTest() {
        return test;
    }
    
    public Boolean getAdult() { return adult; }

    public void setAdult(Boolean adult) {
        this.adult = adult;
        eventBus.publish(new AdultInfoChangeEvent(adult, this));
    }
    
    public void setDate(Date date) {
    	this.date = date;
    }
    
    public Date getDate() {
        return date;
    }

    public boolean needsData() {
        if (adult == null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        LOG.debug("Doing ContextAdultInfo...");
        return "[Adult?: " + adult + "]";
    }
    
}// ContextAdultInfo
