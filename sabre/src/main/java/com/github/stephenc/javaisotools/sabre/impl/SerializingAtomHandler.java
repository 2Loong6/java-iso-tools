/*
 * Copyright (c) 2010. Stephen Connolly.
 * Copyright (c) 2006. Michael Hartle <mhartle@rbg.informatik.tu-darmstadt.de>.
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

package com.github.stephenc.javaisotools.sabre.impl;

import com.github.stephenc.javaisotools.sabre.*;

import java.io.*;

public class SerializingAtomHandler implements StreamHandler {

    private final RandomAccessFile randomAccessFile;
    // private DataOutputStream dataOutputStream = null;
    private long position = 0;

    public SerializingAtomHandler(File file) throws FileNotFoundException {
        this.randomAccessFile = new RandomAccessFile(file, "rw");
        // this.dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.file)));
    }

    public void startDocument() throws HandlerException {
        try {
            this.randomAccessFile.setLength(0);
        } catch (IOException e) {
            throw new HandlerException(e);
        }
    }

    public void startElement(Element element) throws HandlerException {
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

            this.randomAccessFile.seek(this.position);
            while (lengthToWrite > 0) {
                if (lengthToWrite > bufferLength) {
                    bytesToRead = bufferLength;
                } else {
                    bytesToRead = (int) lengthToWrite;
                }

                bytesHandled = inputStream.read(buffer, 0, bytesToRead);
                // System.out.println(" Got " + reference);
                // System.out.println(" => " + inputStream + ", " + inputStream.available());
                // System.out.println(" " + lengthToWrite + " => " + bytesToRead + ", handled " + bytesHandled);
                if (bytesHandled == -1) {
                    // System.out.println("Trying to read again... " + inputStream.read());
                    throw new HandlerException("Cannot read all data from reference.");
                }

                // dataOutputStream.write(buffer, 0, bytesHandled);
                this.randomAccessFile.write(buffer, 0, bytesHandled);
                lengthToWrite -= bytesHandled;
                this.position += bytesHandled;
            }
            // dataOutputStream.flush();
        } catch (IOException e) {
            throw new HandlerException(e);
        }
    }

    public Fixup fixup(DataReference reference) throws HandlerException {
        Fixup fixup = new FileFixup(this.randomAccessFile, this.position, reference.getLength());
        data(reference);
        return fixup;
    }

    public long mark() throws HandlerException {
        return this.position;
    }

    public void endElement() throws HandlerException {
    }

    public void endDocument() throws HandlerException {
        try {
            // this.dataOutputStream.close();
            this.randomAccessFile.close();
        } catch (IOException e) {
            throw new HandlerException(e);
        }

    }
}
