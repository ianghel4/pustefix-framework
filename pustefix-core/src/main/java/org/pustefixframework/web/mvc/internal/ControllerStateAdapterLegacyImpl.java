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
package org.pustefixframework.web.mvc.internal;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.pustefixframework.web.mvc.AnnotationMethodHandlerAdapterConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import de.schlund.pfixxml.PfixServletRequest;

/**
 * Bridge from Pustefix States to Spring MVC's legacy AnnotationMethodHandlerAdapter.
 */
public class ControllerStateAdapterLegacyImpl implements ControllerStateAdapter, InitializingBean {

	private AnnotationMethodHandlerAdapter adapter;
	private AnnotationMethodHandlerAdapterConfig adapterConfig;
	private ConcurrentHashMap<Class<?>, Boolean> mappedClassCache = new ConcurrentHashMap<Class<?>, Boolean>();

	public void setAdapterConfig(AnnotationMethodHandlerAdapterConfig adapterConfig) {
		this.adapterConfig = adapterConfig;
	}

	/**
     * Call AnnotationMethodHandlerAdapter if State class contains request mappings.
     */
	@Override
	public ModelAndView tryHandle(PfixServletRequest request, Object handler, String pageName) throws Exception {
		if(hasRequestMapping(handler.getClass())) {
			try {
				ControllerRequestWrapper wrappedRequest = new ControllerRequestWrapper(request, pageName);
				ControllerResponseWrapper response = new ControllerResponseWrapper();
				return adapter.handle(wrappedRequest, response, handler);
			} catch(NoSuchRequestHandlingMethodException x) {
				//let implementing a handler method be optional and ignore this exception
			}
		}
		return null;
	}

    @Override
    public void afterPropertiesSet() throws Exception {
        adapter = new AnnotationMethodHandlerAdapter();
        adapter.setCustomArgumentResolvers(adapterConfig.getCustomArgumentResolvers());
    }

	/**
	 * Check if class contains request mapping methods.
	 */
	private boolean hasRequestMapping(Class<?> clazz) {
		Boolean hasMapping = mappedClassCache.get(clazz);
		if(hasMapping == null) {
			Method[] methods = clazz.getMethods();
			for(Method method: methods) {
				RequestMapping mapping = method.getAnnotation(RequestMapping.class);
				if(mapping != null) {
					hasMapping = true;
					break;
				}
			}
			if(hasMapping == null) {
				hasMapping = false;
			}
			mappedClassCache.putIfAbsent(clazz, hasMapping);
		}
		return hasMapping;
	}

}
