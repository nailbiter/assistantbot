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

package jshell;

import java.io.*;
import java.util.*;
import jshell.commandline.*;
import jshell.util.*;

public class Jobs
{
    public static Jobs only()
    {
        return _only;
    }

    public void add(CommandLineInterpreter job)
    {
        _jobs.addElement(job);
    }

    public CommandLineInterpreter job(int id)
    {
        for (Enumeration job_scan = _jobs.elements();
             job_scan.hasMoreElements();)
        {
            CommandLineInterpreter job =
                (CommandLineInterpreter) job_scan.nextElement();
            if (job.id() == id)
                return job;
        }
        return null;
    }

    public void remove(CommandLineInterpreter job)
    {
        if (job != null)
            _jobs.removeElement(job);
    }

    public void print(PrintStream out)
    {
        for (Enumeration job_scan = _jobs.elements();
             job_scan.hasMoreElements();)
        {
            CommandLineInterpreter job =
                (CommandLineInterpreter) job_scan.nextElement();
            out.print(job.id());
            out.print(": ");
            out.println(job.commandLine());
        }
    }

    public void kill(int[] job_ids)
    {
        for (int i = 0; i < job_ids.length; i++)
        {
            int job_id = job_ids[i];
            CommandLineInterpreter job = find(job_id);
            if (job != null)
            {
                remove(job);
                job.kill();
            }
        }
    }

    private CommandLineInterpreter find(int job_id)
    {
        for (Enumeration job_scan = _jobs.elements();
             job_scan.hasMoreElements();)
        {
            CommandLineInterpreter job =
                (CommandLineInterpreter) job_scan.nextElement();
            if (job.id() == job_id)
                return job;
        }
        return null;
    }

    private static Jobs _only = new Jobs();

    private Vector _jobs = new Vector();
}
