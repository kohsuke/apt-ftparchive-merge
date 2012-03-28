package org.kohsuke.apt.ftparchive.merge;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class PackageList extends ArrayList<PackageDef> {
    public PackageDef get(String name) {
        for (PackageDef p : this) {
            if (p.getName().equals(name))
                return p;
        }
        return null;
    }

    public void sort() {
        Collections.sort(this,new Comparator<PackageDef>() {
            public int compare(PackageDef o1, PackageDef o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    public void write(OutputStream out) throws IOException {
        OutputStreamWriter w = new OutputStreamWriter(out,"UTF-8");
        for (PackageDef p : this) {
            p.write(w);
        }
        w.flush();
    }

    public Set<String> getPackageNames() {
        Set<String> r = new HashSet<String>();
        for (PackageDef p : this)
            r.add(p.getName());
        return r;
    }
}
