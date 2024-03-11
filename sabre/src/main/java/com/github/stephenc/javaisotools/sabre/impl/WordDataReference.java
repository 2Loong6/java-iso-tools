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

import com.github.stephenc.javaisotools.sabre.DataReference;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class WordDataReference implements DataReference {

    private long value = 0;

    public WordDataReference(long value) {
        this.value = value;
    }

    public long getLength() {
        return 4;
    }

    public InputStream createInputStream() throws IOException {
        byte[] buffer = null;

        buffer = new byte[4];
        buffer[0] = (byte) ((this.value & 0xFF000000) >> 24);
        buffer[1] = (byte) ((this.value & 0x00FF0000) >> 16);
        buffer[2] = (byte) ((this.value & 0x0000FF00) >> 8);
        buffer[3] = (byte) (this.value & 0x000000FF);

        return new ByteArrayInputStream(buffer);
    }
}
