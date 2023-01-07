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

package jfs.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import jfs.conf.JFSSyncModes;
import jfs.conf.JFSSyncMode.SyncAction;
import jfs.sync.JFSElement;
import jfs.sync.JFSTable;
import jfs.sync.JFSElement.ElementState;

/**
 * This class is responsible for handling actions of the synchronization table.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSTableListener.java,v 1.4 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSTableListener implements MouseListener, ActionListener {
	/** The parent frame. */
	private final Frame parent;

	/** The corresponding JTable. */
	private final JTable table;

	/** Stores the names for the icons used for displaying actions. */
	private HashMap<SyncAction, String> actionIconNames;

	/** Stores the current selection of JFS elements in the table. */
	private Vector<JFSElement> currentSelection = new Vector<JFSElement>();

	/**
	 * The default constructor just performs some initialization work.
	 * 
	 * @param parent
	 *            The parent frame.
	 * @param table
	 *            The table to listen to.
	 * @param actionIconNames
	 *            The action icon names.
	 */
	public JFSTableListener(Frame parent, JTable table,
			HashMap<SyncAction, String> actionIconNames) {
		this.parent = parent;
		this.table = table;
		this.actionIconNames = actionIconNames;
	}

	/**
	 * @see MouseListener#mouseClicked(MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		handlePopupMenu(e);
	}

	/**
	 * @see MouseListener#mouseEntered(MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * @see MouseListener#mouseExited(MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * @see MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		handlePopupMenu(e);
	}

	/**
	 * @see MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		handlePopupMenu(e);
	}

	/**
	 * Creates a popup menu for a certain mouse event on the table.
	 * 
	 * @param e
	 *            The mouse event to deal with.
	 */
	private void handlePopupMenu(MouseEvent e) {
		if (e.isPopupTrigger() && !table.getSelectionModel().isSelectionEmpty()) {
			// Updates the selection of JFS elements:
			updateSelection();

			// Go through selection and add actions that are common for all
			// selected JFS elements:
			TreeMap<SyncAction, Integer> validActions = new TreeMap<SyncAction, Integer>();
			for (JFSElement element : currentSelection) {
				// Skip pop-up if root element is part of selection:
				if (element.getState() == ElementState.IS_ROOT)
					return;

				for (SyncAction a : element.getValidActions()) {
					Integer i = validActions.get(a);
					if (i != null) {
						validActions.put(a, i + 1);
					} else {
						validActions.put(a, 1);
					}
				}
			}

			// Create menu:
			JPopupMenu popup = new JPopupMenu();
			popup.add(JFSSupport.getMenuItem("compTable.menu.activate",
					"ACTIVATE", this));
			popup.add(JFSSupport.getMenuItem("compTable.menu.deactivate",
					"DEACTIVATE", this));
			popup.addSeparator();

			// If the number of appearances in the hash map is equal to
			// the size of the selection, the action is valid:
			for (Entry<SyncAction, Integer> entry : validActions.entrySet()) {
				if (entry.getValue() == currentSelection.size()) {
					popup.add(JFSSupport.getMenuItem(entry.getKey().getName(),
							String.valueOf(entry.getKey()), this,
							actionIconNames.get(entry.getKey())));
				}
			}
			if (currentSelection.size() >= 1) {
				popup.addSeparator();
				popup.add(JFSSupport.getMenuItem("general.reset",
						"general.reset", this));
			}

			// Add properties field, if only one element is selected:
			if (currentSelection.size() == 1) {
				popup.addSeparator();
				popup.add(JFSSupport.getMenuItem("fileProps.title",
						"PROPERTIES", this));
			}

			// Show menu:
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (table.getSelectionModel().isSelectionEmpty())
			return;

		String cmd = e.getActionCommand();

		for (JFSElement element : currentSelection) {
			// Set whether the action of the element is active:
			if (cmd.equals("ACTIVATE")) {
				element.setActive(true);
			}
			if (cmd.equals("DEACTIVATE")) {
				element.setActive(false);
			}

			// Change action based on command string:
			if (cmd.equals("general.reset")) {
				element.setActive(true);
				element.setManuallySetAction(false);
				JFSSyncModes.getInstance().getCurrentMode().computeAction(
						element);
			}
			for (SyncAction a : SyncAction.values()) {
				if (cmd.equals(String.valueOf(a))) {
					element.setAction(a);
					element.setManuallySetAction(true);
				}
			}

			// Show properties for selected element:
			if (cmd.equals("PROPERTIES")) {
				new JFSPropertiesView(parent, element);
			}
		}

		table.updateUI();
	}

	/**
	 * Updates the vector of selected JFS elements based on the selection model.
	 */
	private void updateSelection() {
		ListSelectionModel sm = table.getSelectionModel();
		currentSelection.clear();
		for (int i = sm.getMinSelectionIndex(); i <= sm.getMaxSelectionIndex(); i++) {
			if (sm.isSelectedIndex(i)) {
				currentSelection.add(JFSTable.getInstance().getViewElement(i));
			}
		}
	}
}