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

package de.schlund.pfixcore.editor2.core.spring.internal;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.transform.TransformerException;

import org.pustefixframework.editor.common.dom.AbstractIncludeFile;
import org.pustefixframework.editor.common.dom.IncludePart;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixcore.util.ModuleDescriptor;
import de.schlund.pfixcore.util.ModuleInfo;
import de.schlund.pfixxml.resources.ModuleResource;
import de.schlund.pfixxml.resources.ModuleSourceResource;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;

/**
 * Class implementing functions common to IncludeFiles and DynIncludeFiles
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class CommonIncludeFileImpl extends AbstractIncludeFile {
    
    private String path;

    private HashMap<String, IncludePart> cache;

    private FileSystemService filesystem;

    private PathResolverService pathresolver;

    private long lastModTime;

    private Document xmlCache;

    private long currentSerial = 0;
    
    //private Boolean readOnly;

    protected abstract IncludePart createIncludePartInstance(String name,
            Element el, long serial);

    public CommonIncludeFileImpl(FileSystemService filesystem,
            PathResolverService pathresolver, String path) {
        this.filesystem = filesystem;
        this.pathresolver = pathresolver;
        this.path = path;
        this.cache = new HashMap<String, IncludePart>();
        this.lastModTime = 0;
        this.xmlCache = null;
    }

    protected FileSystemService getFileSystemService() {
        return this.filesystem;
    }

    protected PathResolverService getPathResolverService() {
        return this.pathresolver;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludeFile#getPath()
     */
    public String getPath() {
        return this.path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludeFile#getPart(java.lang.String)
     */
    public IncludePart getPart(String name) {
        synchronized (cache) {
            if (this.cache.containsKey(name)) {
                return (IncludePart) this.cache.get(name);
            } else {
                Node xml = this.getContentXML();
                if (xml == null) {
                    return null;
                }
                Element el = null;
                try {
                    el = (Element) XPath.selectNode(xml, "part[@name='" + name
                            + "']");
                } catch (TransformerException e) {
                    // Should NEVER happen
                    LoggerFactory.getLogger(this.getClass()).error("XPath error!", e);
                    return null;
                }
                IncludePart incPart = createIncludePartInstance(name, el, this
                        .getSerial());
                this.cache.put(name, incPart);
                return incPart;
            }
        }
    }

    public Document getContentXML() {
        return this.getContentXML(false);
    }

    public Document getContentXML(boolean forceUpdate) {
        
        File xmlFile = null;
        
        if(getPath().startsWith("module://")) {
            Resource res = ResourceUtil.getResource(getPath());
            if(res instanceof ModuleSourceResource) {
                xmlFile = ((ModuleSourceResource)res).getFile();
            } else {
                if(xmlCache == null) {
                    try {
                        xmlCache = Xml.parseMutable((ModuleResource)res);
                    } catch(SAXException x) {
                        LoggerFactory.getLogger(this.getClass()).warn(x.getMessage(), x);
                        return null;
                    } catch(IOException x) {
                        LoggerFactory.getLogger(this.getClass()).warn(x.getMessage(), x);
                        return null;
                    }
                }
                return xmlCache;
            }
        }

        if(xmlFile == null) xmlFile = this.pathresolver.resolve(this.getPath());
        
        if (!forceUpdate && this.xmlCache != null) {
            synchronized (this.xmlCache) {
                if (xmlFile.lastModified() <= this.lastModTime) {
                    return this.xmlCache;
                }
            }
        }
        synchronized (this.filesystem.getLock(xmlFile)) {
            try {
                synchronized (this.cache) {
                    this.xmlCache = this.filesystem
                            .readXMLDocumentFromFile(xmlFile);
                    this.lastModTime = xmlFile.lastModified();
                    this.currentSerial++;
                    return this.xmlCache;
                }
            } catch (SAXException e) {
                LoggerFactory.getLogger(this.getClass()).warn(e.getMessage(), e);
                return null;
            } catch (IOException e) {
                LoggerFactory.getLogger(this.getClass()).warn(e.getMessage(), e);
                return null;
            }
        }
    }

    public IncludePart createPart(String name) {
        synchronized (cache) {
            if (!this.cache.containsKey(name)) {
                IncludePart incPart = createIncludePartInstance(name, null, -1);
                this.cache.put(name, incPart);
            }
            return (IncludePart) this.cache.get(name);
        }
    }

    public boolean hasPart(String name) {
        return (this.getPart(name) == null);
    }

    public Collection<IncludePart> getParts() {
        // Make sure all physically existing parts are in cache
        synchronized (this.cache) {
            Document xmlDoc = this.getContentXML();
            Node xml = null;
            if (xmlDoc != null) {
                xml = xmlDoc.getDocumentElement();
            }
            if (xml != null) {
                NodeList nlist = xml.getChildNodes();
                for (int i = 0; i < nlist.getLength(); i++) {
                    Node n = nlist.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        Element el = (Element) n;
                        if (el.getNodeName().equals("part")
                                && el.hasAttribute("name")) {
                            String partName = el.getAttribute("name");
                            if (!this.cache.containsKey(partName)) {
                                IncludePart incPart = createIncludePartInstance(
                                        partName, el, this.getSerial());
                                this.cache.put(partName, incPart);
                            }
                        }
                    }
                }
            }

            // Now use cache to return physical and virtual parts
            // Synchronize and copy the values to make sure we return a
            // static version to iterate over.
            HashSet<IncludePart> temp = new HashSet<IncludePart>(this.cache.values());
            return temp;
        }
    }

    public long getSerial() {
        synchronized (this.cache) {
            // Check for changes on filesystem
            this.getContentXML();
            
            return this.currentSerial;
        }
    }
    
    public boolean isReadOnly() {
        Boolean readOnly = null;
        if(readOnly == null) {
            Resource res = ResourceUtil.getResource(getPath());
            if(res instanceof ModuleResource) {
                if(res.getClass() == ModuleSourceResource.class) {
                    String module = ((ModuleSourceResource)res).toURI().getAuthority();
                    ModuleDescriptor desc = ModuleInfo.getInstance().getModuleDescriptor(module);
                    if(desc != null && desc.isContentEditable()) {
                        readOnly = false;
                    } else {
                        readOnly = true;
                    }
                } else {
                    readOnly = true;
                }
            } else {
                readOnly = false;
            }
        }
        return readOnly;
    }

}
