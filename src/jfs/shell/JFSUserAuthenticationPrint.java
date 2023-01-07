/*
 * JFileSync
 * Copyright (C) 2002-2007, Jens Heidrich
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA, 02110-1301, USA
 */

package jfs.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import jfs.conf.JFSLog;
import jfs.conf.JFSText;
import jfs.sync.JFSUserAuthentication;
import jfs.sync.JFSUserAuthenticationInterface;

/**
 * Asks for the user name and password on the command line.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSUserAuthenticationPrint.java,v 1.2 2009/10/08 08:19:53 heidrich Exp $
 */
public class JFSUserAuthenticationPrint implements
		JFSUserAuthenticationInterface {
	/** The user name. */
	String userName = "";

	/** The password. */
	String password = "";

	/**
	 * @see JFSUserAuthenticationInterface#ask(JFSUserAuthentication)
	 */
	public void ask(JFSUserAuthentication auth) {
		PrintStream p = JFSLog.getOut().getStream();
		JFSText t = JFSText.getInstance();
		userName = auth.getUriUserName();
		password = auth.getUriPassword();

		p.println();
		p.println(t.get("auth.print.resource"));
		p.println("  '" + auth.getResource() + "'");

		p.print("  " + t.get("auth.print.userName"));
		if (userName.length() > 0) {
			p.println(" ('" + userName + "')");
		} else {
			p.println();
		}
		BufferedReader dinUserName = new BufferedReader(new InputStreamReader(
				System.in));
		try {
			String readUserName = dinUserName.readLine();
			if (readUserName != null && !readUserName.trim().equals("")) {
				userName = readUserName;
			}
		} catch (IOException exeption) {
			JFSLog.getErr().getStream().println(t.get("error.inputRead"));
		}

		p.print("  " + t.get("auth.print.password"));
		if (password.length() > 0) {
			p.println(" ('" + password + "')");
		} else {
			p.println();
		}
		BufferedReader dinPassword = new BufferedReader(new InputStreamReader(
				System.in));
		try {
			String readPassword = dinPassword.readLine();
			if (readPassword != null && !readPassword.trim().equals("")) {
				password = readPassword;
			}
		} catch (IOException exeption) {
			JFSLog.getErr().getStream().println(t.get("error.inputRead"));
		}
	}

	/**
	 * @see JFSUserAuthenticationInterface#getPassword()
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @see JFSUserAuthenticationInterface#getUserName()
	 */
	public String getUserName() {
		return userName;
	}
}