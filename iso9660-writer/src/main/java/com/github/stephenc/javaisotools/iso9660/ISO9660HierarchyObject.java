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

package com.github.stephenc.javaisotools.iso9660;

public interface ISO9660HierarchyObject extends Cloneable, Comparable<ISO9660HierarchyObject> {

    /**
     * Returns the name of the hierarchy object
     *
     * @return Name
     */
    String getName();

    /**
     * Set the name of the hierarchy object
     *
     * @param name Name
     */
    void setName(String name);

    /**
     * Returns the root of the directory hierarchy
     *
     * @return Root
     */
    ISO9660RootDirectory getRoot();

    /**
     * Returns whether the hierarchy object is a directory
     *
     * @return Whether this is a directory
     */
    boolean isDirectory();

    /**
     * Returns the parent directory of this hierarchy object
     *
     * @return Parent directory
     */
    ISO9660Directory getParentDirectory();

    /**
     * Returns the path from the root to this hierarchy object with each path component separated by File.separator so
     * that its length represents the ISO 9660 path length
     *
     * @return Path
     */
    String getISOPath();

    /**
     * Returns an Object identifying this hierarchy object
     *
     * @return Identifying Object
     */
    Object getID();
}
