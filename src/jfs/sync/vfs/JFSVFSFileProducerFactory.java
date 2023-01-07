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

import java.util.HashMap;

import jfs.conf.JFSConfig;
import jfs.sync.JFSFileProducer;
import jfs.sync.JFSFileProducerFactory;

/**
 * This class produces factories for FTP files.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSVFSFileProducerFactory.java,v 1.1 2008/06/10 18:35:22
 *          heidrich Exp $
 */
public class JFSVFSFileProducerFactory extends JFSFileProducerFactory {
	/** The map of file producers. */
	private HashMap<String, JFSVFSFileProducer> producers = new HashMap<String, JFSVFSFileProducer>();

	/**
	 * @see JFSFileProducerFactory#resetProducers()
	 */
	public final void resetProducers() {
		// Reset previous producers before clearing the list:
		for (JFSVFSFileProducer p : producers.values()) {
			p.reset();
		}
		producers.clear();
	}

	/**
	 * @see JFSFileProducerFactory#createProducer(String)
	 */
	public final JFSFileProducer createProducer(String uri) {
		JFSVFSFileProducer p = new JFSVFSFileProducer(uri);
		producers.put(uri, p);
		return p;
	}

	/**
	 * @see JFSFileProducerFactory#shutDownProducer(String)
	 */
	public final void shutDownProducer(String uri) {
		JFSVFSFileProducer p = producers.get(uri);
		if (p != null && JFSConfig.getInstance().getServerShutDown()) {
			// Shut-down currently not possible.
		}
	}

	/**
	 * @see JFSFileProducerFactory#cancelProducer(String)
	 */
	public final void cancelProducer(String uri) {
		JFSVFSFileProducer p = producers.get(uri);
		if (p != null) {
			//TODO: Cancellation currently not possible.
		}
	}
}