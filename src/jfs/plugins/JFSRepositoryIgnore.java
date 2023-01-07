/*
 * JFileSync
 * Copyright (C) 2002-2007, Jens Heidrich
 * 
 * JFSPluginRepository plug-in
 * Copyright (C) 2009, Jan Rieke
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
 
package jfs.plugins;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jfs.conf.JFSText;
import jfs.conf.JFSSyncMode.SyncAction;
import jfs.sync.JFSElement;
import jfs.sync.JFSProgress;
import jfs.sync.JFSTable;

/**
 * This plug-in traverses the comparison tree and sorts out every directory
 * that is a CVS or SVN repository checkout.
 * 
 * @author Jan Rieke
 * @version $Id:
 *          Exp $
 */
public class JFSRepositoryIgnore implements JFSPlugin {

	/** HashSet for all file names that filter. */
	private HashMap<String, String> filter = new HashMap<String, String>();

	/** Vector storing all read only files! */
	private Vector<String> repositoryDirectories = new Vector<String>();

	/**
	 * @see JFSPlugin#getId()
	 */
	public String getId() {
		return "plugin.repignore.name";
	}

	/**
	 * @see JFSPlugin#init(JFrame)
	 */
	public void init(JFrame frame) {
		JFSText t = JFSText.getInstance();
		repositoryDirectories.clear();
		
		if (filter.size() == 0){
			filter.put(".svn", "entries");
			filter.put("CVS", "Root");
		}

		// Create main panel:
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1));

		JLabel desciption = new JLabel(t.get("plugin.repignore.description"));
		panel.add(desciption);

		int result = JOptionPane.showConfirmDialog(frame, panel,
				t.get(getId()), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);

		if (result == JOptionPane.OK_OPTION) {
			removeRepositories();

			// View result:
			JList<String> list = new JList<String>(repositoryDirectories);
			JScrollPane pane = new JScrollPane(list);
			JOptionPane.showMessageDialog(frame, pane, t
					.get("plugin.repignore.result.title"),
					JOptionPane.INFORMATION_MESSAGE);
			repositoryDirectories.clear();
		}
	}

	/**
	 * Checks whether there is a subdirectory named "CVS" or ".svn" and 
	 * ignores all files in this case.
	 */
	public void removeRepositories() {
		JFSProgress progress = JFSProgress.getInstance();
		JFSTable table = JFSTable.getInstance();
		
		for (int i = 0; i < table.getRootsSize(); i++) {
			traverse(table.getRootElement(i), progress);
		}
	}
	
	/**
	 * Modifies a given file object and traverses the whole file system tree
	 * structure.
	 * 
	 * @param element
	 *            The file to modify.
	 * @param progress 
	 */
	protected void traverse(JFSElement element, JFSProgress progress) {
		assert element.isDirectory();

		Vector<JFSElement> files = element.getChildren();
		if (files == null)
			return;
		
		//progress.start();
		
		boolean ignore = false;

		for (JFSElement file : files) {
			if (file.isDirectory() && filter.containsKey(file.getName())) {
				String childfilename = filter.get(file.getName());
				Vector<JFSElement> children = file.getChildren();
				if (children != null) {
					for (JFSElement child : file.getChildren()) {
						if (child.getName().equalsIgnoreCase(childfilename)) {
							ignore = true;
							break;
						}
					}
				}
			}
		}
		if (ignore) {
			element.setAction(SyncAction.NOP);
			element.setManuallySetAction(true);
			repositoryDirectories.add(element.getRelativePath());
			ignoreAllChildren(element);
		} else {
			for (JFSElement file : files) {
				traverse(file, progress);
			}
		}
	}
	
	protected void ignoreAllChildren(JFSElement element) {
		assert element.isDirectory();

		Vector<JFSElement> files = element.getChildren();
		if (files == null)
			return;
		for (JFSElement file : files) {
			file.setAction(SyncAction.NOP);
			file.setManuallySetAction(true);
			if (file.isDirectory()) {
				//repositoryDirectories.add(file.getRelativePath());
				ignoreAllChildren(file);
			}
		}
	}
	
}