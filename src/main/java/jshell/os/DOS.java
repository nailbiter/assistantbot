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

package jshell.os;

import java.io.*;
import jshell.*;
import jshell.util.*;

// Break ties
import jshell.File;

public class DOS extends OS
{
    public boolean caseSensitive()
    {
        return false;
    }

    public String jshellToOsPath(String jshell_path)
    {
        String os_path = null;
        switch (jshell_path.length())
        {
        case 0:
            Assertion.check(false);
            break;

        case 1:
            os_path = "\\";
            break;

        case 2:
            if (jshell_path.charAt(0) == '/')
                os_path = jshell_path.charAt(1) + ":\\";
            else
                throw new InvalidJShellPathException
                    (jshell_path+" is not a valid jshell path.");
            break;
            
        default:
            if (jshell_path.charAt(0) == '/' && 
                jshell_path.charAt(2) == '/')
                os_path = 
                    jshell_path.charAt(1) + 
                    ":" + 
                    jshell_path.substring(2);
            else
                throw new InvalidJShellPathException
                    (jshell_path+" is not a valid "+
                     "jshell path.");
        }
        os_path = os_path.replace('/', File.separatorChar);
        return os_path;
    }

    public String osToJshellPath(String os_path)
    {
        String jshell_path = null;
        if (os_path.charAt(1) == ':' &&
            os_path.charAt(2) == File.separatorChar)
            jshell_path =
                "/" +
                os_path.charAt(0) +
                os_path.substring(2);
        else
            throw new JShellException(os_path+" is not a valid OS path.");
        jshell_path = jshell_path.replace(File.separatorChar, '/');
        if (jshell_path.endsWith("/") && jshell_path.length() > 1)
            jshell_path = 
                jshell_path.substring(0, jshell_path.length() - 1);
        return jshell_path;
    }

    public String canonicalizePath(String path)
    {
        return Path.simplify(path);
    }

    public jshell.File createFile(String path)
    {
        return new DOSFile(path);
    }

    public jshell.File createFile(String directory, String file_name)
    {
        return new DOSFile(directory, file_name);
    }
}
