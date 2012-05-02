package org.kohsuke.apt.ftparchive.merge;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Merges APT repository index in the current directory with what's specified.
 *
 * @goal merge
 * @requiresProject false
 */
public class MergeMojo
    extends AbstractMojo
{
    /**
     * Location of the remote repository.
     * @parameter expression="${url}"
     * @required
     */
    private URL repository;

    /**
     * Output directory.
     * @parameter expression="${out}"
     * @required
     */
    private File output;
    
    public void execute() throws MojoExecutionException {
        try {
            getLog().info("Merging Packages");

            boolean skip = !isEmpty(System.getenv("SKIP_APT_MERGE"));

            PackageList base = skip ? new PackageList() : loadPackages(new GZIPInputStream(new URL(repository, "Packages.gz").openStream()));

            PackageList updated = loadPackages(new FileInputStream(new File("Packages")));

            for (PackageDef p : updated) {
                PackageDef old = base.get(p.getName());
                if (old!=null)  base.remove(old);
                base.add(p);
            }
            base.sort();

            output.mkdirs();
            FileOutputStream pw = new FileOutputStream(new File(output,"Packages"));
            base.write(pw);
            pw.close();

            {// merge Contents
                getLog().info("Merging Contents");
                Set<String> localPackageNames = updated.getPackageNames();

                // copy remote Contents.gz to the output while leaving out our local overwrites
                PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(output,"Contents")),"UTF-8"));
                String line;
                if (!skip) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new URL(repository,"Contents.gz").openStream()),"UTF-8"));
                    while ((line=in.readLine())!=null) {
                        int idx = Math.max(line.lastIndexOf('\t'),line.lastIndexOf(' '));
                        String name = line.substring(idx+1);
                        if (localPackageNames.contains(name))
                            continue;   // skip as we'll overwrite this
                        out.println(line);
                    }
                    in.close();
                }
                
                // now stream through our local overwrites
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("Contents"),"UTF-8"));
                while ((line=in.readLine())!=null) {
                    out.println(line);
                }
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoExecutionException("Failed to merge",e);
        }

    }

    private boolean isEmpty(String s) {
        return s==null || s.trim().length()==0;
    }

    private PackageList loadPackages(InputStream in) throws IOException {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(in,"UTF-8"));
            PackageList packages = new PackageList();
            String line;
            List<String> read = new ArrayList<String>();
            while ((line=r.readLine())!=null) {
                if (line.length()==0) {
                    packages.add(new PackageDef(read));
                    read = new ArrayList<String>();
                } else {
                    read.add(line);
                }
            }
            if (!read.isEmpty())
                packages.add(new PackageDef(read)); // this shouldn't happen, IIUC
            return packages;
        } finally {
            in.close();
        }
    }
}
