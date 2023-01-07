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

import java.util.HashMap;

import jfs.conf.JFSConst;
import jfs.sync.external.JFSExternalFileProducerFactory;
import jfs.sync.local.JFSLocalFileProducerFactory;
import jfs.sync.vfs.JFSVFSFileProducerFactory;

/**
 * This class manages all JFS file producer factories that exist for the
 * program. It is able to detect the right file producer for a certain scheme
 * (like "ext" or "file") and stimulates the corresponding producer factory to
 * create a new file produces or to destroy an already existing file producer.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSFileProducerManager.java,v 1.1 2005/05/06 11:06:57 heidrich
 *          Exp $
 */
public class JFSFileProducerManager {
	/** Stores the only instance of the class. */
	private static JFSFileProducerManager instance = null;

	/** All registered factories for a certain URI scheme. */
	private final HashMap<String, JFSFileProducerFactory> factories = new HashMap<String, JFSFileProducerFactory>();

	/**
	 * Registers all factories and sets the default factory.
	 */
	private JFSFileProducerManager() {
		factories.put(JFSConst.SCHEME_LOCAL, new JFSLocalFileProducerFactory());
		factories.put(JFSConst.SCHEME_EXTERNAL,
				new JFSExternalFileProducerFactory());
		JFSVFSFileProducerFactory vfsFactory = new JFSVFSFileProducerFactory();
		for (String s : JFSConst.SCHEME_VFS) {
			factories.put(s, vfsFactory);
		}
	}

	/**
	 * Returns the reference of the only object of the class.
	 * 
	 * @return The only instance.
	 */
	public static JFSFileProducerManager getInstance() {
		if (instance == null)
			instance = new JFSFileProducerManager();

		return instance;
	}

	/**
	 * Resets all producers.
	 */
	public final void resetProducers() {
		for (JFSFileProducerFactory f : factories.values()) {
			f.resetProducers();
		}
	}

	/**
	 * Returns a new producer for a special URI.
	 * 
	 * @param uri
	 *            The URI to create the producer for.
	 * @return The created producer.
	 */
	public final JFSFileProducer createProducer(String uri) {
		return factories.get(getScheme(uri)).createProducer(uri);
	}

	/**
	 * Shuts down an existing producer for a special URI.
	 * 
	 * @param uri
	 *            The URI to destroy the producer for.
	 */
	public final void shutDownProducer(String uri) {
		factories.get(getScheme(uri)).shutDownProducer(uri);
	}

	/**
	 * Cancels an existing producer for a special URI.
	 * 
	 * @param uri
	 *            The URI to destroy the producer for.
	 */
	public final void cancelProducer(String uri) {
		factories.get(getScheme(uri)).cancelProducer(uri);
	}

	/**
	 * Returns the factory for a special URI.
	 * 
	 * @param uri
	 *            The URI to create the factory for.
	 * @return The created factory.
	 */
	public final String getScheme(String uri) {
		for (String scheme : factories.keySet()) {
			if (uri.startsWith(scheme + ":"))
				return scheme;
		}

		return JFSConst.SCHEME_LOCAL;
	}
}