/*
 * Copyright (c) 2010. Stephen Connolly
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.github.stephenc.javaisotools.iso9660.impl;

import com.github.stephenc.javaisotools.iso9660.ISO9660Directory;
import com.github.stephenc.javaisotools.iso9660.ISO9660File;
import com.github.stephenc.javaisotools.iso9660.ISO9660RootDirectory;
import com.github.stephenc.javaisotools.joliet.impl.JolietConfig;
import com.github.stephenc.javaisotools.rockridge.impl.RockRidgeConfig;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.StreamHandler;
import com.github.stephenc.javaisotools.sabre.impl.ByteArrayDataReference;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Some simple ISO file system tests.
 *
 * @author connollys
 * @since Sep 24, 2010 3:27:44 PM
 */
public class CreateISOTest {

    private static File workDir;

    private Random entropy = new Random();

    @BeforeAll
    public static void loadConfiguration() throws Exception {
        Properties props = new Properties();
        try (InputStream is = CreateISOTest.class.getResourceAsStream("/test.properties")) {
            props.load(is);
        }
        workDir = new File(props.getProperty("work-directory"));
        assertTrue(workDir.mkdirs() || workDir.isDirectory(), "We can create our work directory");
    }

    @Test
    public void canCreateAnEmptyIso() throws Exception {
        // Output file
        File outfile = new File(workDir, "empty.iso");

        // Directory hierarchy, starting from the root
        ISO9660RootDirectory.MOVED_DIRECTORIES_STORE_NAME = "rr_moved";
        ISO9660RootDirectory root = new ISO9660RootDirectory();
        StreamHandler streamHandler = new ISOImageFileHandler(outfile);
        CreateISO iso = new CreateISO(streamHandler, root);
        iso.process(new ISO9660Config(), null, null, null);

        assertTrue(outfile.isFile());
        assertNotEquals(0L, outfile.length());

        // TODO use loop-fs to check that the iso is empty
    }

    @Test
    public void canCreateAnIsoWithOneFile() throws Exception {
        final String contentString = "This is a test file";
        // Output file
        File outfile = new File(workDir, "one-file.iso");
        File contents = new File(workDir, "readme.txt");
        OutputStream os = new FileOutputStream(contents);
        IOUtils.write(contentString, os, StandardCharsets.UTF_8);
        IOUtils.close(os);

        // Directory hierarchy, starting from the root
        ISO9660RootDirectory.MOVED_DIRECTORIES_STORE_NAME = "rr_moved";
        ISO9660RootDirectory root = new ISO9660RootDirectory();

        root.addFile(contents);

        StreamHandler streamHandler = new ISOImageFileHandler(outfile);
        CreateISO iso = new CreateISO(streamHandler, root);
        ISO9660Config iso9660Config = new ISO9660Config();
        iso9660Config.allowASCII(false);
        iso9660Config.setInterchangeLevel(1);
        iso9660Config.restrictDirDepthTo8(true);
        iso9660Config.setVolumeID("ISO Test");
        iso9660Config.forceDotDelimiter(true);
        RockRidgeConfig rrConfig = new RockRidgeConfig();
        rrConfig.setMkisofsCompatibility(false);
        rrConfig.hideMovedDirectoriesStore(true);
        rrConfig.forcePortableFilenameCharacterSet(true);

        JolietConfig jolietConfig = new JolietConfig();
        jolietConfig.setVolumeID("Joliet Test");
        jolietConfig.forceDotDelimiter(true);

        iso.process(iso9660Config, rrConfig, jolietConfig, null);

        assertTrue(outfile.isFile());
        assertNotEquals(0L, outfile.length());

        FileSystemManager fsManager = VFS.getManager();
        // TODO figure out why we can't just do
        // FileObject isoFile = fsManager.resolveFile("iso:/" + outfile.getPath() + "!/");
        // smells like a bug between loop-fs and commons-vfs
        FileObject isoFile = fsManager.resolveFile("iso:/" + outfile.getPath() + "!/readme.txt").getParent();
        assertEquals(FileType.FOLDER, isoFile.getType());

        FileObject[] children = isoFile.getChildren();
        assertEquals(1, children.length);
        assertEquals("readme.txt", children[0].getName().getBaseName());
        assertEquals(FileType.FILE, children[0].getType());
        assertEquals(contentString, IOUtils.toString(children[0].getContent().getInputStream(), StandardCharsets.UTF_8));
    }

    @Test
    public void canCreateAnIsoWithSomeFiles() throws Exception {
        // Output file
        File outfile = new File(workDir, "test.iso");
        File contentsA = new File(workDir, "a.txt");
        OutputStream os = new FileOutputStream(contentsA);
        IOUtils.write("Hello", os, StandardCharsets.UTF_8);
        IOUtils.close(os);
        File contentsB = new File(workDir, "b.txt");
        os = new FileOutputStream(contentsB);
        IOUtils.write("Goodbye", os, StandardCharsets.UTF_8);
        IOUtils.close(os);

        // Directory hierarchy, starting from the root
        ISO9660RootDirectory.MOVED_DIRECTORIES_STORE_NAME = "rr_moved";
        ISO9660RootDirectory root = new ISO9660RootDirectory();

        ISO9660Directory dir = root.addDirectory("root");
        dir.addFile(contentsA);
        dir.addFile(contentsB);

        StreamHandler streamHandler = new ISOImageFileHandler(outfile);
        CreateISO iso = new CreateISO(streamHandler, root);
        ISO9660Config iso9660Config = new ISO9660Config();
        iso9660Config.allowASCII(false);
        iso9660Config.setInterchangeLevel(2);
        iso9660Config.restrictDirDepthTo8(true);
        iso9660Config.setVolumeID("ISO Test");
        iso9660Config.forceDotDelimiter(true);
        RockRidgeConfig rrConfig = new RockRidgeConfig();
        rrConfig.setMkisofsCompatibility(true);
        rrConfig.hideMovedDirectoriesStore(true);
        rrConfig.forcePortableFilenameCharacterSet(true);

        JolietConfig jolietConfig = new JolietConfig();
        jolietConfig.setVolumeID("Joliet Test");
        jolietConfig.forceDotDelimiter(true);

        iso.process(iso9660Config, rrConfig, jolietConfig, null);

        assertTrue(outfile.isFile());
        assertNotEquals(0, outfile.length());

        FileSystemManager fsManager = VFS.getManager();
        FileObject isoFile = fsManager.resolveFile("iso:/" + outfile.getPath() + "!/root");

        FileObject t = isoFile.getChild("a.txt");
        assertNotNull(t);
        assertEquals(FileType.FILE, t.getType());
        assertEquals(5, t.getContent().getSize());
        assertEquals("Hello", IOUtils.toString(t.getContent().getInputStream(), StandardCharsets.UTF_8));
        t = isoFile.getChild("b.txt");
        assertNotNull(t);
        assertEquals(FileType.FILE, t.getType());
        assertEquals(7, t.getContent().getSize());
        assertEquals("Goodbye", IOUtils.toString(t.getContent().getInputStream(), StandardCharsets.UTF_8));
    }

    @Test
    public void canCreateAnIsoWithLoadsOfFiles() throws Exception {
        final int numFiles = entropy.nextInt(50) + 50;
        // Output file
        File outfile = new File(workDir, "big.iso");
        File rootDir = new File(workDir, "big");
        assertTrue(rootDir.isDirectory() || rootDir.mkdirs());

        // Directory hierarchy, starting from the root
        ISO9660RootDirectory.MOVED_DIRECTORIES_STORE_NAME = "rr_moved";
        ISO9660RootDirectory root = new ISO9660RootDirectory();
        for (int i = 0; i < numFiles; i++) {
            File content = new File(rootDir, Integer.toString(i) + ".bin");
            int length = entropy.nextInt(1024 * 10 + 1);
            byte[] contents = new byte[length];
            entropy.nextBytes(contents);
            try (FileOutputStream fos = new FileOutputStream(content)) {
                fos.write(contents);
            }
            root.addFile(content);
        }

        StreamHandler streamHandler = new ISOImageFileHandler(outfile);
        CreateISO iso = new CreateISO(streamHandler, root);
        ISO9660Config iso9660Config = new ISO9660Config();
        iso9660Config.allowASCII(false);
        iso9660Config.setInterchangeLevel(2);
        iso9660Config.restrictDirDepthTo8(true);
        iso9660Config.setVolumeID("ISO Test");
        iso9660Config.forceDotDelimiter(true);
        RockRidgeConfig rrConfig = new RockRidgeConfig();
        rrConfig.setMkisofsCompatibility(true);
        rrConfig.hideMovedDirectoriesStore(true);
        rrConfig.forcePortableFilenameCharacterSet(true);

        JolietConfig jolietConfig = new JolietConfig();
        jolietConfig.setVolumeID("Joliet Test");
        jolietConfig.forceDotDelimiter(true);

        iso.process(iso9660Config, rrConfig, jolietConfig, null);

        assertTrue(outfile.isFile());
        assertNotEquals(0, outfile.length());

        FileSystemManager fsManager = VFS.getManager();
        for (int i = 0; i < numFiles; i++) {
            File content = new File(rootDir, i + ".bin");
            FileObject t = fsManager.resolveFile("iso:/" + outfile.getPath() + "!/" + i + ".bin");
            assertNotNull(t);
            assertEquals(FileType.FILE, t.getType());
            assertEquals(content.length(), t.getContent().getSize());
            assertArrayEquals(IOUtils.toByteArray(new FileInputStream(content)), IOUtils.toByteArray(t.getContent().getInputStream()));
        }
    }

    @Test
    public void canCreateAnIsoTopDownHierarchy() throws Exception {
        // Output file
        File outfile = new File(workDir, "test.iso");
        File contentsA = new File(workDir, "a.txt");
        OutputStream os = new FileOutputStream(contentsA);
        IOUtils.write("Hello", os, StandardCharsets.UTF_8);
        IOUtils.close(os);
        File contentsB = new File(workDir, "b.txt");
        os = new FileOutputStream(contentsB);
        IOUtils.write("Goodbye", os, StandardCharsets.UTF_8);
        IOUtils.close(os);

        // Top down
        ISO9660RootDirectory root = new ISO9660RootDirectory();
        ISO9660Directory n1 = root.addDirectory("D1");
        ISO9660Directory n2 = n1.addDirectory("D2");
        ISO9660Directory n3 = n2.addDirectory("D3");
        n3.addFile(contentsA);
        n3.addFile(contentsB);

        StreamHandler streamHandler = new ISOImageFileHandler(outfile);
        CreateISO iso = new CreateISO(streamHandler, root);
        ISO9660Config iso9660Config = new ISO9660Config();
        iso9660Config.allowASCII(false);
        iso9660Config.setInterchangeLevel(2);
        iso9660Config.restrictDirDepthTo8(true);
        iso9660Config.setVolumeID("ISO Test");
        iso9660Config.forceDotDelimiter(true);
        RockRidgeConfig rrConfig = new RockRidgeConfig();
        rrConfig.setMkisofsCompatibility(true);
        rrConfig.hideMovedDirectoriesStore(true);
        rrConfig.forcePortableFilenameCharacterSet(true);

        JolietConfig jolietConfig = new JolietConfig();
        jolietConfig.setVolumeID("Joliet Test");
        jolietConfig.forceDotDelimiter(true);

        iso.process(iso9660Config, rrConfig, jolietConfig, null);

        assertTrue(outfile.isFile());
        assertNotEquals(0, outfile.length());
    }

    @Test
    public void canCreateAnIsoBottomUpHierarchy() throws Exception {
        // Output file
        File outfile = new File(workDir, "test.iso");
        File contentsA = new File(workDir, "a.txt");
        OutputStream os = new FileOutputStream(contentsA);
        IOUtils.write("Hello", os, StandardCharsets.UTF_8);
        IOUtils.close(os);
        File contentsB = new File(workDir, "b.txt");
        os = new FileOutputStream(contentsB);
        IOUtils.write("Goodbye", os, StandardCharsets.UTF_8);
        IOUtils.close(os);

        // Bottom up
        ISO9660Directory n3 = new ISO9660Directory("D3");
        n3.addFile(contentsA);
        n3.addFile(contentsB);
        ISO9660Directory n2 = new ISO9660Directory("D2");
        n2.addDirectory(n3);
        ISO9660Directory n1 = new ISO9660Directory("D1");
        n1.addDirectory(n2);
        ISO9660RootDirectory root = new ISO9660RootDirectory();
        root.addDirectory(n1);

        StreamHandler streamHandler = new ISOImageFileHandler(outfile);
        CreateISO iso = new CreateISO(streamHandler, root);
        ISO9660Config iso9660Config = new ISO9660Config();
        iso9660Config.allowASCII(false);
        iso9660Config.setInterchangeLevel(2);
        iso9660Config.restrictDirDepthTo8(true);
        iso9660Config.setVolumeID("ISO Test");
        iso9660Config.forceDotDelimiter(true);
        RockRidgeConfig rrConfig = new RockRidgeConfig();
        rrConfig.setMkisofsCompatibility(true);
        rrConfig.hideMovedDirectoriesStore(true);
        rrConfig.forcePortableFilenameCharacterSet(true);

        JolietConfig jolietConfig = new JolietConfig();
        jolietConfig.setVolumeID("Joliet Test");
        jolietConfig.forceDotDelimiter(true);

        iso.process(iso9660Config, rrConfig, jolietConfig, null);

        assertTrue(outfile.isFile());
        assertNotEquals(0, outfile.length());
    }

    @Test
    public void canOpenFakeIso() throws Exception {
        final String contentString = "This is a text file, not an iso";
        // Output file
        File fakeIso = new File(workDir, "fake.iso");
        OutputStream os = new FileOutputStream(fakeIso);
        IOUtils.write(contentString, os, StandardCharsets.UTF_8);
        IOUtils.close(os);

        // Trying to open a fake iso
        FileSystemManager fsManager = VFS.getManager();
        FileObject fo = fsManager.resolveFile("iso:/" + fakeIso.getPath() + "!/");
        assertFalse(fo.exists(), "The file '" + fakeIso.getName() + "' is not a valid iso file");
    }

    @Test
    public void cahShortenLongFileNames() throws Exception {
        File outfile = new File(workDir, "64chars.iso");

        ISO9660RootDirectory root = new ISO9660RootDirectory();

        root.addFile(new ISO9660File(new ByteArrayDataReference("Hello, world!".getBytes(StandardCharsets.UTF_8)), "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.wsdl", 1121040000L));
        root.addFile(new ISO9660File(new ByteArrayDataReference("Hello, world!".getBytes(StandardCharsets.UTF_8)), "yxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.wsdl", 1121040000L));

        root.addFile(new ISO9660File(new ByteArrayDataReference("Hello, world!".getBytes(StandardCharsets.UTF_8)), "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", 1121040000L));
        root.addFile(new ISO9660File(new ByteArrayDataReference("Hello, world!".getBytes(StandardCharsets.UTF_8)), "yxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", 1121040000L));

        StreamHandler streamHandler = new ISOImageFileHandler(outfile);
        CreateISO iso = new CreateISO(streamHandler, root);

        ISO9660Config iso9660Config = new ISO9660Config();
        iso9660Config.setVolumeID("ISO Test");
        iso9660Config.setVolumeSetID("ISO Test");

        JolietConfig jolietConfig = new JolietConfig();
        jolietConfig.setVolumeID("ISO Test");
        jolietConfig.setVolumeSetID("ISO Test");

        iso.process(iso9660Config, null, jolietConfig, null);
    }

    @Test
    public void canFailOnTruncatedName() throws Exception {
        File outfile = new File(workDir, "truncate.iso");

        ISO9660RootDirectory root = new ISO9660RootDirectory();

        root.addFile(new ISO9660File(new ByteArrayDataReference("Hello, world!".getBytes(StandardCharsets.UTF_8)), "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.wsdl", 1121040000L));

        StreamHandler streamHandler = new ISOImageFileHandler(outfile);
        CreateISO iso = new CreateISO(streamHandler, root);

        ISO9660Config iso9660Config = new ISO9660Config();
        iso9660Config.setVolumeID("ISO Test");
        iso9660Config.setVolumeSetID("ISO Test");

        JolietConfig jolietConfig = new JolietConfig();
        jolietConfig.setVolumeID("ISO Test");
        jolietConfig.setVolumeSetID("ISO Test");
        jolietConfig.setMaxCharsInFilename(12);
        jolietConfig.setFailOnTruncation(true);

        try {
            iso.process(iso9660Config, null, jolietConfig, null);

            fail("Should have failed because a filename would have been truncated");
        } catch (HandlerException x) {
            /* Success: the truncation was noted */
        }
    }
}
