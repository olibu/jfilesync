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

import java.io.PrintStream;

import jfs.conf.JFSDirectoryPair;
import jfs.conf.JFSHistory;
import jfs.conf.JFSHistoryManager;
import jfs.conf.JFSLog;
import jfs.conf.JFSSyncMode;
import jfs.conf.JFSText;
import jfs.conf.JFSSyncMode.SyncAction;

/**
 * Represents a JFS root element. File factories are created for the source and
 * target side, which may be accessed (and shut down) via this object.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSRootElement.java,v 1.6 2009/10/08 08:19:53 heidrich Exp $
 */
public class JFSRootElement extends JFSElement {

	/** The used producer to create source JFS file objects. */
	private JFSFileProducer srcProducer;

	/** The used producer to create target JFS file objects. */
	private JFSFileProducer tgtProducer;

	/** The synchronization history. */
	private JFSHistory history;

	/**
	 * Constructs a root element.
	 * 
	 * @param pair
	 *            The directory pair to construct a root element for.
	 */
	public JFSRootElement(JFSDirectoryPair pair) {
		// Create file producers to use:
		JFSFileProducerManager pm = JFSFileProducerManager.getInstance();
		srcProducer = pm.createProducer(pair.getSrc());
		tgtProducer = pm.createProducer(pair.getTgt());

		// Extract root source and target:
		srcFile = srcProducer.getRootJfsFile();
		tgtFile = tgtProducer.getRootJfsFile();

		// Assert root characteristics:
		assert srcFile != null && tgtFile != null;

		// Test whether the file objects exists and are directories:
		JFSText t = JFSText.getInstance();
		if (!srcFile.exists() || !tgtFile.exists() || !srcFile.isDirectory()
				|| !tgtFile.isDirectory()) {
			PrintStream p = JFSLog.getErr().getStream();
			p.println(t.get("error.validDirectoryPair"));
			p.println("  '" + srcProducer.getRootPath() + "', ");
			p.println("  '" + tgtProducer.getRootPath() + "'");
			isActive = false;
		} else {
			isActive = true;
		}

		// Set root standard characteristics:
		root = this;
		parent = this;
		isDirectory = true;
		state = ElementState.IS_ROOT;
		action = SyncAction.NOP_ROOT;

		// Load history
		history = JFSHistoryManager.getInstance().getHistory(pair);
		history.load();
	}

	/**
	 * Returns the assigned source JFS file producer.
	 * 
	 * @return The source producer.
	 */
	public JFSFileProducer getSrcProducer() {
		return srcProducer;
	}

	/**
	 * Returns the assigned target JFS file producer.
	 * 
	 * @return The target producer.
	 */
	public JFSFileProducer getTgtProducer() {
		return tgtProducer;
	}

	/**
	 * Returns the history.
	 * 
	 * @return The history.
	 */
	public JFSHistory getHistory() {
		return history;
	}

	/**
	 * Shut downs the file producers of the comparison object when not needed
	 * any more (e.g., before creating a new one for the directory pair).
	 */
	public final void shutDownProducers() {
		JFSFileProducerManager pm = JFSFileProducerManager.getInstance();
		pm.shutDownProducer(srcProducer.getUri());
		pm.shutDownProducer(tgtProducer.getUri());
	}

	/**
	 * @see JFSElement#setSrcFile(JFSFile)
	 */
	public final void setSrcFile(JFSFile file) {
	}

	/**
	 * @see JFSElement#setTgtFile(JFSFile)
	 */
	public final void setTgtFile(JFSFile file) {
	}

	/**
	 * @see JFSElement#setAction(JFSSyncMode.SyncAction)
	 */
	public final void setAction(SyncAction action) {
	}

	/**
	 * @see JFSElement#setActive(boolean)
	 */
	public void setActive(boolean isActive) {
	}
	
	
	/**
	 *  Explicitly implement that the extra fields don't change equality. 
	 */
	@Override
	public boolean equals(Object arg0) {
		return (arg0 != null) && arg0 instanceof JFSRootElement
				&& super.equals(arg0);
	}

	/**
	 *  Explicitly implement that the extra field don't change the hash code. 
	 */
	@Override
	public int hashCode() {
		return super.hashCode() + 23;
	}
}