package de.schlund.pfixxml.testenv;

import com.icl.saxon.Controller;
import com.icl.saxon.TransformerFactoryImpl;
import com.icl.saxon.expr.NodeSetValue;
import com.icl.saxon.om.NodeEnumeration;

import com.sun.net.ssl.KeyManager;
import com.sun.net.ssl.SSLContext;
import com.sun.net.ssl.TrustManager;
import com.sun.net.ssl.X509TrustManager;
import com.sun.net.ssl.internal.ssl.SSLServerSocketFactoryImpl;
import com.sun.net.ssl.internal.ssl.SSLSocketFactoryImpl;

import de.schlund.pfixxml.targets.TraxXSLTProcessor;
import de.schlund.pfixxml.xpath.PFXPathEvaluator;

import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.X509Certificate;

import java.util.ArrayList;
import java.util.Arrays;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpRecoverableException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import org.apache.xpath.XPathAPI;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * TestClient is an application for testing business logic on
 * pustefix-based projects.
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class TestClient {

    //~ Instance/static variables ..................................................................

    private static final String XMLONLY_PARAM_KEY   = "__xmlonly";
    private static final String XMLONLY_PARAM_VALUE = "1";
    private static final int    LOOP_COUNT          = 1;
    private static final String GNU_DIFF            = "diff -u";
    private String              srcDir;
    private String              tmpDir;
    private String              styleDir;
    private HttpConnection      httpConnect;
    // Xerces
    private DocumentBuilderFactoryImpl  doc_factory;
    private long                        request_count = 0;
    private String                      sessionId;
    private boolean                     modeQuiet     = false;
    private boolean                     modeVerbose   = false;
    private boolean                     ssl           = false;
    private HostConfiguration           currentConfig = new HostConfiguration();
    private String                      uri_session   = new String();
    private SimpleHttpConnectionManager conMan        = new SimpleHttpConnectionManager();

    //~ Constructors ...............................................................................

    public TestClient() throws TestClientException {
        doc_factory = (DocumentBuilderFactoryImpl) DocumentBuilderFactoryImpl.newInstance();
        doc_factory.setValidating(false);
        doc_factory.setNamespaceAware(true);
    }

    //~ Methods ....................................................................................

    public static void main(String[] args) {
        System.out.println("|====================================================|");
        System.out.println("|               Pustefix Test TextClient             |");
        System.out.println("|====================================================|");
        TestClient tc = null;
        try {
            tc = new TestClient();
            if (! tc.scanOptions(args)) {
                tc.printUsage();
                return;
            }
            tc.checkOptions();
            ArrayList files = tc.readFiles();
            StepConfig[] config = tc.doPrepare(files);
            System.out.println("\n Starting test NOW!\n");
            for (int i = 0; i < LOOP_COUNT; i++) {
                tc.doTest(config);
            }
        } catch (TestClientException e) {
            System.out.println("\n**********************************************");
            System.out.println("ERROR in TestClient");
            System.out.println("Exception:");
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("Nested Exception:");
            System.out.println(e.getCause().getMessage());
            e.getCause().printStackTrace();
            System.out.println("\n**********************************************");
        }
    }

    private void doTest(StepConfig[] config) throws TestClientException {
        boolean has_diff = false;
        //boolean redo = false;
        for (int j = 0; j < config.length; j++) {
            //System.out.println("j ist:"+j);
            if (modeQuiet) {
            } else if (modeVerbose) {
                StringBuffer sb = new StringBuffer();
                sb.append("________________________________________________________________\n");
                sb.append("Step ").append(j).append("\n");
                sb.append("  File=").append(config[j].getFileName()).append("\n");
                System.out.println(sb.toString());
            } else {
                System.out.println("\nDoing step " + j);
            }
            Document current_output_tree = null;
            try {
                current_output_tree = getResultFromFormInput(config[j].getRecordedInput());
            } catch (TestClientException e) {
                if (e.getCause() instanceof HttpRecoverableException) {
                    System.out.println("Uuuups...skipping...");
                    break;
                } else {
                    throw e;
                }
            }
            if (modeQuiet) {
            } else if (modeVerbose) {
                System.out.println("  Transforming recorded and current output document...");
            } else {
                System.out.println("  Transforming...");
            }
            Document tmp_rec       = doTransform(config[j].getRecordedOutput(), 
                                                 config[j].getStyleSheet());
            Document tmp_out       = doTransform(current_output_tree, config[j].getStyleSheet());
            String   tmp_fname_cur = tmpDir + "/_current" + j;
            String   tmp_fname_rec = tmpDir + "/_recorded" + j;
            writeDocument(tmp_out, tmp_fname_cur);
            writeDocument(tmp_rec, tmp_fname_rec);
            if (modeQuiet) {
            } else if (modeVerbose) {
                System.out.println("  Diffing " + tmp_fname_cur + " and " + tmp_fname_rec + " ...");
            } else {
                System.out.println("  Diffing...");
            }
            String msg = doDiff(tmp_fname_cur, tmp_fname_rec);
            if (modeQuiet) {
            } else if (modeVerbose) {
                System.out.print("  Output:");
            } else {
                System.out.print("  Output:");
            }
            if (msg == null || msg.equals("")) {
                msg = ":-)";
            } else {
                has_diff = true;
            }
            System.out.println("  " + msg);
        }
        System.out.print("\n*** Resut: ***");
        System.out.println(has_diff ? ";-(" : ";-)");
    }

    private StepConfig[] doPrepare(ArrayList files) throws TestClientException {
        DocumentBuilder doc_builder;
        StepConfig[]    config = new StepConfig[files.size()];
        boolean ssl_initialized = false;
        try {
            doc_builder = doc_factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new TestClientException("Could not get a DocumentBuilder!", e);
        }
        System.out.println("Analyzing files...");
        for (int i = 0; i < files.size(); i++) {
            File file = (File) files.get(i);
            if (modeQuiet) {
            } else if (modeVerbose) {
                System.out.println("  " + file.getName());
            } else {
            }
            Document file_content = null;
            try {
                file_content = doc_builder.parse(file);
            } catch (SAXException e) {
                if (e instanceof SAXParseException) {
                    SAXParseException saxex = (SAXParseException) e;
                    throw new TestClientException("SAXException occured at line "
                                                  + saxex.getLineNumber() + "in file "
                                                  + file.getName(), e);
                } else {
                    throw new TestClientException("SAXException occured", e);
                }
            }
             catch (IOException e) {
                throw new TestClientException("IOException occured", e);
            }
            Document recorded_out = new DocumentImpl();
            Document recorded_in = new DocumentImpl();
            Node     result_out  = null;
            try {
                result_out = XPathAPI.selectSingleNode(file_content, "/step/formresult");
            } catch (TransformerException e) {
                throw new TestClientException("TransformerException occured!", e);
            }
            Node result_in = null;
            try {
                result_in = XPathAPI.selectSingleNode(file_content, "/step/request");
            } catch (TransformerException e) {
                throw new TestClientException("TransformerException occured!", e);
            }
            String stylessheet = null;
            try {
                stylessheet = XPathAPI.selectSingleNode(file_content, "/step/stylesheet").getFirstChild()
                        .getNodeValue();
            } catch (TransformerException e) {
                throw new TestClientException("TransformerException occured!", e);
            }
            Node n_out = recorded_out.importNode(result_out, true);
            Node n_in = recorded_in.importNode(result_in, true);
            recorded_out.appendChild(n_out);
            recorded_in.appendChild(n_in);
            StepConfig conf = new StepConfig(((File) files.get(i)).getName(), recorded_in, 
                                             recorded_out, stylessheet);
            config[i] = conf;
            
            // check, if we must init SSL
            String proto = null;
            try {
                proto = XPathAPI.selectSingleNode(recorded_in, "/request/proto").getFirstChild().getNodeValue();
            } catch (TransformerException e) {
            	throw new TestClientException("TransformerException occured!", e);
            }
            if(proto.toUpperCase().equals("https".toUpperCase()) && !ssl_initialized) {
            	System.out.println("https detected!");
            	System.out.print("Initializing SSL...");
            	initSSL();
            	System.out.println("Done");
            	ssl_initialized = true;
            }
        }
        System.out.println("Done");
        return config;
    }

    private Document doTransform(Document in, String stylesheet_name) throws TestClientException {
        // saxon
        TransformerFactoryImpl trans_fac     = (TransformerFactoryImpl) TransformerFactory.newInstance();
        String                 path          = styleDir + "/" + stylesheet_name;
        StreamSource           stream_source = new StreamSource("file://" + path);
        Templates              templates     = null;
        try {
            templates = trans_fac.newTemplates(stream_source);
        } catch (TransformerConfigurationException e) {
            throw new TestClientException("TransformerConfigurationException occured!", e);
        }
        Transformer trafo = null;
        try {
            trafo = templates.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new TestClientException("TransformerConfigurationException occured!", e);
        }
        DOMSource dom_source = new DOMSource(in);
        DOMResult dom_result = new DOMResult();
        try {
            trafo.transform(dom_source, dom_result);
        } catch (TransformerException e) {
            throw new TestClientException("TransformerException occured!", e);
        }
        return (Document) dom_result.getNode();
    }

    private void writeDocument(Document doc, String path) throws TestClientException {
        XMLSerializer ser        = new XMLSerializer();
        OutputFormat  out_format = new OutputFormat("xml", "ISO-8859-1", true);
        out_format.setIndent(2);
        out_format.setPreserveSpace(false);
        ser.setOutputFormat(out_format);
        FileWriter file_writer = null;
        try {
            file_writer = new FileWriter(path);
        } catch (IOException e) {
            throw new TestClientException("IOException occured!", e);
        }
        ser.setOutputCharStream(file_writer);
        try {
            ser.serialize(doc);
        } catch (IOException e) {
            throw new TestClientException("IOException ocuured!", e);
        }
    }

    private String documentToString(Document doc) throws TestClientException {
        XMLSerializer ser        = new XMLSerializer();
        OutputFormat  out_format = new OutputFormat("xml", "ISO-8859-1", true);
        out_format.setIndent(2);
        out_format.setPreserveSpace(false);
        ser.setOutputFormat(out_format);
        StringWriter string_writer = new StringWriter();
        ser.setOutputCharStream(string_writer);
        try {
            ser.serialize(doc);
        } catch (IOException e) {
            throw new TestClientException("IOExcpetion during serialization!", e);
        }
        return string_writer.getBuffer().toString();
    }

    private String doDiff(String path1, String path2) throws TestClientException {
        String diff = GNU_DIFF + " " + path2 + " " + path1;
        //System.out.println(" Doing diff :"+diff);
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(diff);
        } catch (IOException e) {
            throw new TestClientException("IOException occured!", e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String         s;
        StringBuffer   buf = new StringBuffer();
        try {
            while ((s = reader.readLine()) != null) {
                buf.append(s).append("\n");
            }
        } catch (IOException e) {
            throw new TestClientException("IOException occured!", e);
        }
        //System.out.println("=============>>====>>>"+buf.toString());
        try {
            reader.close();
        } catch (IOException e) {
            throw new TestClientException("IOException occured!", e);
        }
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new TestClientException("InterruptedException occured!", e);
        }
        return buf.toString();
    }

    private ArrayList readFiles() throws TestClientException {
        File      dir       = new File(srcDir);
        File[]    all_files = dir.listFiles();
        ArrayList files     = new ArrayList();
        for (int i = 0; i < all_files.length; i++) {
            if (all_files[i].canRead() && all_files[i].isFile()) {
                files.add(all_files[i]);
            }
        }
        Arrays.sort(files.toArray());
        if (modeQuiet) {
        } else if (modeVerbose) {
            System.out.println("Reading files...");
            for (int i = 0; i < files.size(); i++) {
                System.out.println("  " + ((File) files.get(i)).getName());
            }
        } else {
            System.out.println("Reading files...");
        }
        if(!modeQuiet) {
	        System.out.println("Done");
        }
        return files;
    }

    private boolean scanOptions(String[] args) {
        Getopt getopt = new Getopt("TestClient", args, "d:t:s:qv");
        int    c = 0;
        while ((c = getopt.getopt()) != -1) {
            switch (c) {
                case 'd':
                    srcDir = getopt.getOptarg();
                    break;
                case 't':
                    tmpDir = getopt.getOptarg();
                    break;
                case 's':
                    styleDir = getopt.getOptarg();
                    break;
                case 'q':
                    modeQuiet   = true;
                    modeVerbose = false;
                    break;
                case 'v':
                    modeVerbose = true;
                    modeQuiet   = false;
                    break;
                default:
            }
        }
        if (srcDir == null || srcDir.equals("") || tmpDir == null || tmpDir.equals("")
            || styleDir == null || styleDir.equals("")) {
            return false;
        } else {
            return true;
        }
    }

    private void checkOptions() throws TestClientException {
        System.out.print("Checking options...");
        File input_dir = new File(srcDir);
        if (! input_dir.isDirectory() || ! input_dir.canRead()) {
            throw new TestClientException(srcDir + " is not a directory or not readable!", null);
        }
        File tmp_dir = new File(tmpDir);
        if (! tmp_dir.isDirectory() || ! tmp_dir.canRead()) {
            throw new TestClientException(tmpDir + " is not a directory or not readable!", null);
        }
        File style_dir = new File(styleDir);
        if (! style_dir.isDirectory() || ! style_dir.canRead()) {
            throw new TestClientException(styleDir + " is not a directory or not readable!", null);
        }
        System.out.println("Ok");
    }

    private void printUsage() {
        System.out.println(
                "TestClient -d [recorded dir] -t [temporary dir] -s [stylesheet dir] -q -v");
    }

    private void initHttpConnection(String hostname, int port, String proto)
                             throws TestClientException {
        HostConfiguration config = new HostConfiguration();
        config.setHost(hostname, port, proto);
        if (httpConnect != null && config.hostEquals(httpConnect)) {
            return;
        } else {
        	StringBuffer sb = new StringBuffer(255);
            currentConfig = config;
            sessionId     = null;
            sb.append("----------------------------------------------\n");
            if (httpConnect == null) {
                sb.append("No HTTP Connection. Establishing new connection.\n");
            } else if (! config.hostEquals(httpConnect)) {
                sb.append("HostConfiguration has changed. Establishing new connection.\n");
            }
            sb.append("  Host=").append(currentConfig.getHost()).append("\n");
            sb.append("  Port=").append(currentConfig.getPort()).append("\n");
            sb.append(" Proto=").append(currentConfig.getProtocol().toString()).append("\n");
            if(httpConnect != null && httpConnect.isOpen()) {
            	httpConnect.close();
            	conMan.releaseConnection(httpConnect);
            }
            httpConnect = conMan.getConnection(currentConfig);
            try {
                httpConnect.open();
            } catch (IOException e) {
                throw new TestClientException("Unable to reopen HTTP connection!", e);
            }
            sb.append("   SSL=").append(httpConnect.isSecure()).append("\n");
            sb.append("----------------------------------------------\n");
            if(!modeQuiet) {
	            System.out.println(sb.toString());
            }
        }
    }

    private Document getResultFromFormInput(Document form_data) throws TestClientException {
        String host  = getHostnameFromInput(form_data);
        int    port  = getPortFromInput(form_data);
        String proto = getProtoFromInput(form_data);
        initHttpConnection(host, port, proto);
        //       return true;
        String          uri         = getURIFromInput(form_data);
        NameValuePair[] post_params = getPostParamsFromInput(form_data);
        PostMethod      post        = null;
        int             status_code = -1;
        uri_session = uri;
        if (sessionId != null) {
            //It not the first request, we already have a session
            uri_session = uri.substring(0, uri.indexOf('=') + 1) + sessionId;
        }
        StringBuffer sb = null;
        if(!modeQuiet) {
        	sb = new StringBuffer(255);
        }
        if (modeQuiet) {
        } else if (modeVerbose) {
            sb.append("  Executing HTTP POST\n");
            sb.append("          URI=").append(uri_session).append("\n");
            sb.append("       Params=\n");
            for (int i = 0; i < post_params.length; i++) {
                sb.append("            ").append(post_params[i].getName()).append("=").append(post_params[i].getValue())
                  .append("\n");
            }
        } else {
            sb.append("  Executing HTTP POST\n");
        }
        post = new PostMethod(uri_session);
        post.setFollowRedirects(true);
        post.addParameters(post_params);
        try {
            status_code = post.execute(new HttpState(), httpConnect);
      	} catch (HttpException e) {
            throw new TestClientException("HTTPException occured!:" + status_code, e);
        } catch (IOException e) {
            throw new TestClientException("IOException occured!", e);
        }
        request_count++;
        if(!modeQuiet) {
	        sb.append("   StatusCode=").append(status_code).append("\n");
        }
        if (sessionId == null) { // it's the first request, follow redirect to get a new session
            try {
                uri = post.getURI().toString();
            } catch (URIException e) {
                throw new TestClientException("URIException occured!", e);
            }
            sessionId   = uri.substring(uri.indexOf('=') + 1, uri.length());
            uri_session = uri.substring(0, uri.indexOf('=') + 1) + sessionId;
        }
        if (status_code != HttpStatus.SC_OK) {
            //String resp = post.getResponseBodyAsString();
            throw new TestClientException("HTTP-Status code =" + status_code + " (Must be 200)! ", 
                                          null);
        }
        InputStream response_stream = null;
        try {
            response_stream = post.getResponseBodyAsStream();
        } catch (IOException e) {
            throw new TestClientException("IOException occured!", e);
        }
        DocumentBuilder doc_builder = null;
        try {
            doc_builder = doc_factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new TestClientException("ParserConfigurationException occured", e);
        }
        Document doc = null;
        try {
            doc = doc_builder.parse(response_stream);
        } catch (SAXException e) {
            throw new TestClientException("SaxException occured", e);
        } catch (IOException e) {
            throw new TestClientException("IOException occured", e);
        }
        if(!modeQuiet) {
        	System.out.println(sb.toString());
        }
        return doc;
    }

    private NameValuePair[] getPostParamsFromInput(Document form_data) throws TestClientException {
        NodeList value = null;
        try {
            value = XPathAPI.selectNodeList(form_data, "/request/params/param");
        } catch (TransformerException e) {
            throw new TestClientException("TransformerException occured!", e);
        }
        NameValuePair[] post_params = new NameValuePair[value.getLength() + 1];
        int             i = 0;
        for (int ii = 0; ii < value.getLength(); ii++) {
            Element e           = (Element) value.item(ii);
            String  param_name  = e.getAttribute("name");
            String  param_value = e.hasChildNodes() ? e.getFirstChild().getNodeValue() : "";
            post_params[i] = new NameValuePair(param_name, param_value);
            i++;
        }
        post_params[i] = new NameValuePair(XMLONLY_PARAM_KEY, XMLONLY_PARAM_VALUE);
        return post_params;
    }

    private String getHostnameFromInput(Document form_data) throws TestClientException {
        Node value = null;
        try {
            value = XPathAPI.selectSingleNode(form_data, "/request/hostname");
        } catch (TransformerException e) {
            throw new TestClientException("TransformerException occured!", e);
        }
        String name = ((Element) value).getFirstChild().getNodeValue();
        return name;
    }

    private int getPortFromInput(Document form_data) throws TestClientException {
        Node value = null;
        try {
            value = XPathAPI.selectSingleNode(form_data, "/request/port");
        } catch (TransformerException e) {
            throw new TestClientException("TransformerException occured!", e);
        }
        String p    = ((Element) value).getFirstChild().getNodeValue();
        int    port = Integer.parseInt(p);
        return port;
    }

    private String getProtoFromInput(Document form_data) throws TestClientException {
        Node value = null;
        try {
            value = XPathAPI.selectSingleNode(form_data, "/request/proto");
        } catch (TransformerException e) {
            throw new TestClientException("TransformerException occured!", e);
        }
        String proto = ((Element) value).getFirstChild().getNodeValue();
        return proto;
    }

    private String getURIFromInput(Document form_data) throws TestClientException {
        Node value = null;
        try {
            value = XPathAPI.selectSingleNode(form_data, "/request/uri");
        } catch (TransformerException e) {
            throw new TestClientException("TransformerException occured!", e);
        }
        String uri = ((Element) value).getFirstChild().getNodeValue();
        return uri;
    }

    private void initSSL() throws TestClientException {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
        X509TrustManager tm         = new MyX509TrustManager();
        KeyManager[]     km         = null;
        TrustManager[]   tma        = {tm};
        SSLContext       sslContext;
        try {
            sslContext = SSLContext.getInstance("SSLv3");
        } catch (NoSuchAlgorithmException e) {
            throw new TestClientException(" Error during SSLInit: No such Algorithm!", e);
        }
        try {
            sslContext.init(km, tma, new java.security.SecureRandom());
        } catch (KeyManagementException e) {
            throw new TestClientException("KeyManagmentException during SSLInit!", e);
        }
        SSLSocketFactory   ssl_fac = sslContext.getSocketFactory();
        MySSLSocketfactory myssl   = new MySSLSocketfactory(ssl_fac);
        Protocol           myHTTPS = new Protocol("https", myssl, 443);
        Protocol.registerProtocol("https", myHTTPS);
    }
}

class StepConfig {

    //~ Instance/static variables ..................................................................

    private Document recordedInput;
    private Document recordedOutput;
    private String   styleSheet;
    private String   fileName;

    //~ Constructors ...............................................................................

    StepConfig(String filename, Document recordIn, Document recordOut, String stylesheet) {
        this.fileName       = filename;
        this.recordedInput  = recordIn;
        this.recordedOutput = recordOut;
        this.styleSheet     = stylesheet;
    }

    //~ Methods ....................................................................................

    /**
     * Returns the recordedInput.
     * @return Document
     */
    public Document getRecordedInput() {
        return recordedInput;
    }

    /**
     * Returns the recordedOutput.
     * @return Document
     */
    public Document getRecordedOutput() {
        return recordedOutput;
    }

    /**
     * Returns the styleSheet.
     * @return String
     */
    public String getStyleSheet() {
        return styleSheet;
    }

    /**
     * Returns the fileName.
     * @return String
     */
    public String getFileName() {
        return fileName;
    }
}

class MyX509TrustManager implements X509TrustManager {

    //~ Methods ....................................................................................

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    public boolean isClientTrusted(X509Certificate[] chain) {
        return true;
    }

    public boolean isServerTrusted(X509Certificate[] chain) {
        return true;
    }
}

class MySSLSocketfactory implements SecureProtocolSocketFactory {

    //~ Instance/static variables ..................................................................

    private SSLSocketFactory sslImpl;

    //~ Constructors ...............................................................................

    public MySSLSocketfactory(SSLSocketFactory ssl) {
        sslImpl = ssl;
    }

    //~ Methods ....................................................................................

    public Socket createSocket(String host, int port) throws IOException {
        return sslImpl.createSocket(host, port);
    }

    public Socket createSocket(String host, int port, InetAddress client_address, int client_port)
                        throws IOException {
        return sslImpl.createSocket(host, port, client_address, client_port);
    }

    public Socket createSocket(Socket socket, String host, int port, boolean close)
                        throws IOException {
        return sslImpl.createSocket(socket, host, port, close);
    }
}