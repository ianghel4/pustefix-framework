/*
 * de.schlund.pfixcore.webservice.config.ServiceConfig
 */
package de.schlund.pfixcore.webservice.config;

import java.util.*;

import de.schlund.pfixcore.webservice.*;

/**
 * ServiceConfig.java 
 * 
 * Created: 27.07.2004
 * 
 * @author mleidig
 */
public class ServiceConfig extends AbstractConfig {

    private final static String PROP_PREFIX="webservice.";
    private final static String PROP_ITFNAME=".interface.name";
    private final static String PROP_IMPLNAME=".implementation.name";
    private final static String PROP_CTXNAME=".context.name";
    private final static String PROP_SESSTYPE=".session.type";
    private final static String PROP_ENCODINGSTYLE=".encoding.style";
    private final static String PROP_ENCODINGUSE=".encoding.use";

    ConfigProperties props;
    String name;
    String itfName;
    String implName;
    String ctxName;
    String sessType=Constants.SESSION_TYPE_SERVLET;
    String encStyle;
    String encUse;
    HashMap params;
    
    public ServiceConfig(ConfigProperties props,String name) throws ConfigException {
        this.props=props;
        this.name=name;
        init();
    }
    
    private void init() throws ConfigException {
        String prefix=PROP_PREFIX+name;
        itfName=props.getStringProperty(prefix+PROP_ITFNAME,true);
        implName=props.getStringProperty(prefix+PROP_IMPLNAME,true);
        ctxName=props.getStringProperty(prefix+PROP_CTXNAME,false);
        sessType=props.getStringProperty(prefix+PROP_SESSTYPE,Constants.SESSION_TYPES,true);
        encStyle=props.getStringProperty(prefix+PROP_ENCODINGSTYLE,Constants.ENCODING_STYLES,false);
        encUse=props.getStringProperty(prefix+PROP_ENCODINGUSE,Constants.ENCODING_USES,false);
        //TODO: get params
        params=new HashMap();
    }

    public void reload() throws ConfigException {
        init();
    }
    
    public String getName() {
        return name;
    }
    
    public String getInterfaceName() {
        return itfName;
    }
    
    public String getImplementationName() {
        return implName;
    }
    
    public void setContextName(String ctxName) {
        this.ctxName=ctxName;
    }
    
    public String getContextName() {
        return ctxName;
    }
    
    public void setSessionType(String sessType) {
        this.sessType=sessType;
    }
    
    public String getSessionType() {
        return sessType;
    }
    
    public String getEncodingStyle() {
        return encStyle;
    }
    
    public String getEncodingUse() {
        return encUse;
    }
    
    public Iterator getParameterNames() {
        return params.keySet().iterator();
    }
    
    public String getParameter(String name) {
        return (String)params.get(name);
    }

    protected Properties getProperties() {
        return props.getProperties("webservice\\."+getName()+"\\..*");
    }
    
    protected String getPropertiesDescriptor() {
        return "webservice properties";
    }
    
}
