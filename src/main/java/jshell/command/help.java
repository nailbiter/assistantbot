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

import java.lang.reflect.*;
import jshell.*;
import jshell.util.*;

public class help extends Command
{
    public void execute(String[] args)
        throws
        ClassNotFoundException,
        IllegalAccessException
    {
        process_args(args);
        print_command_usage();
    }

    public void usage()
    {
        out().println("help command");
        out().println("    Provides usage information for builtin JShell\n"+
                      "    commands.");
    }

    private void process_args(String[] args)
    {
        _command =
            args.length == 0
            ? "help"
            : args[0];
    }

    private void print_command_usage()
    {
        try
        {
            String class_name = "jshell.command." + _command;
            Class klass = Class.forName(class_name);
            Command command = (Command) klass.newInstance();
            command.usage();
        }
        catch (ClassNotFoundException e)
        {
            System.out.println(_command+" is not a JShell command.");
        }
        catch (InstantiationException e)
        { Assertion.check(false); }
        catch (IllegalAccessException e)
        { Assertion.check(false); }
    }

    private String _command;
}
