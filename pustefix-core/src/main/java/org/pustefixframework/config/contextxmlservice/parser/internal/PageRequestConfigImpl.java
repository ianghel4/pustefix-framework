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

package org.pustefixframework.config.contextxmlservice.parser.internal;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.pustefixframework.config.contextxmlservice.PageRequestConfig;
import org.pustefixframework.config.contextxmlservice.ProcessActionPageRequestConfig;
import org.pustefixframework.config.contextxmlservice.SSLOption;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import de.schlund.pfixcore.auth.AuthConstraint;
import de.schlund.pfixcore.workflow.State;

/**
 * Stores configuration for a PageRequest
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageRequestConfigImpl implements Cloneable, PageRequestConfig, SSLOption {
    
    private String pageName = null;
    private boolean ssl = false;
    private AuthConstraint authConstraint;
    private String defaultFlow = null;
    private Properties props = new Properties();
    private LinkedHashMap<String, ProcessActionPageRequestConfigImpl> actions = new LinkedHashMap<String, ProcessActionPageRequestConfigImpl>();
    private State state;
    private RuntimeBeanReference stateReference;
    
    public void setPageName(String page) {
        this.pageName = page;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.PageRequestConfig#getPageName()
     */
    public String getPageName() {
        return this.pageName;
    }
    
    public void setSSL(boolean forceSSL) {
        this.ssl = forceSSL;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixxml.config.PageRequestConfig#isSSL()
     */
    public boolean isSSL() {
        return this.ssl;
    }
    
    public AuthConstraint getAuthConstraint() {
    	return authConstraint;
    }
    
    public void setAuthConstraint(AuthConstraint authConstraint) {
    	this.authConstraint = authConstraint;
    }

    public String getDefaultFlow() {
        return defaultFlow;
    }

    public void setDefaultFlow(String defaultFlow) {
        this.defaultFlow = defaultFlow;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public RuntimeBeanReference getStateReference() {
        return this.stateReference;
    }

    public void setStateReference(RuntimeBeanReference stateReference) {
        this.stateReference = stateReference;
    }
    
    public void setProperties(Properties props) {
        this.props = new Properties();
        Enumeration<?> e = props.propertyNames();
        while (e.hasMoreElements()) {
            String propname = (String) e.nextElement();
            this.props.setProperty(propname, props.getProperty(propname));
        }
    }
    
    public Properties getProperties() {
        return this.props;
    }
    
    public Map<String, ProcessActionPageRequestConfigImpl> getProcessActions() {
        return Collections.unmodifiableMap(this.actions);
    }

    public void setProcessActions(Map<String, ProcessActionPageRequestConfigImpl> actions) {
        this.actions = new LinkedHashMap<String, ProcessActionPageRequestConfigImpl>(actions);
    }

    public BeanDefinition generateBeanDefinition(Map<String, ? extends ProcessActionPageRequestConfig> processActionConfigs) {
        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(PageRequestConfigImpl.class);
        beanBuilder.setScope("singleton");
        beanBuilder.addPropertyValue("pageName", getPageName());
        beanBuilder.addPropertyValue("SSL", isSSL());
        beanBuilder.addPropertyValue("authConstraint", getAuthConstraint());
        beanBuilder.addPropertyValue("defaultFlow", getDefaultFlow());
        beanBuilder.addPropertyValue("state", getStateReference());
        beanBuilder.addPropertyValue("properties", getProperties());
        beanBuilder.addPropertyValue("processActions", processActionConfigs);
        
        return beanBuilder.getBeanDefinition();
    }
 }