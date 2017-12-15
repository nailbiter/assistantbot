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
import jshell.util.*;

public class FileFilter implements FilenameFilter
{
    public String toString()
    {
        return _pattern.toString();
    }

    public FileFilter(String filter)
    {
        int slash = filter.lastIndexOf("/");
        if (slash >= 0)
            filter = filter.substring(slash + 1);
        _pattern = GlobPattern.create(filter);
    }

    public boolean accept(java.io.File dir, String file_name)
    {
        return _pattern.match(file_name);
    }

    public boolean hasWildcards()
    {
        return _pattern.hasWildcards();
    }

    private GlobPattern _pattern;
}
