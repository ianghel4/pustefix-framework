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

package de.schlund.pfixxml.serverutil;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pustefixframework.http.AbstractPustefixRequestHandler;

import de.schlund.pfixxml.PfixServletRequest;

public class SessionHelper {

    private final static Logger LOG = LoggerFactory.getLogger(SessionHelper.class);

    private static final String ENC_STR = "jsessionid";
    
    public static void saveSessionData(Map<String, Object> store, HttpSession session) {
        try {
            Enumeration<?> enm = session.getAttributeNames();
            while (enm.hasMoreElements()) {
                String valName = (String) enm.nextElement();
                store.put(valName, session.getAttribute(valName));
            }
        } catch (NullPointerException e) {
            LOG.warn("Caught NP-Exception: " + e.getMessage());
        }
    }

    public static void copySessionData(Map<String, Object> store, HttpSession session) {
        try {
            Iterator<String> iter = store.keySet().iterator();
            String key = null;
            Object value = null;
            while (iter.hasNext()) {
                key = iter.next();
                value = store.get(key);
                if (value instanceof NoCopySessionData) {
                    LOG.debug("*** Will not copy a object implementing NoCopySessionData!!! ***");
                } else if (!key.equals(SessionAdmin.LISTENER)) {
                    session.setAttribute(key, value);
                }
            }
        } catch (NullPointerException e) {
            LOG.warn("Caught NP-Exception: " + e.getMessage());
        }
    }

    public static String getClearedURI(PfixServletRequest req) {
        StringBuffer rcBuf = new StringBuffer();
        stripUriSessionId(null, req.getRequestURI(), rcBuf);
        return rcBuf.toString();
    }

    public static String getClearedURI(HttpServletRequest req) {
        StringBuffer rcBuf = new StringBuffer();
        stripUriSessionId(null, req.getRequestURI(), rcBuf);
        return rcBuf.toString();
    }

    public static String getClearedURL(String scheme, String host, HttpServletRequest req) {
        return getClearedURL(scheme, host, req, null);
    }
    
    public static String getClearedURL(String scheme, String host, HttpServletRequest req, Properties props) {
        if (scheme == null)
            scheme = req.getScheme();
        if (host == null)
            host = req.getServerName();
        StringBuffer rcBuf = createPrefix(scheme, host, req, props);
        stripUriSessionId(null, req.getRequestURI(), rcBuf);

        String query = req.getQueryString();
        if (query != null && 0 < query.length()) {
            rcBuf.append('?').append(query);
        }
        return rcBuf.toString();
    }

    public static String encodeURI(HttpServletRequest req) {
        StringBuffer rcBuf = new StringBuffer();

        String oldSessionId = stripUriSessionId(null, req.getRequestURI(), rcBuf);

        HttpSession session = req.getSession(false);
        if (session != null) {
            if(session.getAttribute(AbstractPustefixRequestHandler.SESSION_ATTR_COOKIE_SESSION) == null) {
                rcBuf.append(';').append(ENC_STR).append('=').append(session.getId());
            }
        } else if (oldSessionId != null && 0 < oldSessionId.length()) {
            rcBuf.append(';').append(ENC_STR).append('=').append(oldSessionId);
        }
        
        return rcBuf.toString();
    }
    
    /**
     * 
     * @param req
     * @return session id path extension or empty string if session comes from cookie
     */
    public static String getSessionIdPath(HttpServletRequest req) {
        String sessionIdPath = "";
        HttpSession session = req.getSession(false);
        if (session != null) {
            if(session.getAttribute(AbstractPustefixRequestHandler.SESSION_ATTR_COOKIE_SESSION) == null) {
                sessionIdPath = ";" + ENC_STR + "=" + session.getId();
            }
        }
        return sessionIdPath;
    }

    public static String encodeURL(String scheme, String host, HttpServletRequest req) {
        return encodeURL(scheme, host, req, null, null);
    }
    
    public static String encodeURL(String scheme, String host, HttpServletRequest req, Properties props) {
        return encodeURL(scheme, host, req, null, props);
    }
    
    public static String encodeURL(String scheme, String host, HttpServletRequest req, String sessid) {
        return encodeURL(scheme, host, req, sessid, null);
    }
    
    public static String encodeURL(String scheme, String host, HttpServletRequest req, String sessid, Properties props) {
        if (scheme == null)
            scheme = req.getScheme();
        if (host == null)
            host = req.getServerName();
        StringBuffer rcBuf = createPrefix(scheme, host, req, props);
        String oldSessionId = stripUriSessionId(null, req.getRequestURI(), rcBuf);
        HttpSession session = req.getSession(false);

        if (sessid != null) {
            rcBuf.append(';').append(ENC_STR).append('=').append(sessid);
        } else if (session != null) {
            rcBuf.append(';').append(ENC_STR).append('=').append(session.getId());
        } else if (oldSessionId != null && 0 < oldSessionId.length()) {
            rcBuf.append(';').append(ENC_STR).append('=').append(oldSessionId);
        }

        String query = req.getQueryString();
        if (query != null && 0 < query.length()) {
            rcBuf.append('?').append(query);
        }
        return rcBuf.toString();
    }

    private static StringBuffer createPrefix(String scheme, String host, HttpServletRequest req, Properties props) {
        StringBuffer rcBuf;

        rcBuf = new StringBuffer();
        rcBuf.append(scheme).append("://").append(host);
        if (!AbstractPustefixRequestHandler.isDefault(req.getScheme(), req.getServerPort())) {
            // we are using non-default ports and are redirecting to ssl:
            // try to get the right ssl port from the configuration
            if ("https".equals(scheme) && !req.isSecure()) {
                if (props != null) {
                    int redirectPort = AbstractPustefixRequestHandler.getSSLRedirectPort(req.getServerPort(), props);
                    if(redirectPort != 443) rcBuf.append(":" + redirectPort);
                }
            } else if("http".equals(scheme) && req.isSecure()) {
                if (props != null) {
                    int redirectPort = AbstractPustefixRequestHandler.getNonSSLRedirectPort(req.getServerPort(), props);
                    if(redirectPort != 80) rcBuf.append(":" + redirectPort);
                }
            } else {
                rcBuf.append(":").append(req.getServerPort());
            }
        }
        return rcBuf;
    }
    
    protected static String stripUriSessionId(String oldSessionId, String uri, StringBuffer rcUri) {
        String rc = oldSessionId;
        try {
            int semiIdx = uri.indexOf(";jsessionid");
            if (0 <= semiIdx) {
                rc = uri.substring(semiIdx + 1);
                uri = uri.substring(0, semiIdx);
            }

            if (uri.startsWith("/jsessionid")) {
                int nextSlash = uri.indexOf('/', 1);
                if (0 < nextSlash) {
                    rc = uri.substring(1, nextSlash);
                    uri = uri.substring(nextSlash);
                } else {
                    rc = uri.substring(1);
                }
            }

            if (uri.length() == 0)
                uri = "/";

            rcUri.append(uri);
        } catch (NullPointerException e) {
            LOG.warn("Caught NP-Exception: " + e.getMessage());
        }
        return rc;
    }

}
