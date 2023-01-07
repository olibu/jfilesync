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

package jfs.sync.vfs;

import java.util.ArrayList;

import jfs.conf.JFSConst;
import jfs.conf.JFSLog;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;

/**
 * This class produces FTP files to be handled by the algorithm.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSVFSFileProducer.java,v 1.4 2008/06/11 15:13:08 heidrich Exp $
 */
public class JFSVFSFileProducer extends JFSFileProducer {
	/** The base file. */
	private FileObject baseFile = null;

	/**
	 * @see JFSFileProducer#JFSFileProducer(String, String)
	 */
	public JFSVFSFileProducer(String uri) {
		super(uri.substring(0, uri.indexOf(":")), uri);
	}

	/**
	 * Resets the file system manager.
	 */
	public void reset() {
		try {
			((DefaultFileSystemManager) VFS.getManager()).close();
			((DefaultFileSystemManager) VFS.getManager()).init();
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
		}
	}

	/**
	 * @return Returns the base file.
	 */
	public FileObject getBaseFile() {
		return baseFile;
	}

	/**
	 * @see JFSFileProducer#getRootJfsFile()
	 */
	public JFSFile getRootJfsFile() {
		JFSVFSFile jfsFile = new JFSVFSFile(this);
		baseFile = jfsFile.getFileObject();
		return jfsFile;
	}

	/**
	 * @see JFSFileProducer#getJfsFile(String)
	 */
	public JFSFile getJfsFile(String path) {
		return new JFSVFSFile(this, path);
	}

	/**
	 * @return Returns the available schemes.
	 */
	static public String[] getSchemes() {
		ArrayList<String> schemes = new ArrayList<String>();
		String[] schemesArray = new String[0];
		try {
			for (String s : VFS.getManager().getSchemes()) {
				if (!s.equals(JFSConst.SCHEME_LOCAL)
						&& !s.equals(JFSConst.SCHEME_EXTERNAL)) {
					schemes.add(s);
				}
			}
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
		}
		return schemes.toArray(schemesArray);
	}
}