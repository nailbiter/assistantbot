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
import java.lang.reflect.*;
import java.util.*;
import jshell.commandline.*;
import jshell.util.*;

public class JShell
{
    public static final String JSHELL_BUFFER         = "jshell.buffer";
    public static final String JSHELL_PAGE           = "jshell.page";
    public static final String JSHELL_PROMPT         = "jshell.prompt";
    public static final String JSHELL_COLUMNS        = "jshell.columns";
    public static final String JSHELL_LINES          = "jshell.lines";
    public static final String JSHELL_TIME_COMMANDS  = "jshell.time_commands";
    public static final String JSHELL_DIR            = "jshell.dir";
    public static final String JSHELL_HISTORY_SIZE   = "jshell.history_size";
    public static final String JSHELL_FLAG           = "jshell.flag";

    
    // Interface for interactive usage

    public static void main(String[] args)
        throws Exception
    {
        new JShell(true /* interactive */);
    }


    // Interface for programmatic usage

    public static JShell create()
        throws Exception
    {
        return new JShell(false /* not interactive */);
    }

    public void runCommand(String command)
        throws Exception
    {
        process_command_line(command);
    }


    // For use by JShell

    public static OS os()
    {
        return _os;
    }

    public static void okToExit()
    {
        _security_manager.okToExit();
    }


    // For use by this class

    private JShell(boolean interactive)
        throws Exception
    {
        initialize(interactive);
        // The command jshell.command.exit calls System.exit.
        if (_interactive)
            while (true)
                process_command_line();
    }

    private void initialize(boolean interactive)
        throws Exception
    {
    			
    			_security_manager = new JShellSecurityManager();
    			if(interactive)//FIXME?
    				System.setSecurityManager(_security_manager);

        _interactive = interactive;

        // Set up object for dealing with platform dependencies.
        System.out.println("was here");
        _os = OS.create();

        // Set up shell input
        _shell_input = new BufferedReader
            (new InputStreamReader(System.in));

        // Set up environment
        initialize_environment();
    }

    private void initialize_environment()
    {
        Util.systemProperty(JSHELL_PROMPT, ">");
        Util.systemProperty(JSHELL_PAGE, "false");
        Util.systemProperty(JSHELL_COLUMNS, "60");
        Util.systemProperty(JSHELL_LINES, "19");
        Util.systemProperty(JSHELL_BUFFER, "false");
        Util.systemProperty(JSHELL_FLAG, ":");
        Util.systemProperty
            (JSHELL_DIR,
             Path.osToJshell(System.getProperty("user.dir")));
        Util.systemProperty(JSHELL_HISTORY_SIZE, "100");
    }
        
    private void process_command_line()
        throws Exception
    {
        String text = read_command_line();
        try
        { process_command_line(text); }
        catch (Exception e)
        { System.err.println("Illegal syntax."); }
    }

    private void process_command_line(String command_line)
        throws Exception
    {
        JobThread job_thread = null;
        job_thread = JobThread.create(command_line);
        job_thread.startWork();
        job_thread.waitIfForegroundJob();
    }

    private String read_command_line()
        throws IOException
    {
        String line = null;
        do
        {
            System.out.print
                ((History.only().lastCommandId() + 1)+
                 System.getProperty("jshell.prompt")+" ");
            System.out.flush();
            line = readLine();
        }
        while (line == null || line.trim().length() == 0);
        // Trim to get rid of cr and or lf at end of line.
        return line.trim();
    }

    // Needed because BufferedReader.readLine doesn't work
    // on Psion 5mx
    public String readLine() 
        throws java.io.IOException
    {
        StringBuffer s = new StringBuffer();
        char c;
        while ((c = (char)System.in.read())!= '\n')
        {
            if (c == '\b' && s.length() > 0)
                s.setLength(s.length() - 1);
            else
                s.append(c);
        }
        return s.toString();
    }

    private static OS _os;
    private static JShellSecurityManager _security_manager;

    private BufferedReader _shell_input;
    private boolean _interactive;
}
