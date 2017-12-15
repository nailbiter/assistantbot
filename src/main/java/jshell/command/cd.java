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

import java.io.IOException;
import java.util.*;
import jshell.*;

public class cd extends Command
{
    public void execute(String[] args)
        throws IOException
    {
        if (args.length != 1)
            err().println("cd must have exactly one argument.");
        else
        {
            File dir = File.create(args[0]);
            if (!dir.exists())
                err().println("No such directory.");
            else if (!dir.isDirectory())
                err().println(dir.getPath()+" is not a directory.");
            else
            {
                String new_dir = dir.getCanonicalPath();
                property("jshell.dir", new_dir);
                out().println(new_dir);
            }
        }
    }

    public void usage()
    {
        out().println("cd directory");
        out().println("    Go to the specified directory. Modifies the value\n"+
                      "    of jshell.dir.");
    }
}
