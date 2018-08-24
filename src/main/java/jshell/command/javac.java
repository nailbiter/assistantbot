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

package jshell.command;

import java.io.*;
import java.util.*;
import jshell.*;
import jshell.util.*;

// Break ties
import jshell.File;  // Instead of java.io.File
//import com.*;

public class javac extends Command
{
    public void execute(String[] args)
    {
        process_args(args);
        sun.tools.javac.Main compiler = 
            new sun.tools.javac.Main(out(), "javac");
        compiler.compile(_javac_args);
    }

    public void usage()
    {
        out().println("javac [javac_args] java_source ...");
        out().println("    Invokes javac on the specified Java source files.\n"+
                      "    The compilation flags specified by javac_args are\n"+
                      "    relayed to javac.");
    }

    private void process_args(String[] args)
    {
        Vector javac_args = new Vector();
        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if (isFlag(arg))
                javac_args.addElement(arg.substring(1));
            else
            {
                File file = File.create(arg);
                javac_args.addElement(Path.jshellToOs(file.getAbsolutePath()));
            }
        }
        _javac_args = new String[javac_args.size()];
        javac_args.copyInto(_javac_args);
    }

    private String[] _javac_args;
}
