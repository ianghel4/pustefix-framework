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
package de.schlund.pfixcore.oxm;

import de.schlund.pfixcore.oxm.impl.annotation.XMLFragmentSerializer;

/**
 * Simple test bean to test the serialization
 * of XML fragments
 * 
 * @author  Stephan Schmidt <schst@stubbles.net>
 */
public class FragmentBean {

    private String myFragment = "<foo><bar baz=\"true\"/>character data</foo>";

    @XMLFragmentSerializer
    public String anotherFragment = "This is a <real>fragment</real>.";

    @XMLFragmentSerializer
    public String thirdFragment = "<foo/><bar>baz</bar>";

    @XMLFragmentSerializer
    public String getMyFragment() {
        return myFragment;
    }
}