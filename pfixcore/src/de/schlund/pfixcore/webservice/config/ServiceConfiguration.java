/*
 * de.schlund.pfixcore.webservice.config.ServiceConfiguration
 */
package de.schlund.pfixcore.webservice.config;

import java.util.*;

/**
 * ServiceConfiguration.java 
 * 
 * Created: 22.07.2004
 * 
 * @author mleidig
 */
public class ServiceConfiguration {

    private ConfigProperties props;
    private ServiceGlobalConfig globConf;
    private HashMap srvsConf;
    
    public ServiceConfiguration(ConfigProperties props) throws ServiceConfigurationException {
        this.props=props;
        init();
    }
    
    private void init() throws ServiceConfigurationException {
        globConf=new ServiceGlobalConfig(props);
        srvsConf=new HashMap();
        Iterator it=props.getPropertyKeys("webservice\\.[^\\.]*\\.name");
        while(it.hasNext()) {
            String key=(String)it.next();
            String name=props.getProperty(key);
            ServiceConfig sc=new ServiceConfig(props,name);
            srvsConf.put(name,sc);
        }
    }
    
    public void reload() throws ServiceConfigurationException {
        globConf.reload();
        HashSet names=new HashSet();
        Iterator it=props.getPropertyKeys("webservice\\.[^\\.]*\\.name");
        while(it.hasNext()) {
            String key=(String)it.next();
            String name=props.getProperty(key);
            ServiceConfig sc=(ServiceConfig)srvsConf.get(name);
            if(sc!=null) sc.reload();
            else {
                sc=new ServiceConfig(props,name);
                srvsConf.put(name,sc);
            }
            names.add(name);
        }
        it=srvsConf.keySet().iterator();
        while(it.hasNext()) {
            String name=(String)it.next();
            if(!names.contains(name)) srvsConf.remove(name);
        }
    }
    
    public ServiceGlobalConfig getServiceGlobalConfig() {
        return globConf;
    }
    
    public ServiceConfig getServiceConfig(String name) {
        return (ServiceConfig)srvsConf.get(name);
    }
    
    public Iterator getServiceConfig() {
        return srvsConf.values().iterator();
    }
    
}
