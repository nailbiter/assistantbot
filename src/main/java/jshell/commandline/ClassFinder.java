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

package jshell.commandline;

import java.util.*;
import jshell.util.*;

class ClassFinder
{
    ClassFinder()
    {
        _stable_classes = new Hashtable();
        int i = 0;
        while (i < _stable_class_names.length)
        {
            String command = _stable_class_names[i++];
            String class_name = _stable_class_names[i++];
            try
            {
                Class klass = Class.forName(class_name);
                _stable_classes.put(command, klass);
            }
            catch (ClassNotFoundException e)
            {
                System.err.println("Couldn't find "+class_name);
                Assertion.check(false);
            }
        }
    }

    Class find(String class_name)
        throws ClassNotFoundException
    {
        Class klass = (Class) _stable_classes.get(class_name);
        if (klass == null)
        {
            JShellClassLoader loader = new JShellClassLoader();
            klass = loader.loadClass(class_name);
        }
        return klass;
    }

    private static String[] _stable_class_names =
    {
        // Real names
        "jshell.command.cat",         "jshell.command.cat",
        "jshell.command.cd",          "jshell.command.cd",
        "jshell.command.cp",          "jshell.command.cp",
        "jshell.command.dirs",        "jshell.command.dirs",
        "jshell.command.echo",        "jshell.command.echo",
        "jshell.command.env",         "jshell.command.env",
        "jshell.command.exit",        "jshell.command.exit",
        "jshell.command.gc",          "jshell.command.gc",
        "jshell.command.help",        "jshell.command.help",
        "jshell.command.history",     "jshell.command.history",
        "jshell.command.javac",       "jshell.command.javac",
        "jshell.command.jobs",        "jshell.command.jobs",
        "jshell.command.kill",        "jshell.command.kill",
        "jshell.command.ls",          "jshell.command.ls",
        "jshell.command.mkdir",       "jshell.command.mkdir",
        "jshell.command.popd",        "jshell.command.popd",
        "jshell.command.pushd",       "jshell.command.pushd",
        "jshell.command.pwd",         "jshell.command.pwd",
        "jshell.command.rm",          "jshell.command.rm",
        "jshell.command.save",        "jshell.command.save",
        "jshell.command.set",         "jshell.command.set",
        "jshell.command.sh",         "jshell.command.sh",

        // Aliases
        "cat",                        "jshell.command.cat",
        "cd",                         "jshell.command.cd",
        "cp",                         "jshell.command.cp",
        "dirs",                       "jshell.command.dirs",
        "echo",                       "jshell.command.echo",
        "env",                        "jshell.command.env",
        "exit",                       "jshell.command.exit",
        "gc",                         "jshell.command.gc",
        "help",                       "jshell.command.help",
        "history",                    "jshell.command.history",
        "javac",                      "jshell.command.javac",
        "jobs",                       "jshell.command.jobs",
        "kill",                       "jshell.command.kill",
        "ls",                         "jshell.command.ls",
        "mkdir",                      "jshell.command.mkdir",
        "popd",                       "jshell.command.popd",
        "pushd",                      "jshell.command.pushd",
        "pwd",                        "jshell.command.pwd",
        "quit",                       "jshell.command.exit",
        "rm",                         "jshell.command.rm",
        "save",                       "jshell.command.save",
        "set",                        "jshell.command.set",
        "sh",                        "jshell.command.sh",
    };
    public static String[] getCommandAliases()
    {
    		List<String> res = new ArrayList<String>();
    		System.out.println("len: "+_stable_class_names.length);
    		
    		for(int i = 0; i < _stable_class_names.length; i+=2)
    		{
    			if(!_stable_class_names[i].startsWith("jshell."))
    				res.add(_stable_class_names[i]);
    		}
    		
    		String[] model= {};
    		return res.toArray(model);
    }

    private Hashtable _stable_classes;
}
