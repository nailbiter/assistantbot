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

public class JShellSecurityManager extends SecurityManager
{
    // SecurityManager interface

    public void checkCreateClassLoader()
    {}

    public void checkAccess(Thread thread)
    {}

    public void checkAccess(ThreadGroup thread_group)
    {}

    public void checkExit(int exit_code)
    {
        if (!_ok_to_exit)
            throw new JShellCommandExit(exit_code);
    }

    public void checkExec(String s)
    {}

    public void checkLink(String s)
    {}

    public void checkRead(java.io.FileDescriptor fd)
    {}

    public void checkRead(String s)
    {}

    public void checkRead(String s, Object o)
    {}

    public void checkWrite(java.io.FileDescriptor fd)
    {}

    public void checkWrite(String s)
    {}

    public void checkDelete(String s)
    {}

    public void checkConnect(String s, int i)
    {}

    public void checkConnect(String s, int i, Object o)
    {}

    public void checkListen(int i)
    {}

    public void checkAccept(String s, int i)
    {}

    public void checkMulticast(java.net.InetAddress a)
    {}

    public void checkMulticast(java.net.InetAddress a, byte b)
    {}

    public void checkPropertiesAccess()
    {}

    public void checkPropertyAccess(String s)
    {}

    public boolean checkTopLevelWindow(Object o)
    {
        return true;
    }

    public void checkPrintJobAccess()
    {}

    public void checkSystemClipboardAccess()
    {}

    public void checkAwtEventQueueAccess()
    {}

    public void checkPackageAccess(String s)
    {}

    public void checkPackageDefinition(String s)
    {}

    public void checkSetFactory()
    {}

    public void checkMemberAccess(Class c, int i)
    {}

    public void checkSecurityAccess(String s)
    {}


    // JShellSecurityManager interface

    public void okToExit()
    {
        _ok_to_exit = true;
    }


    private boolean _ok_to_exit = false;
}
