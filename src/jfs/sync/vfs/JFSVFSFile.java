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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import jfs.conf.JFSLog;
import jfs.sync.JFSFile;
import jfs.sync.JFSFileProducer;
import jfs.sync.JFSUserAuthentication;
import jfs.sync.JFSUserAuthenticationInterface;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;

/**
 * Represents an FTP file.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSVFSFile.java,v 1.5 2009/10/02 08:21:19 heidrich Exp $
 */
public class JFSVFSFile extends JFSFile {
	/** The list of included files. */
	private JFSFile[] list = null;

	/** The abstract file. */
	private FileObject file = null;

	/**
	 * Creates a new external root file and reads the structure from server.
	 * 
	 * @param fileProducer
	 *            The assigned file producer.
	 */
	public JFSVFSFile(JFSVFSFileProducer fileProducer) {
		super(fileProducer, "");
		try {
			FileSystemOptions opts = new FileSystemOptions();

			// Avoid using known hosts file if SFTP is used:
			if (fileProducer.getScheme().equals("sftp")) {
				SftpFileSystemConfigBuilder.getInstance()
						.setStrictHostKeyChecking(opts, "no");
			}

			// Get user name and password, if not specified:
			try {
				URI uriObject = new URI(fileProducer.getUri());
				String userInfo = uriObject.getUserInfo();
				if (userInfo == null || !userInfo.contains(":")) {
					JFSUserAuthentication userAuth = JFSUserAuthentication
							.getInstance();
					userAuth.setResource(fileProducer.getUri());
					JFSUserAuthenticationInterface userInterface = userAuth
							.getUserInterface();

					StaticUserAuthenticator auth = new StaticUserAuthenticator(
							null, userInterface.getUserName(), userInterface
									.getPassword());
					DefaultFileSystemConfigBuilder.getInstance()
							.setUserAuthenticator(opts, auth);
				}
			} catch (URISyntaxException e) {
				JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			}

			file = VFS.getManager().resolveFile(fileProducer.getUri(), opts);
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
		}
	}

	/**
	 * Creates a new external file for a certain path using a specific file
	 * producer.
	 * 
	 * @param fileProducer
	 *            The assigned file producer.
	 * @param path
	 *            The path to create the external file for.
	 */
	public JFSVFSFile(JFSVFSFileProducer fileProducer, String path) {
		super(fileProducer, path);
		try {
			file = VFS.getManager().resolveFile(fileProducer.getBaseFile(),
					path);
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
		}
	}

	/**
	 * Creates an external file based on a previously read-in structure.
	 * 
	 * @param fileProducer
	 *            The assigned file producer.
	 * @param file
	 *            The previously read-in file information object.
	 * @param relativePath
	 *            The relative path of the file.
	 */
	private JFSVFSFile(JFSFileProducer fileProducer, FileObject file,
			String relativePath) {
		super(fileProducer, relativePath);
		this.file = file;
	}

	/**
	 * @return Returns the file object.
	 */
	public FileObject getFileObject() {
		return file;
	}

	/**
	 * @see JFSFile#canRead()
	 */
	public boolean canRead() {
		if (file == null) {
			return false;
		}
		try {
			return file.isReadable();
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * @see JFSFile#canWrite()
	 */
	public boolean canWrite() {
		if (file == null) {
			return false;
		}
		try {
			return file.isWriteable();
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * @see JFSFile#canExecute()
	 */
	public boolean canExecute() {
		return false;
	}

	/**
	 * @see JFSFile#getInputStream()
	 */
	protected InputStream getInputStream() {
		if (file == null) {
			return null;
		}
		try {
			return file.getContent().getInputStream();
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			return null;
		}

	}

	/**
	 * @see JFSFile#getOutputStream()
	 */
	protected OutputStream getOutputStream() {
		if (file == null) {
			return null;
		}
		try {
			return file.getContent().getOutputStream();
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			return null;
		}
	}

	/**
	 * @see JFSFile#closeInputStream()
	 */
	protected void closeInputStream() {
		if (file != null) {
			try {
				file.getContent().close();
			} catch (IOException e) {
				JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			}
		}
	}

	/**
	 * @see JFSFile#closeOutputStream()
	 */
	protected void closeOutputStream() {
		if (file != null) {
			try {
				file.getContent().close();
			} catch (IOException e) {
				JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			}
		}
	}

	/**
	 * @see JFSFile#delete()
	 */
	public boolean delete() {
		if (file == null) {
			return false;
		}
		try {
			return file.delete();
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * @see JFSFile#exists()
	 */
	public boolean exists() {
		if (file == null) {
			return false;
		}
		try {
			return file.exists();
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * @see JFSFile#getLastModified()
	 */
	public long getLastModified() {
		if (file == null || isDirectory()) {
			return 0;
		}
		try {
			return file.getContent().getLastModifiedTime();
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			return 0;
		}
	}

	/**
	 * @see JFSFile#getLength()
	 */
	public long getLength() {
		if (file == null || isDirectory()) {
			return 0;
		}
		try {
			return file.getContent().getSize();
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			return 0;
		}
	}

	/**
	 * @see JFSFile#getList()
	 */
	public JFSFile[] getList() {
		if (file == null) {
			return new JFSVFSFile[0];
		}
		try {
			if (list == null) {
				FileObject[] files = file.getChildren();

				if (files != null) {
					list = new JFSVFSFile[files.length];

					for (int i = 0; i < files.length; i++) {
						list[i] = new JFSVFSFile(fileProducer, files[i],
								getRelativePath() + "/"
										+ files[i].getName().getBaseName());
					}
				} else {
					list = new JFSVFSFile[0];
				}
			}
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			list = new JFSVFSFile[0];
		}

		return list;
	}

	/**
	 * @see JFSFile#getFile()
	 */
	public final File getFile() {
		return null;
	}

	/**
	 * @see JFSFile#getName()
	 */
	public String getName() {
		if (file == null) {
			return "";
		}
		return file.getName().getBaseName();
	}

	/**
	 * @see JFSFile#getPath()
	 */
	public String getPath() {
		if (file == null) {
			return "";
		}
		return file.getName().getPath();
	}

	/**
	 * @see JFSFile#isDirectory()
	 */
	public boolean isDirectory() {
		if (file == null) {
			return false;
		}
		try {
			return file.getType().equals(FileType.FOLDER);
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * @see JFSFile#mkdir()
	 */
	public boolean mkdir() {
		if (file == null) {
			return false;
		}
		try {
			file.createFolder();
			file.refresh();
			return true;
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * @see JFSFile#setLastModified(long)
	 */
	public boolean setLastModified(long time) {
		if (file == null) {
			return false;
		}
		try {
			file.getContent().setLastModifiedTime(time);
			return true;
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * @see JFSFile#setReadOnly()
	 */
	public boolean setReadOnly() {
		// Not implemented yet:
		return true;
	}

	/**
	 * @see JFSFile#setExecutable()
	 */
	public boolean setExecutable () {
		// Not implemented yet:
		return true;
	}

	/**
	 * @see JFSFile#preCopyTgt(JFSFile)
	 */
	protected boolean preCopyTgt(JFSFile srcFile) {
		try {
			file.getContent().close();
			return true;
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * @see JFSFile#preCopySrc(JFSFile)
	 */
	protected boolean preCopySrc(JFSFile tgtFile) {
		return true;
	}

	/**
	 * @see JFSFile#postCopyTgt(JFSFile)
	 */
	protected boolean postCopyTgt(JFSFile srcFile) {
		try {
			if (!srcFile.isDirectory()) {
				file.getContent()
						.setLastModifiedTime(srcFile.getLastModified());
			}
			file.refresh();
			return true;
		} catch (FileSystemException e) {
			JFSLog.getErr().getStream().println(e.getLocalizedMessage());
			return false;
		}
	}

	/**
	 * @see JFSFile#postCopySrc(JFSFile)
	 */
	protected boolean postCopySrc(JFSFile tgtFile) {
		return true;
	}

	/**
	 * @see JFSFile#flush()
	 */
	public boolean flush() {
		return true;
	}
}