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

import jshell.*;

public class kill extends Command
{
    public void execute(String[] args)
         throws Exception
    {
        int[] job_ids = new int[args.length];
        for (int i = 0; i < args.length; i++)
            job_ids[i] = Integer.parseInt(args[i]);
        killJobs(job_ids);
    }

    public void usage()
    {
        out().println("kill job ...");
        out().println("    Terminates execution of the specified jobs.\n"+
                      "    The job numbers are obtained by running the\n"+
                      "    jobs command.");
    }
}
