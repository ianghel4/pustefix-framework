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
package de.schlund.pfixxml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ThrowableInformation;
import org.apache.log4j.xml.DOMConfigurator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.pfixxml.config.CustomizationHandler;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.config.XMLPropertiesUtil;
import de.schlund.pfixxml.loader.AppLoader;
import de.schlund.pfixxml.loader.Reloader;
import de.schlund.pfixxml.loader.StateTransfer;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.Misc;
import de.schlund.pfixxml.util.TransformerHandlerAdapter;
import de.schlund.pfixxml.util.logging.ProxyLogUtil;

/**
 * This Servlet is just there to have it's init method called on startup of the
 * VM. It starts all VM-global factories by calling their 'init' method from the
 * {@link FactoryInit} interface. These factories are located by analyzing the
 * "servlet.propfile" parameter which points to a file where all factories are
 * listed.
 */
public class FactoryInitServlet extends HttpServlet implements Reloader {

    // ~ Instance/static variables
    // ..................................................................
    public static String PROP_DOCROOT = "pustefix.docroot";

    private static String PROP_LOG4J = "pustefix.log4j.config";

    private static String PROP_PREFER_CONTAINER_LOGGING = "pustefix.logging.prefercontainer";

    private Object LOCK = new Object();

    private static Category CAT = Category.getInstance(FactoryInitServlet.class.getName());

    private static boolean configured = false;

    private ArrayList factories;

    private static String log4jconfig = null;

    private static long log4jmtime = -1;
    
    private boolean warMode = false;
    private boolean standaloneMode = false;

    // ~ Methods
    // ....................................................................................

    /**
     * Handle the HTTP-Post method.
     * 
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest,
     *      HttpServletResponse)
     * @throws ServletException
     *             on all
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        doGet(req, res);
    }

    /**
     * Handle the HTTP-Get method
     * 
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest,
     *      HttpServletResponse)
     * @throws ServletException
     *             on call
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        throw new ServletException("This servlet can't be called interactively");
    }

    public static void tryReloadLog4j() {
        if (log4jconfig != null) {
            FileResource l4jfile = ResourceUtil.getFileResourceFromDocroot(log4jconfig);
            long tmpmtime = l4jfile.lastModified();
            if (tmpmtime > log4jmtime) {
                CAT.error("\n\n################################\n" + "#### Reloading log4j config ####\n" + "################################\n");
                try {
                    configureLog4j(l4jfile);
                } catch (FileNotFoundException e) {
                    Logger.getLogger(FactoryInitServlet.class).error("Reloading log4j config failed!", e);
                } catch (SAXException e) {
                    Logger.getLogger(FactoryInitServlet.class).error("Reloading log4j config failed!", e);
                } catch (IOException e) {
                    Logger.getLogger(FactoryInitServlet.class).error("Reloading log4j config failed!", e);
                }
                log4jmtime = tmpmtime;
            }
        }
    }

    /**
     * Initialize this servlet. Also call the 'init' method of all classes
     * listed in the configuration. These classes must implement the FactoryInit
     * interface.
     * 
     * @param config
     *            the servlet configuration
     * @see javax.servlet.Servlet#init(ServletConfig)
     * @throws ServletException
     *             on errors
     */
    public void init(ServletConfig Config) throws ServletException {
        super.init(Config);
        Properties properties = new Properties(System.getProperties());
        // old webapps specify docroot -- true webapps don't
        String docrootstr = Config.getInitParameter(PROP_DOCROOT);
        if (docrootstr != null && !docrootstr.equals("")) {
            standaloneMode = true;
        } else {
            docrootstr = Config.getServletContext().getRealPath("/WEB-INF/pfixroot");
            if (docrootstr == null) {
                warMode = true;
            }
        }

        // Setup global configuration before doing anything else
        if (docrootstr != null) {
            GlobalConfigurator.setDocroot(docrootstr);
        }
        if (warMode) {
            GlobalConfigurator.setServletContext(getServletContext());
        }
        
        if (docrootstr != null) {
            // For compatibility with old apps, initialize PathFactory
            PathFactory.getInstance().init(docrootstr);
        }

        String confname = Config.getInitParameter("servlet.propfile");
        if (confname != null) {
            FileResource confFile = ResourceUtil.getFileResourceFromDocroot(confname);
            try {
                XMLPropertiesUtil.loadPropertiesFromXMLFile(confFile, properties);
            } catch (FileNotFoundException e) {
                throw new ServletException("*** [" + confname + "] Not found: " + e.toString());
            } catch (IOException e) {
                throw new ServletException("*** [" + confname + "] IO-error: " + e.toString());
            } catch (SAXException e) {
                throw new ServletException("*** [" + confname + "] Parsing-error: " + e.toString());
            }
        } else {
            throw new ServletException("*** FATAL: Need the servlet.propfile property as init parameter! ***");
        }
        
        if (docrootstr != null) {
            // this is for stuff that can't use the PathFactory. Should not be used
            // when possible...
            properties.setProperty("pustefix.docroot", docrootstr);
        }

        synchronized (LOCK) {
            if (!configured) {
                configureLogging(properties);

                HashMap to_init = PropertiesUtils.selectProperties(properties, "factory.initialize");
                if (to_init != null) {
                    // sort key to initialize the factories in defined order
                    TreeSet keyset = new TreeSet(to_init.keySet());
                    for (Iterator i = keyset.iterator(); i.hasNext();) {
                        String key = (String) i.next();
                        String the_class = (String) to_init.get(key);
                        try {
                            CAT.debug(">>>> Init key: [" + key + "] class: [" + the_class + "] <<<<");
                            AppLoader appLoader = AppLoader.getInstance();
                            long start = 0;
                            long stop = 0;
                            if (appLoader.isEnabled() && appLoader.isReloadableClass(the_class)) {
                                Class clazz = appLoader.loadClass(the_class);
                                Object factory = clazz.getMethod("getInstance", Misc.NO_CLASSES).invoke(null, Misc.NO_OBJECTS);
                                CAT.debug("     Object ID: " + factory);
                                start = System.currentTimeMillis();
                                clazz.getMethod("init", new Class[] { Properties.class }).invoke(factory, new Object[] { properties });
                                stop = System.currentTimeMillis();
                                CAT.debug("Init of " + factory + " took " + (stop - start) + " ms");
                                if (factories == null) {
                                    factories = new ArrayList();
                                }
                                factories.add(factory);
                            } else {
                                Class clazz = Class.forName(the_class);
                                Object factory = clazz.getMethod("getInstance", Misc.NO_CLASSES).invoke(null, Misc.NO_OBJECTS);
                                CAT.debug("     Object ID: " + factory);
                                start = System.currentTimeMillis();
                                clazz.getMethod("init", new Class[] { Properties.class }).invoke(factory, new Object[] { properties });
                                stop = System.currentTimeMillis();
                                CAT.debug("Init of " + factory + " took " + (stop - start) + " ms");
                            }
                        } catch (Exception e) {
                            CAT.error(e.toString());
                            ThrowableInformation info = new ThrowableInformation(e);
                            String[] trace = info.getThrowableStrRep();
                            StringBuffer strerror = new StringBuffer();
                            for (int ii = 0; ii < trace.length; ii++) {
                                strerror.append("->" + trace[ii] + "\n");
                            }
                            CAT.error(strerror.toString());
                            throw new ServletException(e.toString());
                        }
                    }
                }
            }
            configured = true;
            CAT.debug("***** INIT of FactoryInitServlet done *****");

            AppLoader appLoader = AppLoader.getInstance();
            if (appLoader.isEnabled())
                appLoader.addReloader(this);
        }
    }

    private Class classFromContainer(Class clazz) {
        ClassLoader webappLoader = clazz.getClassLoader();
        ClassLoader cl = webappLoader.getParent();
        while (cl != null) {
            Class containerClazz;
            try {
                containerClazz = cl.loadClass(clazz.getName());
            } catch (ClassNotFoundException e) {
                cl = cl.getParent();
                continue;
            }
            if (clazz != containerClazz) {
                return containerClazz;
            }
            cl = cl.getParent();
        }
        return null;
    }

    private void configureLogging(Properties properties) throws ServletException {
        String containerProp = properties.getProperty(PROP_PREFER_CONTAINER_LOGGING);
        if (warMode || (!standaloneMode && (containerProp != null && containerProp.toLowerCase().equals("true")))) {
            ProxyLogUtil.getInstance().configureLog4jProxy();
            ProxyLogUtil.getInstance().setServletContext(getServletContext());
        } else {
            log4jconfig = properties.getProperty(PROP_LOG4J);
            if (log4jconfig == null || log4jconfig.equals("")) {
                throw new ServletException("*** FATAL: Need the pustefix.log4j.config property in factory.xml! ***");
            }
            FileResource l4jfile = ResourceUtil.getFileResourceFromDocroot(log4jconfig);
            try {
                configureLog4j(l4jfile);
            } catch (FileNotFoundException e) {
                throw new ServletException("File for log4j configuration not found!", e);
            } catch (SAXException e) {
                throw new ServletException("Error on parsing log4j configuration file!", e);
            } catch (IOException e) {
                throw new ServletException("Error on reading log4j configuration file!", e);
            }

        }
    }

    private static void configureLog4j(FileResource configFile) throws SAXException, FileNotFoundException, IOException {
        log4jmtime = configFile.lastModified();
        XMLReader xreader = XMLReaderFactory.createXMLReader();
        TransformerFactory tf = SAXTransformerFactory.newInstance();
        if (tf.getFeature(SAXTransformerFactory.FEATURE)) {
            SAXTransformerFactory stf = (SAXTransformerFactory) tf;
            TransformerHandler th;
            try {
                th = stf.newTransformerHandler();
            } catch (TransformerConfigurationException e) {
                throw new RuntimeException("Failed to configure TransformerFactory!", e);
            }
            DOMResult dr = new DOMResult();
            DOMResult dr2 = new DOMResult();
            th.setResult(dr);
            DefaultHandler dh = new TransformerHandlerAdapter(th);
            DefaultHandler cushandler = new CustomizationHandler(dh);
            xreader.setContentHandler(cushandler);
            xreader.setDTDHandler(cushandler);
            xreader.setErrorHandler(cushandler);
            xreader.setEntityResolver(cushandler);
            xreader.parse(new InputSource(configFile.getInputStream()));
            try {
                tf.newTransformer(new StreamSource(ResourceUtil.getFileResourceFromDocroot("core/build/create_log4j_config.xsl").toURL().toString())).transform(new DOMSource(dr.getNode()), dr2);
            } catch (TransformerException e) {
                throw new SAXException(e);
            }
            DOMConfigurator.configure(dr2.getNode().getOwnerDocument().getDocumentElement());
        } else {
            throw new RuntimeException("Could not get instance of SAXTransformerFactory!");
        }
    }

    public static boolean isConfigured() {
        return configured;
    }

    public void reload() {
        if (factories != null) {
            ArrayList newFacs = new ArrayList();
            Iterator it = factories.iterator();
            while (it.hasNext()) {
                Object fac = it.next();
                String className = fac.getClass().getName();
                Object facNew = StateTransfer.getInstance().transfer(fac);
                newFacs.add(facNew);
            }
            factories = newFacs;
        }
    }
}
