# apt-ftparchive merger
This tool allows you to merge `Packages` and `Contents` file generated from a local `apt-ftparchive` run
with an existing `Packages.gz` and `Contents.gz` hosted on a web server.

This is useful to incrementally add a new package to an existing apt repository, without needing to
host the entire files locally.

To merge `./Packages` and `http://acmecorp.org/debian/binary/Packages.gz` and generate
`path/to/merged/Packages`, invoke the command as follows:

```
mvn org.kohsuke:apt-ftparchive-merge:latest:merge -Durl=http://acmecorp.org/debian/binary/ -Dout=path/to/merged
```

If the remote repository does not exist, the tool will treat it as empty package list, and simply copy
`./Packages` to `path/to/merged/Packages`.