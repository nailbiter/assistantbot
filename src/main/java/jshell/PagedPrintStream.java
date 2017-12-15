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

class PagedPrintStream extends PrintStream
{
    public PagedPrintStream(PrintStream stream)
    {
        super(stream);
    }

    public void println()
    {
        super.println();
        check_page_end();
    }

    public void println(boolean x)
    {
        super.println(x);
        check_page_end();
    }
    
    public void println(char x)
    {
        super.println(x);
        check_page_end();
    }
    
    public void println(char[] x)
    {
        super.println(x);
        check_page_end();
    }
    
    public void println(double x)
    {
        super.println(x);
        check_page_end();
    }
    
    public void println(float x)
    {
        super.println(x);
        check_page_end();
    }
    
    public void println(int x)
    {
        super.println(x);
        check_page_end();
    }
    
    public void println(long x)
    {
        super.println(x);
        check_page_end();
    }
    
    public void println(Object x)
    {
        super.println(x);
        check_page_end();
    }
    
    public void println(String x)
    {
        super.println(x);
        check_page_end();
    }

    private void check_page_end()
    {
        String page_size_string = System.getProperty(JShell.JSHELL_LINES);
        int page_size =
            page_size_string == null
            ? Integer.MAX_VALUE
            : Integer.parseInt(page_size_string);
        if (++_lines_printed == page_size)
        {
            super.print("Press Enter for more.");
            super.flush();
            try
            {
                System.in.read();
            }
            catch (IOException e) {}
            super.println();
            _lines_printed = 0;
        }
    }

    private int _lines_printed;
    private boolean _done = false;
}
