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

package de.schlund.pfixcore.beans;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;

import de.schlund.pfixcore.beans.metadata.Beans;
import de.schlund.pfixcore.beans.metadata.DOMInit;
import de.schlund.pfixcore.beans.metadata.Locator;


/**
 * @author mleidig@schlund.de
 */
public class BeanDescriptorFactory {

    Map<Class<?>,BeanDescriptor> descriptors;
    Beans metadata;
    
    public BeanDescriptorFactory() {
        descriptors=new HashMap<Class<?>,BeanDescriptor>();
    }
    
    public BeanDescriptorFactory(Beans metadata) {
        this();
        this.metadata=metadata;
    }
    
    public BeanDescriptorFactory(Locator locator) throws InitException {
        this();
        DOMInit domInit=new DOMInit();
        for(URL url:locator.getMetadataResources()) {
            domInit.update(url);
        }
        metadata=domInit.getBeans();
    }
    
    @SuppressWarnings("unchecked")
    public synchronized <T> BeanDescriptor getBeanDescriptor(Class<T> clazz) {
        if(Enhancer.isEnhanced(clazz)) {
            clazz = (Class<T>)clazz.getSuperclass();
        }
        BeanDescriptor desc=descriptors.get(clazz);
        if(desc==null) {
            desc=new BeanDescriptor(clazz,metadata);
            descriptors.put(clazz,desc);
        }
        return desc;
    }
    
}
