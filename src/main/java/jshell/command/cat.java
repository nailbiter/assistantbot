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

public class cat extends Command
{
    public void execute(String[] args)
        throws IOException, InterruptedException
    {
        Vector files = new Vector();
        for (int i = 0; i < args.length; i++)
        {
            File file = File.create(args[i]);
            if (file.exists() && !file.isDirectory())
                files.addElement(file);
            else
            {
                out().print(file.getPath());
                if (file.exists())
                    out().println(": directory");
                else
                    out().println(": does not exist");
            }
        }
        for (Enumeration file_scan = files.elements();
             file_scan.hasMoreElements();)
        {
            File file = (File) file_scan.nextElement();
            type(file);
        }
    }

    public void usage()
    {
        out().println("cat file ...");
        out().println
            ("    Prints the files specified on the command line with no\n"+
             "    header or separator.");
    }

    private void type(File file)
        throws IOException, InterruptedException
    {
        BufferedReader in =
            new BufferedReader(file.fileReader());
        String line;
        while ((line = in.readLine()) != null)
        {
            checkForInterruption();
            out().println(line);
        }
        in.close();
    }
}
