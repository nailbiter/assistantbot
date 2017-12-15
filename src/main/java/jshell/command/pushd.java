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

public class pushd extends Command
{
    public void execute(String[] args)
        throws IOException
    {
        if (args.length == 0)
            swap_current_and_top();
        else
        {
            push_current_directory();
            go_to_new_directory(args);
        }
    }

    public void usage()
    {
        out().println("pushd [directory]");
        out().println("    If no directory is specified, then the current\n"+
                      "    directory and the top of the directory stack\n"+
                      "    are swapped. Otherwise, the current directory is\n"+
                      "    pushed onto the directory stack, and the current\n"+
                      "    directory is then changed to the specified\n"+
                      "    directory. In both cases, the environment\n"+
                      "    variables jshell.dir and jshell.dir_stack\n"+
                      "    are modified.");
    }

    private void swap_current_and_top()
    {
        String dir_stack = property("jshell.dir_stack");
        String current_dir = property("jshell.dir");
        if (dir_stack == null)
            out().println(current_dir);
        else
        {
            int space = dir_stack.indexOf(" ");
            if (space > 0)
            {
                String top = dir_stack.substring(0, space);
                String remainder = dir_stack.substring(space);
                property("jshell.dir", top);
                property("jshell.dir_stack", current_dir + remainder);
            }
            else
            {
                property("jshell.dir", dir_stack);
                property("jshell.dir_stack", current_dir);
            }
            out().println(property("jshell.dir"));
        }
    }

    private void push_current_directory()
    {
        String dir_stack = property("jshell.dir_stack");
        String current_dir = property("jshell.dir");
        dir_stack =
            dir_stack == null
            ? current_dir
            : current_dir + ' ' + dir_stack;
        property("jshell.dir_stack", dir_stack);
    }

    private void go_to_new_directory(String[] args)
        throws IOException
    {
        cd cd_command = new cd();
        cd_command.execute(args);
    }
}
