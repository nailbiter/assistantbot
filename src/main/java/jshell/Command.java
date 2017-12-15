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
import jshell.util.*;

public abstract class Command
{
    // Command interface

    public abstract void execute(String[] args)
        throws Exception;

    public void usage()
    {
        out().println("help not available for "+
                      remove_package_name(getClass().getName()));
    }


    // For use by Command subclasses

    protected Command()
    {}

    protected final InputStream in()
    {
        if (_in == null)
            _in = System.in;
        return _in;
    }

    protected final PrintStream out()
    {
        if (_out == null)
        {
            _out = (_custom_out==null) ? System.out : new PrintStream(_custom_out);
            if (System.getProperty
                (JShell.JSHELL_BUFFER).equals("true"))
                _out = new MultilineBufferPrintStream(_out);
            if (System.getProperty
                (JShell.JSHELL_PAGE).equals("true"))
                _out = new PagedPrintStream(_out);
        }
        return _out;
    }

    protected final PrintStream err()
    {
        if (_err == null)
        {
            _err = System.err;
            if (System.getProperty
                (JShell.JSHELL_BUFFER).equals("true"))
                _err = new MultilineBufferPrintStream(_err);
            if (System.getProperty
                (JShell.JSHELL_PAGE).equals("true"))
                _err = new PagedPrintStream(_err);
        }
        return _err;
    }

    protected void checkForInterruption()
         throws InterruptedException
    {
        if (Thread.interrupted())
            throw new InterruptedException();
    }

    public static Properties properties()
    {
        return System.getProperties();
    }

    public static String property(String var)
    {
        return System.getProperty(var);
    }

    public static void property(String var, String value)
    {
        System.getProperties().put(var, value);
    }

    public static void removeProperty(String var)
    {
        System.getProperties().remove(var);
    }

    public final void finish()
    {
        close_in();
        close_out();
        close_err();
    }

    public static void connectOutputToInput
    (Object source, Object destination)
        throws IOException
    {
        PipedOutputStream out = new PipedOutputStream();
        InputStream in = new PipedInputStream(out);
        ((Command)source)._out = new PrintStream(out);
        ((Command)source)._redirect_out = true;
        ((Command)destination)._in = in;
        ((Command)destination)._redirect_in = true;
    }

    protected void printHistory(int n)
    {
        History.only().printLast(n, out());
    }

    protected void printJobs()
    {
        Jobs.only().print(out());
    }

    protected void killJobs(int[] job_ids)
    {
        Jobs.only().kill(job_ids);
    }

    protected boolean isFlag(String arg)
    {
        return arg.charAt(0) ==
               Util.systemProperty(JShell.JSHELL_FLAG).charAt(0);
    }

    protected String flag(String arg)
    {
        return Util.systemProperty(JShell.JSHELL_FLAG) + arg;
    }

    private void close_in()
    {
        if (_in != null)
        {
            try
            {
                if (_redirect_in)
                    _in.close();
            }
            catch (IOException e) {}
        }
    }

    private void close_out()
    {
        if (_out != null)
        {
            _out.flush();
            if (_redirect_out)
                _out.close();
        }
    }

    private void close_err()
    {
        if (_err != null)
            _err.flush();
    }

    private String remove_package_name(String class_name)
    {
        int dot = class_name.lastIndexOf(".");
        return
            dot < 0
            ? class_name
            : class_name.substring(dot + 1);
    }
    public static void setCustomOut(OutputStream out)
    {
    		_custom_out = out;
    }

    private static OutputStream _custom_out = null;
    private InputStream _in = null;
    private PrintStream _out = null;
    private PrintStream _err = null;
    private boolean _redirect_in = false;
    private boolean _redirect_out = false;
}
