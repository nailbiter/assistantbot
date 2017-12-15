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

package jshell.util;

import java.io.File;
import java.util.*;
import jshell.JShellException;

public class Util
{
    public static void insert(Vector to, Vector from)
    {
        for (Enumeration from_scan = from.elements();
             from_scan.hasMoreElements();)
            to.addElement(from_scan.nextElement());
    }

    public static String systemProperty(String property)
    {
        return System.getProperty(property);
    }

    public static void systemProperty(String property, String value)
    {
        System.getProperties().put(property, value);
    }

    public static String removeEscapes(String s)
    {
        char[] chars = s.toCharArray();
        int from = 0;
        int to = 0;
        while (from < chars.length)
        {
            char c = chars[from++];
            if (c == '\\')
            {
                if (from == chars.length)
                    throw new JShellException
                        ("Malformed command-line argument: "+s);
                c = chars[from++];
            }
            chars[to++] = c;
        }
        return new String(chars, 0, to);
    }
}
