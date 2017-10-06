package org.cowboycoders.ant.profiles.fs;


import org.cowboycoders.ant.profiles.pages.BurstEncodable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Directory implements BurstEncodable
{
    private final List<FileEntry> files;
    private final DirectoryHeader header;

    public List<FileEntry> getFiles() {
        return files;
    }

    public DirectoryHeader getHeader() {
        return header;
    }

    public Directory(DirectoryHeader header, List<FileEntry> files) {
        this.header = header;
        this.files = files;
    }

    public Directory(ByteBuffer buf) {
        header = new DirectoryHeader(buf);
        files = new ArrayList<>();
        while (buf.remaining() >= FileEntry.ENTRY_LENGTH) {
            buf = buf.slice();
            files.add(new FileEntry(header, buf));
        }
    }

    @Override
    public void encode(ByteArrayOutputStream os) {
        header.encode(os);
        for (FileEntry entry : files) {
            entry.encode(os);
        }
    }

    public static class DirectoryBuilder {
        private List<FileEntry> files = new ArrayList<>();
        private DirectoryHeader.DirectoryHeaderBuilder header = new DirectoryHeader.DirectoryHeaderBuilder();

        public DirectoryBuilder addFile(FileEntry file) {
            files.add(file);
            return this;
        }

        public DirectoryBuilder setHeader(DirectoryHeader.DirectoryHeaderBuilder partialHeader) {
            this.header = partialHeader;
            return this;
        }

        public Directory create() {
            DirectoryHeader fullHeader = header.create();
            return new Directory(fullHeader, files);
        }
    }

}
