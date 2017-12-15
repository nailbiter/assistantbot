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

public class JShellException extends RuntimeException
{
    public JShellException(String message)
    {
        super(message);
    }

    public JShellException(Vector exceptions)
    {
        super("Command execution failed.");
        _exceptions = exceptions;
    }

    public void describe(PrintStream out)
    {
        if (_exceptions == null)
            printStackTrace(out);
        else
        {
            for (Enumeration exception_scan = _exceptions.elements();
                 exception_scan.hasMoreElements();)
            {
                Throwable exception = (Throwable) exception_scan.nextElement();
                if (exception instanceof jshell.command.exit.ReallyExit)
                {
                    JShell.okToExit();
                    System.exit(0);
                }
                else
                    exception.printStackTrace(out);
            }
        }
    }

    private Vector _exceptions;
}
