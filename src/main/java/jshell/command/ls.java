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

import java.io.IOException;
import java.util.*;
import jshell.util.Queue;
import jshell.*;
import jshell.os.EPOC;
import jshell.util.*;

public class ls extends Command
{
    public void execute(String[] args)
        throws IOException, InterruptedException
    {
        process_environment();
        process_args(args);
        Vector files = files();
        File[] files_array = new File[files.size()];
        files.copyInto(files_array);
        sort_files(files_array);
        print_files(files_array);
    }

    public void usage()
    {
        out().println("ls ["+flag("ltrR")+"] file ...");
        out().println("    Lists the specified files. By default, only file\n"+
                      "    names are listed, and files are listed \n"+
                      "    alphabetically.\n"+
                      "    - The l flag produces a detailed listing.\n"+
                      "    - The t flag orders files by modification date,\n"+
                      "      oldest first.\n"+
                      "    - The r flag reverses the ordering.\n"+
                      "    - The R flag visits directories recursively.");
    }

    private void process_environment()
    {
        _current_dir = property("jshell.dir");
        _console_width = Integer.parseInt
            (property("jshell.columns"));
        String tab_stop_string = property
            ("jshell.ls.column_width");
        _tab_stop =
            tab_stop_string == null
            ? DEFAULT_TAB_STOP
            : Integer.parseInt(tab_stop_string);
    }

    private void process_args(String[] args)
    {
        _files = new Vector();
        _detailed = false;
        boolean at_least_one_path = false;
        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if (isFlag(arg))
                process_flag(arg);
            else
                _files.addElement(File.create(arg));
        }
        if (_files.size() == 0)
            add_files_in_current_directory();
    }

    private void process_flag(String flags)
    {
        for (int i = 1; i < flags.length(); i++)
        {
            char flag = flags.charAt(i);
            switch (flag)
            {
            case 'l':
                _detailed = true;
                break;

            case 't':
                _order_by_time = true;
                break;

            case 'r':
                _ordering = -1;
                break;

            case 'R':
                 _recursive = true;
                 break;

            default:
                throw new JShellException
                    ("Unrecognized flag to ls: "+flag);
            }
        }
    }

    private void add_files_in_current_directory()
    {
        File here = File.create(".");
        String[] file_names = here.list();
        for (int i = 0; i < file_names.length; i++)
            _files.addElement(File.create(file_names[i]));
    }

    private Vector files()
        throws IOException, InterruptedException
    {
        Vector result;
        if (_recursive)
        {
            Queue not_yet_visited = new Queue();
            for (Enumeration file_scan = _files.elements();
                 file_scan.hasMoreElements();)
            {
                checkForInterruption();
                File file = (File) file_scan.nextElement();
                not_yet_visited.add(file);
            }
            result = new Vector();
            while (!not_yet_visited.empty())
            {
                checkForInterruption();
                File file = (File) not_yet_visited.remove();
                if (_recursive && file.isDirectory())
                    add_files_in_directory
                        (file, not_yet_visited);
                else
                    result.addElement(file);
            }
        }
        else
            result = _files;
        return result;
    }

    private void add_files_in_directory
    (File file, Queue not_yet_visited)
        throws IOException
    {
        String dir_path = file.getPath();
        String[] dir_contents = file.list();
        for (int i = 0; i < dir_contents.length; i++)
        {
            String file_name = dir_contents[i];
            String file_path =
                Path.concatenate(dir_path, file_name);
            File dir_file = File.create(file_path);
            not_yet_visited.add(dir_file);
        }
    }

    private void sort_files(File[] files)
    {
        Sorter.Comparer comparer =
            _order_by_time
            ? (Sorter.Comparer)
              (new Sorter.Comparer()
               {
                   public int compare(Object x, Object y)
                   {
                       return
                           (int) (((File)x).lastModified() -
                                  ((File)y).lastModified());
                   }
               })
            : (Sorter.Comparer)
              (new Sorter.Comparer()
               {
                   public int compare(Object x, Object y)
                   {
                       File f = (File) x;
                       File g = (File) y;
                       return
                           f.toString()
                           .compareTo(g.toString()) * _ordering;
                   }
               });

        Sorter sorter = new Sorter(comparer);
        sorter.sort(files);
    }

    private void print_files(File[] files)
         throws InterruptedException
    {
    		System.err.println("was even here");
        for (int i = 0; i < files.length; i++)
        {
        		try {
        		System.err.print("i="+i);
            checkForInterruption();
            File file = files[i];
            System.err.println("/"+files.length+" "+files[i].getName());
            if (files[i].exists())
                if (_detailed)
                    print_detailed(file);
                else
                    print_brief(file);
        		}
        		catch(Exception e)
        		{
        			e.printStackTrace(System.out);
        		}
        }
        System.err.println("was still here");
        print_remainder();
        System.err.println("and even here");
    }

    private void print_brief(File file)
    {
        String file_name = file.getPath();
        if (file.isDirectory())
            file_name += '/';
        System.err.println(_buffer.length() +"/"+ file_name.length());
        if (_buffer.length() + file_name.length() >
            _console_width)
        {
        		try 
        		{
            out().println(_buffer);
            _buffer.setLength(0);
        		}
        		catch(Exception e)
        		{
        			e.printStackTrace(System.out);
        		}
        }
        _buffer.append(file_name);
        pad(_buffer,
            _tab_stop - (_buffer.length() % _tab_stop));
    }

    private void print_detailed(File file)
    {
        _buffer.setLength(0);

        // Date
        long timestamp = file.lastModified();
        Date date = new Date(timestamp);
        _buffer.append(date);

        // Size
        pad(_buffer,
            10 - Long.toString(file.length()).length());
        _buffer.append(file.length());

        // Permissions
        _buffer.append("  ");
        _buffer.append(file.canRead() ? 'r' : '-');
        _buffer.append(file.canWrite() ? 'w' : '-');
        _buffer.append("  ");

        // Name
        _buffer.append(file.getPath());
        if (file.isDirectory())
            _buffer.append('/');

        out().println(_buffer);

        // Needed here so that print_remainder doesn't
        // print the last line again.
        _buffer.setLength(0);
    }

    private void print_remainder()
    {
        if (_buffer.length() > 0)
            out().println(_buffer.toString());
    }

    private void pad(StringBuffer buffer, int n)
    {
        for (int i = 0; i < n; i++)
            buffer.append(' ');
    }

    private void pad_out(int n)
    {
        for (int i = 0; i < n; i++)
            out().print(' ');
    }

    private static final int DEFAULT_TAB_STOP = 16;

    private String _current_dir;
    private int _console_width;
    private int _tab_stop;
    private Vector _files;
    private StringBuffer _buffer = new StringBuffer();
    private boolean _detailed = false;
    private boolean _order_by_time = false;
    private boolean _recursive = false;
    private int _ordering = 1;
}
