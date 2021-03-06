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
package de.schlund.pfixcore.example.webservices.chat;

import java.util.Calendar;

/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Message {
    
    private String from;
    private String text;
    private Calendar date;
    
    public Message() {   
    }
    
    public Message(String from,String text,Calendar date) {
    	this.from=from;
        this.text=text;
        this.date=date;
    }
    
    public String getFrom() {
    	return from;
    }
    
    public void setFrom(String from) {
        this.from=from;
    }
    
    public String getText() {
    	return text;
    }
    
    public void setText(String text) {
        this.text=text;
    }
    
    public Calendar getDate() {
    	return date;
    }
    
    public void setDate(Calendar date) {
        this.date=date;
    }

}
