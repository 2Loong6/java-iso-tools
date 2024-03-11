/*
 * Copyright (c) 2010. Stephen Connolly.
 * Copyright (C) 2007. Jens Hatlak <hatlak@rbg.informatik.tu-darmstadt.de>
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

package com.github.stephenc.javaisotools.joliet.impl;

import com.github.stephenc.javaisotools.iso9660.*;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.StreamHandler;

import java.nio.charset.StandardCharsets;

public class JolietLayoutHelper extends LayoutHelper {

    public JolietLayoutHelper(StreamHandler streamHandler, ISO9660RootDirectory root, int maxCharsInFilename, boolean failOnTruncation) {
        super(streamHandler, root, new JolietNamingConventions(maxCharsInFilename, failOnTruncation));
    }

    public FilenameDataReference getFilenameDataReference(ISO9660Directory dir) throws HandlerException {
        return new JolietFilenameDataReference(dir);
    }

    public FilenameDataReference getFilenameDataReference(ISO9660File file) throws HandlerException {
        return new JolietFilenameDataReference(file);
    }

    public byte[] pad(String string, int targetByteLength) throws HandlerException {
        byte[] bytes = new byte[targetByteLength];
        byte[] original = null;
        int length = 0;

        if (string != null) {
            original = string.getBytes(StandardCharsets.UTF_16BE); // UCS-2
            length = original.length;
        }
        System.arraycopy(original, 0, bytes, 0, length);
        for (int i = length; i < bytes.length; i++) {
            bytes[i] = 0;
            i++;
            if (i < bytes.length) {
                bytes[i] = 0x20;
            }
        }
        bytes[bytes.length - 1] = 0; // Zero-terminate String

        return bytes;
    }
}
