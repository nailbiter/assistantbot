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

public class env extends Command
{
    public void execute(String[] args)
    {
        switch (args.length)
        {
            case 0:
                print_all();
                break;
                
            case 1:
                print_filtered(args[0]);
                break;
                
            default:
                break;
        }
    }

    public void usage()
    {
        out().println("env [filter]");
        out().println("    Prints environment values and their values.\n"+
                      "    With no arguments, all variables are printed.\n"+
                      "    Otherwise, print the variables whose names match\n"+
                      "    the filter argument. Environment variables\n"+
                      "    include the contents of System.getProperties().");
    }

    private void print_all()
    {
        print_filtered(null);
    }

    private void print_filtered(String filter)
    {
        String[] env = properties_as_array(filter);
        sort(env);
        print(env);
    }

    private String[] properties_as_array(String filter)
    {
        GlobPattern pattern_matcher = 
            filter == null
            ? null
            : GlobPattern.create(filter);
        Vector qualifying_env = new Vector();
        for (Enumeration scan = properties().keys(); scan.hasMoreElements();)
        {
            String var = (String) scan.nextElement();
            if (filter == null || pattern_matcher.match(var))
            {
                String value = property(var);
                qualifying_env.addElement(var + '=' + value);
            }
        }
        String[] result = new String[qualifying_env.size()];
        qualifying_env.copyInto(result);
        return result;
    }

    private void sort(String[] env)
    {
        Sorter sorter = new Sorter
            (new Sorter.Comparer()
             {
                 public int compare(Object x, Object y)
                 {
                     return ((String)x).compareTo((String)y);
                 }
             });
        sorter.sort(env);
    }

    private void print(String[] env)
    {
        for (int i = 0; i < env.length; i++)
            out().println(env[i]);
    }
}
