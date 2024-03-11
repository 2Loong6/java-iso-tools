/*
 * Copyright (c) 2010. Stephen Connolly.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.github.stephenc.javaisotools.loopfs.iso9660;

import com.github.stephenc.javaisotools.loopfs.spi.SeekableInputFile;
import com.github.stephenc.javaisotools.loopfs.spi.SeekableInputFileHadoop;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests the Iso9660 implementation.
 *
 * @author Stephen Connolly
 * @since Oct 5, 2010 1:06:36 PM
 */
public class Iso9660FileSystemTest {

    private static Properties testProperties;
    private static String filePath;

    @BeforeAll
    public static void loadConfiguration() throws Exception {
        testProperties = new Properties();
        try (InputStream is = Iso9660FileSystemTest.class.getResourceAsStream("/test.properties")) {
            testProperties.load(is);
            filePath = testProperties.getProperty("source-image");
        }
    }

    @Test
    public void smokes() throws Exception {
        Iso9660FileSystem image = new Iso9660FileSystem(new File(filePath), true);
        this.runCheck(image);
    }

    @Test
    public void shouldReadAllBytesWhenSeekableInputPartiallyReads() throws IOException {
        // Create seekeable input which does not read up to specified length
        SeekableInputFile input = new PartiallyReadSeekableInput();
        Iso9660FileSystem fs = new Iso9660FileSystem(input, true);
        Iso9660FileEntry entry = Iterables.getLast(fs);

        byte[] bytes = fs.getBytes(entry);

        assertEquals("Goodbye", new String(bytes), "All bytes should have been read");
    }

    @Test
    public void hdfsSmokes() throws Exception {
        assumeTrue(isNotWindows());
        //Creating a Mini DFS Cluster as the default File System does not return a Seekable Stream
        MiniDFSCluster.Builder builder = new MiniDFSCluster.Builder(new Configuration());
        try (MiniDFSCluster hdfsCluster = builder.build()) {
            String hdfsTestFile = "hdfs://127.0.0.1:" + hdfsCluster.getNameNodePort() + "/test/" + filePath;
            hdfsCluster.getFileSystem()
                    .copyFromLocalFile(new Path(filePath), new Path(hdfsTestFile));
            InputStream is = hdfsCluster.getFileSystem().open(new Path(hdfsTestFile));
            Iso9660FileSystem image = new Iso9660FileSystem(new SeekableInputFileHadoop(is), true);
            this.runCheck(image);
            hdfsCluster.shutdown();
        }
    }

    private void runCheck(Iso9660FileSystem image) throws Exception {
        File source = new File(testProperties.getProperty("source-root"));
        for (Iso9660FileEntry entry : image) {
            File sourceFile = new File(source, entry.getPath());
            assertEquals(entry.isDirectory(), sourceFile.isDirectory());
            if (!sourceFile.isDirectory()) {
                assertEquals(entry.getSize(), sourceFile.length());
                assertTrue(IOUtils.contentEquals(image.getInputStream(entry), new FileInputStream(sourceFile)), "contents are equal");
            }
        }
    }

    private boolean isNotWindows() {
        String os = System.getProperty("os.name");
        return !os.startsWith("Windows");
    }

    private static class PartiallyReadSeekableInput extends SeekableInputFile {

        private byte[] bytes;

        public PartiallyReadSeekableInput() throws IOException {
            super(new File(filePath));
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            // Deliberately miss last byte on first pass
            boolean firstPass = b != bytes;
            int length = firstPass ? len - 1 : len;
            bytes = b;
            return super.read(b, off, length);
        }
    }
}
