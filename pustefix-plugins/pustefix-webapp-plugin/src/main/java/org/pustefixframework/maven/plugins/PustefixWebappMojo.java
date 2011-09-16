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

package org.pustefixframework.maven.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;

/**
 * Generates a Pustefix webapp, but without the parts generated by Maven's war plugin. Executes in the generate-sources phase because 
 * some tests need files generated by this plugin.
 *
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class PustefixWebappMojo extends AbstractMojo {
    
    /**
     * Docroot of the application
     * 
     * @parameter default-value="${project.build.directory}/${project.artifactId}-${project.version}"
     */
    private String docroot;
    
    /**
     * Where to place apt-generated classes.
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/apt"
     */
    private File aptdir;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    private List<Artifact> pluginClasspath;

    
    
    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException {
        File basedir;

        // allow plugin declaration in parent pom
        if ("pom".equals(project.getPackaging())) {
            return;
        }

        // because all executions operate on the same pfixcore classes:
        GlobalConfig.reset();
        
        GlobalConfigurator.setDocroot(docroot);
        new File(docroot, "WEB-INF").mkdirs();
        
        basedir = project.getBasedir();
        new Apt(basedir, aptdir, getLog(), project).execute(getPluginClasspath());
        if(aptdir.exists()) {
            project.addCompileSourceRoot(aptdir.getAbsolutePath());
        }
    }

    private String getPluginClasspath() {
        StringBuilder result;
        
        result = new StringBuilder();
        for (String path : pathStrings(pluginClasspath)) {
            if (result.length() > 0) {
                result.append(':');
            } 
            result.append(path);
        }
        return result.toString();
    }

    public void extractJar(String src, File dest) throws IOException {
         JarFile jar;
         JarEntry entry;
         File file;
         byte[] buffer = new byte[4096];
         
         getLog().info("extracting " + src + " to " + dest);
         jar = new JarFile(src);
         Enumeration<JarEntry> entries = jar.entries();
         while (entries.hasMoreElements()) {
             entry = entries.nextElement();
             file = new File(dest, entry.getName());
             if (entry.isDirectory()) {
                 if (!file.mkdirs()) {
                     throw new IOException(file + ": cannot create directory");
                 }
             } else {
                 copy(buffer, jar.getInputStream(entry), file);
             }
         }
         jar.close();
    }

    private void copy(byte[] buffer, InputStream src, File file) throws IOException {
         OutputStream out;
         int len;
         
         out = new FileOutputStream(file);
         while (true)  {
             len = src.read(buffer);
             if (len < 0) {
                 break;
             }
             out.write(buffer, 0, len);
         }
         out.close();
         src.close();
    }
    
    private static List<String> pathStrings(Collection<Artifact> artifacts) {
        List<String> lst;
        
        lst = new ArrayList<String>();
        if (artifacts != null) {
            for (Artifact a : artifacts) {
                lst.add(a.getFile().getPath());
            }
        }

        return lst;
    }

}
