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
import jshell.*;

public class dirs extends Command
{
    public void execute(String[] args)
    {
        String dir_stack = property("jshell.dir_stack");
        if (dir_stack == null)
            out().println("Directory stack is empty.");
        else
            out().println(dir_stack);
    }

    public void usage()
    {
        out().println("dirs");
        out().println("    Prints the directory stack, starting with the\n"+
                      "    most recently visited directory. The stack is\n"+
                      "    recorded in the variable jshell.dir_stack.\n"+
                      "    The stack is modified using the commands pushd\n"+
                      "    and popd.");
    }
}
