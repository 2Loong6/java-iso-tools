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

package com.github.stephenc.javaisotools.iso9660.impl;

import com.github.stephenc.javaisotools.sabre.*;
import com.github.stephenc.javaisotools.sabre.impl.FileFixup;

import java.io.*;

public class ISOImageFileHandler implements StreamHandler {

    private final File file;
    private final RandomAccessFile raFile;
    private final DataOutputStream dataOutputStream;
    private long position = 0;

    /**
     * ISO Image File Handler
     *
     * @param file ISO image output file
     * @throws FileNotFoundException File not found
     */
    public ISOImageFileHandler(File file) throws FileNotFoundException {
        this.file = file;
        this.raFile = new RandomAccessFile(file, "rw");
        this.dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.file)));
    }

    public void startDocument() throws HandlerException {
        // nothing to do here
    }

    public void startElement(Element element) throws HandlerException {
        // nothing to do here
    }

    public void data(DataReference reference) throws HandlerException {
        byte[] buffer;
        int bytesToRead;
        int bytesHandled;
        int bufferLength = 65535;
        long lengthToWrite;
        long length;

        try (InputStream inputStream = reference.createInputStream()) {
            buffer = new byte[bufferLength];
            length = reference.getLength();
            lengthToWrite = length;
            while (lengthToWrite > 0) {
                if (lengthToWrite > bufferLength) {
                    bytesToRead = bufferLength;
                } else {
                    bytesToRead = (int) lengthToWrite;
                }

                bytesHandled = inputStream.read(buffer, 0, bytesToRead);
                if (bytesHandled == -1) {
                    throw new HandlerException("Cannot read all data from reference.");
                }

                dataOutputStream.write(buffer, 0, bytesHandled);
                lengthToWrite -= bytesHandled;
                position += bytesHandled;
            }
            dataOutputStream.flush();
        } catch (IOException e) {
            throw new HandlerException(e);
        }
    }

    public Fixup fixup(DataReference reference) throws HandlerException {
        Fixup fixup;
        fixup = new FileFixup(raFile, position, reference.getLength());
        data(reference);
        return fixup;
    }

    public long mark() throws HandlerException {
        return position;
    }

    public void endElement() throws HandlerException {
        // nothing to do here
    }

    public void endDocument() throws HandlerException {
        try {
            this.raFile.close();
            this.dataOutputStream.close();
        } catch (IOException e) {
            throw new HandlerException(e);
        }
    }
}
