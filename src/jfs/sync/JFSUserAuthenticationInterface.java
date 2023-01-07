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

package jfs.sync;

/**
 * An interface for asking the user to enter username and password.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSUserAuthenticationInterface.java,v 1.1 2008/06/11 12:10:59 heidrich Exp $
 */
public interface JFSUserAuthenticationInterface {

	/**
	 * This method is called every time user interaction is needed.
	 * 
	 * @param question
	 *            The question to ask.
	 */
	public void ask(JFSUserAuthentication auth);

	/**
	 * @return Returns the user name.
	 */
	public String getUserName();

	/**
	 * @return Returns the password.
	 */
	public String getPassword();
}