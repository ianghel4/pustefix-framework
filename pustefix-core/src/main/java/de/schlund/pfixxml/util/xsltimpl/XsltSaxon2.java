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

package de.schlund.pfixxml.util.xsltimpl;

import java.io.StringReader;
import java.io.Writer;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XsltMessageWriter;
import de.schlund.pfixxml.util.XsltSupport;
import net.sf.saxon.Configuration;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.TransformerFactoryImpl;

/**
 * @author mleidig@schlund.de
 */
public class XsltSaxon2 implements XsltSupport {

    private static Configuration config=new Configuration();
    
    private TransformerFactory ifactory = new TransformerFactoryImpl(config);
    
    //TODO: replace by XSLT 2.0 version
    // pretty-print script by M. Kay, see 
    // http://www.cafeconleche.org/books/xmljava/chapters/ch17s02.html#d0e32721
    private static final String ID = "<?xml version='1.0'?>"
        + "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.1'>"
        + "  <xsl:output method='xml' indent='yes'/>"
        + "  <xsl:strip-space elements='*'/>"
        + "  <xsl:template match='/'>" + "    <xsl:copy-of select='.'/>"
        + "  </xsl:template>" + "</xsl:stylesheet>";
    
    private static Templates PP_XSLT;
    
    static {
        Source src = new SAXSource(Xml.createXMLReader(), new InputSource(new StringReader(ID)));
        TransformerFactory factory = new TransformerFactoryImpl(config);
        try {
            PP_XSLT = factory.newTemplates(src);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
    
    public TransformerFactory getSharedTransformerFactory() {
        return ifactory;
    }
    
    public TransformerFactory getThreadTransformerFactory() {
        return new TransformerFactoryImpl(config);
    }
    
    public Templates getPrettyPrinterTemplates() {
        return PP_XSLT;
    }
 
    public boolean isInternalTemplate(Templates templates) {
        return templates instanceof PreparedStylesheet;
    }
    
    public void doTracing(Transformer transformer, Writer traceWriter) {
        //TODO: implementation
    }
    
    public void doPerformanceTracing(Transformer transformer, Templates templates) {
        //TODO: implementation
    }
    
    public String getSystemId(Templates templates) {
    	//TODO: implementation
    	return null;
    }
    
    @Override
    public XsltMessageWriter recordMessages(Transformer transformer) {
    	//TODO: implementation
    	return null;
    }
    
    @Override
    public void doErrorListening(Transformer transformer, boolean traceLocation) {
        transformer.setErrorListener(new ErrorListenerBase());
    }
    
}
