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
package de.schlund.pfixcore.generator;

import java.io.IOException;


/**
 * IWrappers are objects which aggregate part of the submitted data
 * of a HTTP post or get event in a typesafe way and present getter
 * and setter methods to manipulate that data.</br>
 * Classes which implement the IWrapper interface are not written by
 * hand, but are autogenerated from a describtion written in a 
 * special xml format.</br>
 * From the application programmers point of view IWrappers
 * are never instanciated directly. Following snipplet
 * shows the use of an <code>IWrapper</code> in the handleSubmittedData method
 * of an {@link IHandler}.
 * <pre>
 *   public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
 *      AdultInfo info = (AdultInfo) wrapper;
 *   }
 * </pre>
 * <br/>
 * For more information see the 'IWrapper_XML_Description_Language.txt' in the docs
 * directory of the Pfixcore package.
 */
     
public interface IWrapper extends Comparable {
    void init(String prefix) throws Exception;
    void initLogging(String logdir, String pagename, String visitid);
    void tryLogging() throws IOException;
    void load(RequestData req) throws Exception;
    // The reason for these to not being called get* is to avoid nameclashes with
    // descendents who may want to use a Parameter called e.g. "Prefix" (which would
    // result in a method getPrefix be generated)
    String              gimmePrefix();
    IHandler            gimmeIHandler();
    boolean             errorHappened();
    IWrapperParamInfo[] gimmeAllParamInfos();
    IWrapperParamInfo[] gimmeAllParamInfosWithErrors();
    void                defineOrder(int order);
}
