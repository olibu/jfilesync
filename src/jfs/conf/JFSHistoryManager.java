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

package jfs.conf;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import jfs.sync.JFSElement;
import jfs.sync.JFSRootElement;
import jfs.sync.JFSTable;
import jfs.sync.JFSElement.ElementState;

/**
 * Manager all histories of synchronized directory pairs.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSHistoryManager.java,v 1.9 2009/10/08 08:19:53 heidrich Exp $
 */
public class JFSHistoryManager {
	/** Stores the only instance of the class. */
	private static JFSHistoryManager instance = null;

	/** The stored histories. */
	private Vector<JFSHistory> histories = new Vector<JFSHistory>();

	/**
	 * Constructs a the only history manager.
	 */
	private JFSHistoryManager() {
	}

	/**
	 * Returns the reference of the only instance.
	 * 
	 * @return The only instance.
	 */
	public final static JFSHistoryManager getInstance() {
		if (instance == null)
			instance = new JFSHistoryManager();

		return instance;
	}

	/**
	 * Sorts all histories stored.
	 */
	public final void sortHistories() {
		Collections.sort(histories);
	}

	/**
	 * Returns the set of histories.
	 * 
	 * @return The histories.
	 */
	public final Vector<JFSHistory> getHistories() {
		return histories;
	}

	/**
	 * Returns the history for a special directory pair. If no history is found,
	 * a new one is created and added to the list of histories.
	 * 
	 * @param pair
	 *            The directory pair to search the history for.
	 * @return The history.
	 */
	public final JFSHistory getHistory(JFSDirectoryPair pair) {
		for (JFSHistory h : histories)
			if (h.getPair().equals(pair))
				return h;
		JFSHistory h = new JFSHistoryXML();
		h.setPair(pair);
		histories.add(h);
		return h;
	}

	/**
	 * Adds a special history.
	 * 
	 * @param history
	 *            The history to add.
	 */
	public final void addHistory(JFSHistory history) {
		histories.add(history);
	}

	/**
	 * Deletes the history for a special history; that is, deletes the file, the
	 * history is stored to and removes the history from the list of histories.
	 * 
	 * @param history
	 *            The history to delete.
	 */
	public final void deleteHistory(JFSHistory history) {
		try {
			deleteHistoryFile(history);
		} catch (IOException e) {
		}
		history.clear();
		histories.remove(history);
	}

	/**
	 * Deletes all histories.
	 * 
	 * @see #deleteHistory(JFSHistory)
	 */
	public final void deleteAll() {
		for (JFSHistory h : histories) {
			try {
				deleteHistoryFile(h);
			} catch (IOException e) {
				continue;
			}
			h.clear();
		}
		histories.clear();
	}

	/**
	 * Deletes the history file for a special history.
	 * 
	 * @param history
	 *            The history to delete the corresponding file for.
	 * @throws IOException 
	 */
	private final void deleteHistoryFile(JFSHistory history) throws IOException {
		File file = new File(JFSConst.HOME_DIR + File.separatorChar
				+ history.getFileName());
		if (file != null && file.exists()) {
			if (!file.delete())
				throw new IOException("Unable to delete file: " + file.getAbsolutePath());
		}
	}

	/**
	 * Cleans the JFS configuration directory by cleaning all history files that
	 * are not referenced in the history manager.
	 * @throws IOException 
	 */
	public final void cleanHistories() throws IOException {
		Vector<String> historyFiles = new Vector<String>();
		for (JFSHistory h : getHistories()) {
			historyFiles.add(h.getFileName());
		}

		File jfsDir = new File(JFSConst.HOME_DIR);
		for (File f : jfsDir.listFiles()) {
			String name = f.getName();
			if (name.startsWith(JFSConst.HISTORY_FILE_PREFIX)
					&& name.endsWith(".xml") && !historyFiles.contains(name)) {
				if (!f.delete())
					throw new IOException("Unable to delete file: " + f.getAbsolutePath());
			}
		}
	}

	/**
	 * Updates and stores the currently managed histories with the
	 * synchronization table. If the element's files are equal (identified by
	 * their equal time stamp), an existing history item has to be updated or a
	 * new one has to be created. If a history item was found (and updated) or a
	 * new one was created, it is added to the new history. The relative path is
	 * stored as well as the last modified date and the length. That means, if
	 * the JFS element's files are not equal, the previous history item is kept
	 * as is. The latter can be caused by an interrupted or failed
	 * synchronization process or if JFS elements are deactivated before
	 * synchronization.
	 */
	public void updateHistories() {
		JFSTable table = JFSTable.getInstance();

		JFSHistory h = null;
		JFSRootElement root = null;
		Vector<JFSHistoryItem> newHistory = null;
		HashMap<String, JFSHistoryItem> newDirectories = null;
		HashMap<String, JFSHistoryItem> newFiles = null;

		for (int i = 0; i < table.getTableSize(); i++) {
			JFSElement element = table.getTableElement(i);

			// If root is found, update previous history and get new one:
			if (element.getState() == ElementState.IS_ROOT) {
				if (root != null && h != null)
					h.update(root, newHistory, newDirectories, newFiles);

				root = (JFSRootElement) element;
				h = root.getHistory();
				h.setDate(System.currentTimeMillis());
				newHistory = new Vector<JFSHistoryItem>();
				newDirectories = new HashMap<String, JFSHistoryItem>();
				newFiles = new HashMap<String, JFSHistoryItem>();
			}

			JFSHistoryItem item = h.getHistory(element);

			// If the element's files are equal, an existing item has to be
			// updated or a new one has to be created:
			if (element.getState() == ElementState.EQUAL) {
				// Use the source file (which exists if both files are equal)
				// to construct the new history item:
				if (item == null) {
					item = new JFSHistoryItem(element.getRelativePath());
					item.setDirectory(element.isDirectory());
				}
				item.setLastModified(element.getSrcFile().getLastModified());
				item.setLength(element.getSrcFile().getLength());
			}

			// If a history item was found or a new one was created, add this
			// to the new history:
			if (item != null) {
				newHistory.add(item);
				if (item.isDirectory()) {
					newDirectories.put(item.getRelativePath(), item);
				} else {
					newFiles.put(item.getRelativePath(), item);
				}
			}
		}

		// Update last history read:
		if (root != null && h != null)
			h.update(root, newHistory, newDirectories, newFiles);
	}
}