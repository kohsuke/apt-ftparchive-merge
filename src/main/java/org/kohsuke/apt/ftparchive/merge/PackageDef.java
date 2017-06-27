package org.kohsuke.apt.ftparchive.merge;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class PackageDef {
    /**
     * Lines without the last '\n'
     */
    private final List<String> lines;

    public PackageDef(List<String> lines) {
        if (lines.isEmpty())
            throw new AssertionError();
        this.lines = lines;
    }
    
    public String getName() {
        return getHeader(PACKAGE_HEADER);
    }

    public String getVersion() {
        return getHeader(VERSION_HEADER);
    }

    private String getHeader(String prefix) {
        for (String line : lines) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length());
            }
        }
        
        throw new AssertionError("No '"+prefix+"' header found in "+lines);
    }

    private static final String PACKAGE_HEADER = "Package: ";
    private static final String VERSION_HEADER = "Version: ";

    public void write(Writer w) throws IOException {
        for (String line : lines) {
            w.write(line);
            w.write('\n');
        }
        w.write('\n');
    }
}
