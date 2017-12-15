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

public class echo extends Command
{
    public void execute(String[] args)
        throws IOException
    {
        if (args.length == 0)
            echo_input();
        else
            echo_command_line_args(args);
    }

    public void usage()
    {
        out().println("echo [expression ...]");
        out().println("    With no arguments, echo copies standard\n"+
                      "    input to System.out and standard output.\n"+
                      "    This is useful for examining data as it\n"+
                      "    flows from one command to another.\n"+
                      "    If command line arguments are specified,\n"+
                      "    echo simply evaluates and prints the\n"+
                      "    expressions.");
    }

    private void echo_input()
         throws IOException
    {
        // Print input and pass it on
        BufferedReader in = 
            new BufferedReader(new InputStreamReader(in()));
        String line;
        while ((line = in.readLine()) != null)
        {
            System.out.println(line);
            out().println(line);
        }
    }

    private void echo_command_line_args(String[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            if (i > 0)
                out().print(' ');
            out().print(args[i]);
        }
        out().println();
    }
}
