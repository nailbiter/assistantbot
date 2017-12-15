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
import jshell.File; // instead of java.io.File

public class save extends Command
{
    public void execute(String[] args)
        throws IOException
    {
        process_args(args);
        File file = File.create(args[0]);
        if (file.isDirectory())
            err().println(file.getPath()+" is a directory");
        else
        {
            FileOutputStream out = file.outputStream(_append);
            byte[] buffer = new byte[BUFFER_SIZE];
            int n_read;
            while ((n_read = in().read(buffer)) != -1)
                out.write(buffer, 0, n_read);
            out.close();
        }
    }

    public void usage()
    {
        out().println("save "+flag("a")+" file");
        out().println("    The input stream is saved in the specified file.\n"+
                      "    By default, the file is overwritten if it exists.\n"+
                      "    The a flag causes the input stream to be appended\n"+
                      "    instead. The save command is not normally needed\n"+
                      "    because file redirection (using > or >>) can be\n"+
                      "    used instead.");
    }

    private void process_args(String[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if (isFlag(arg))
            {
                if (arg.charAt(1) == 'a')
                    _append = true;
                else
                    err().println
                        ("Ignoring unrecognized flag for save: "+
                         arg);
            }
            else
            {
                if (_file_name == null)
                    _file_name = arg;
                else
                    err().println
                        ("Extra filename to save ignored: "+
                         arg+
                         ". Saving to "+_file_name+".");
            }
        }
    }

    private static final int BUFFER_SIZE = 1024;

    private String _file_name = null;
    private boolean _append = false;
}
