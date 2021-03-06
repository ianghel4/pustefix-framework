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

package org.pustefixframework.editor.webui.remote.dom;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.pustefixframework.editor.common.dom.AbstractTarget;
import org.pustefixframework.editor.common.dom.Image;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.common.dom.Target;
import org.pustefixframework.editor.common.dom.TargetType;
import org.pustefixframework.editor.common.dom.Theme;
import org.pustefixframework.editor.common.dom.ThemeList;
import org.pustefixframework.editor.common.exception.EditorIOException;
import org.pustefixframework.editor.common.exception.EditorParsingException;
import org.pustefixframework.editor.common.remote.transferobjects.IncludePartThemeVariantReferenceTO;
import org.pustefixframework.editor.common.remote.transferobjects.TargetTO;
import org.pustefixframework.editor.common.util.XMLSerializer;
import org.pustefixframework.editor.webui.remote.dom.util.RemoteServiceUtil;
import org.w3c.dom.Document;



public class TargetImpl extends AbstractTarget {
    
    private RemoteServiceUtil remoteServiceUtil;
    private String name;
    private boolean auxDepTarget;

    public TargetImpl(RemoteServiceUtil remoteServiceUtil, String name) {
        this(remoteServiceUtil, name, false);
    }
    
    public TargetImpl(RemoteServiceUtil remoteServiceUtil, String name, boolean auxDepTarget) {
        this.remoteServiceUtil = remoteServiceUtil;
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        this.name = name;
        this.auxDepTarget = auxDepTarget;
    }

    public Collection<Page> getAffectedPages() {
        TargetTO targetTO = getTargetTO();
        LinkedList<Page> pages = new LinkedList<Page>();
        for (String pageName : targetTO.pages) {
            pages.add(new PageImpl(remoteServiceUtil, pageName));
        }
        return pages;
    }
    
    public Collection<Target> getAuxDependencies() {
        TargetTO targetTO = getTargetTO();
        LinkedList<Target> targets = new LinkedList<Target>();
        for (String targetName : targetTO.auxDependencies) {
            if (targetName.startsWith("::aux:")) {
                targetName = targetName.substring(6);
                targets.add(new TargetImpl(remoteServiceUtil, targetName, true));
            } else {
                targets.add(new TargetImpl(remoteServiceUtil, targetName));
            }
        }
        return targets;
    }
    
    public Document getContentXML() throws EditorIOException, EditorParsingException {
        String xml = remoteServiceUtil.getRemoteTargetService().getTargetXML(name);
        if (xml == null) {
            return null;
        } else {
            return (Document) (new XMLSerializer()).deserializeNode(xml);
        }
    }
    
    public Collection<Image> getImageDependencies(boolean recursive) throws EditorParsingException {
        LinkedList<Image> images = new LinkedList<Image>();
        for (String imagePath : remoteServiceUtil.getRemoteTargetService().getImageDependencies(name, recursive)) {
            images.add(new ImageImpl(remoteServiceUtil, imagePath));
        }
        return images;
    }
    
    public Collection<IncludePartThemeVariant> getIncludeDependencies(boolean recursive) throws EditorParsingException {
        LinkedList<IncludePartThemeVariant> includes = new LinkedList<IncludePartThemeVariant>();
        for (IncludePartThemeVariantReferenceTO reference : remoteServiceUtil.getRemoteTargetService().getIncludeDependencies(name, recursive)) {
            includes.add(new IncludePartThemeVariantImpl(remoteServiceUtil, reference.path, reference.part, reference.theme));
        }
        return includes;
    }
    
    public String getName() {
        return name;
    }
    
    public Map<String, Object> getParameters() {
        TargetTO targetTO = getTargetTO();
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(targetTO.parameters);
        return map;
    }
    
    public Target getParentXML() {
        TargetTO targetTO = getTargetTO();
        if (targetTO.parentXML == null) {
            return null;
        }
        return new TargetImpl(remoteServiceUtil, targetTO.parentXML);
    }
    
    public Target getParentXSL() {
        TargetTO targetTO = getTargetTO();
        if (targetTO.parentXSL == null) {
            return null;
        }
        return new TargetImpl(remoteServiceUtil, targetTO.parentXSL);
    }
    
    public Project getProject() {
        return new ProjectImpl(remoteServiceUtil);
    }
    
    public ThemeList getThemeList() {
        TargetTO targetTO = getTargetTO();
        LinkedList<Theme> themes = new LinkedList<Theme>();
        for (String themeName : targetTO.themes) {
            themes.add(new ThemeImpl(themeName));
        }
        return new ThemeListImpl(themes);
    }
    
    public TargetType getType() {
        TargetTO targetTO = getTargetTO();
        return targetTO.type;
    }
    
    private TargetTO getTargetTO() {
        return remoteServiceUtil.getRemoteTargetService().getTarget(getName(), auxDepTarget);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof TargetImpl) {
            TargetImpl t = (TargetImpl) obj;
            return this.remoteServiceUtil.equals(t.remoteServiceUtil);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ("TARGET: " + super.hashCode() + remoteServiceUtil.hashCode()).hashCode();
    }
}
