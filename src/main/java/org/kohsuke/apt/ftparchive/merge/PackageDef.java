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
        for (String line : lines) {
            if (line.startsWith(PACKAGE_HEADER)) {
                return line.substring(PACKAGE_HEADER.length());
            }
        }
        
        throw new AssertionError("No package name found in "+lines);
    }

    private static final String PACKAGE_HEADER = "Package: ";

    public void write(Writer w) throws IOException {
        for (String line : lines) {
            w.write(line);
            w.write('\n');
        }
        w.write('\n');
    }
}
