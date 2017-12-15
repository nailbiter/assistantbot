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

public class History
{
    public static History only()
    {
        return _only;
    }

    public void record(String command_line)
    {
        Record record = new Record(_id_counter++, command_line);
        _commands.put(new Integer(record.id()), record);
        trim_history();
    }
    
    public void printLast(int n, PrintStream out)
    {
        if (n == 0 || n > _commands.size())
            n = _commands.size();
        int first = _id_counter - n;
        int last = _id_counter - 1;
        for (int i = first; i <= last; i++)
        {
            Integer id = new Integer(i);
            Record record = (Record) _commands.get(id);
            out.println(record.id()+": "+
                        record.commandLine());
        }
    }
    
    public int lastCommandId()
    {
        return _id_counter - 1;
    }
    
    public String commandLine(int id)
    {
        Record record = (Record) _commands.get(new Integer(id));
        return
            record == null
            ? null
            : record.commandLine();
    }

    private void trim_history()
    {
        int history_size = Integer.parseInt
            (Util.systemProperty(JShell.JSHELL_HISTORY_SIZE));
        int first_to_delete = _id_counter - _commands.size();
        int n_delete = _commands.size() - history_size;
        int last_to_delete = first_to_delete + n_delete - 1;
        for (int i = first_to_delete; i <= last_to_delete; i++)
        {
            Integer id = new Integer(i);
            Assertion.check(_commands.remove(id) != null);
        }
    }

    private static History _only = new History();

    private int _id_counter = 0;
    private Hashtable _commands = new Hashtable();

    //------------------------------------------------------------

    static class Record
    {
        public Record(int id, 
                      String command_line)
        {
            _id = id;
            _command_line = command_line;
        }

        public int id()
        {
            return _id;
        }

        public String commandLine()
        {
            return _command_line;
        }


        private int _id;
        private String _command_line;
    }
}
