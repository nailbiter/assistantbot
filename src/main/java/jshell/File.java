// JShell
// Copyright (C) 2000 Jack A. Orenstein
// 
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of
// the License, or (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
// 02111-1307, USA.
// 
// Jack A. Orenstein  jao@mediaone.net

package jshell;

import java.io.*;
import java.util.*;
import jshell.util.*;

public abstract class File extends java.io.File
{
    // Object interface

    public String toString()
    {
        return getAbsolutePath();
    }

    public int hashCode()
    {
        return getAbsolutePath().hashCode();
    }


    // File interface

    public String getAbsolutePath()
    {
        return _path.absolutePath();
    }

    public String getCanonicalPath() 
        throws IOException
    {
        return _path.canonicalPath();
    }

    public String getPath() 
    {
        return _path.statedPath();
    }
    
    public boolean isAbsolute() 
    {
        return _path.absolute();
    }

    public String getParent()
    {
        String absolute_path = _path.absolutePath();
        String parent = null;
        int last_slash = absolute_path.lastIndexOf("/");
        if (last_slash >= 0)
            parent = absolute_path.substring(0, last_slash);
        return parent;
    }


    // Creation methods

    public static File create(String path)
    {
        return JShell.os().createFile(path);
    }

    public static File create(String directory, String file_name)
    {
        return JShell.os().createFile(directory, file_name);
    }


    // jshell.File interface

    public FileInputStream inputStream()
        throws IOException
    {
        String absolute_jshell_path = _path.absolutePath();
        String absolute_os_path = Path.jshellToOs(absolute_jshell_path);
        return new FileInputStream(absolute_os_path);
    }

    public FileOutputStream outputStream()
        throws IOException
    {
        return outputStream(false);
    }

    public FileOutputStream outputStream(boolean append)
        throws IOException
    {
        String absolute_jshell_path = _path.absolutePath();
        String absolute_os_path = Path.jshellToOs(absolute_jshell_path);
        return new FileOutputStream(absolute_os_path, append);
    }

    public FileReader fileReader()
        throws IOException
    {
        String absolute_jshell_path = _path.absolutePath();
        String absolute_os_path = Path.jshellToOs(absolute_jshell_path);
        return new FileReader(absolute_os_path);
    }



    // For use by subclasses

    protected File(String path)
    {
        // Why Path.create is called twice: The invocation to
        // super has to be the first statement in the constructor, 
        // and _path can't be assigned inside the super() invocation.
        super(Path.jshellToOs(Path.create(path).absolutePath()));
        _path = Path.create(path);
    }

    protected File(String directory, String file_name)
    {
        this(Path.concatenate(directory, file_name));
    }


    // Representation

    protected Path _path;
}
