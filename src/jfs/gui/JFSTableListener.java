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

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import jfs.conf.JFSConfig;
import jfs.conf.JFSFilter;
import jfs.conf.JFSLog;
import jfs.conf.JFSSettings;
import jfs.conf.JFSSyncModes;
import jfs.conf.JFSText;
import jfs.conf.JFSFilter.FilterRange;
import jfs.conf.JFSFilter.FilterType;
import jfs.conf.JFSSyncMode.SyncAction;
import jfs.sync.JFSElement;
import jfs.sync.JFSTable;
import jfs.sync.JFSElement.ElementState;
import jlibdiff.DiffDialog;

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

			// Add compare element if a program is defined.
			if (currentSelection.size() == 1) {
				String compareProg = JFSSettings.getInstance().getCompareProgram(); 
				if (compareProg != null) {
					popup.addSeparator();
					popup.add(JFSSupport.getMenuItem("compTable.menu.compare",
							"COMPARE", this));
				}
			}

			// Add elements for ignoring selected entry
			if (currentSelection.size() == 1) {
				JMenu addIgnore = new JMenu(JFSText.getInstance().get("compTable.menu.addIgnore"));
				popup.add(addIgnore);
				addIgnore.add(JFSSupport.getMenuItem("compTable.menu.addIgnore.byName", "IGNORE_BY_NAME", this));
				addIgnore.add(JFSSupport.getMenuItem("compTable.menu.addIgnore.byRelPath", "IGNORE_BY_RELPATH", this));
				addIgnore.add(JFSSupport.getMenuItem("compTable.menu.addIgnore.bySrcPath", "IGNORE_BY_SRCPATH", this));
				addIgnore.add(JFSSupport.getMenuItem("compTable.menu.addIgnore.byTrgPath", "IGNORE_BY_TRGPATH", this));
				addIgnore.add(JFSSupport.getMenuItem("compTable.menu.addIgnore.byDialog", "IGNORE_BY_DIALOG", this));
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

			// execute the compare program
			if (cmd.equals("COMPARE")) {
				String compareProg = JFSSettings.getInstance().getCompareProgram(); 
				if (compareProg != null)
				{
					// replace placeholders
					String source = element.getSrcFile().getFile().getAbsolutePath();
					String target = element.getTgtFile().getFile().getAbsolutePath();
					if ("internal".equals(compareProg))
					{
					    showCompareDialog(source, target);
					}
					else
					{
    					String command = replaceString(compareProg, "%1", source);
    					command = replaceString(command, "%2", target);
    					String[] param = new String[]{source, target};
    					JFSLog.getOut().getStream().println("CompareExec: " + command);
    					try {
    						Process proc = Runtime.getRuntime().exec(command, param);
    						if (proc.exitValue()!=0)
    						{
    						    throw new Exception("Error while executing compare program.");
    						}
    					} catch (Exception e1) {
    						JFSLog.getErr().getStream().println(JFSText.getInstance().get("error.callCompareProgram") + ": " + command);
    					}
					}
				}
			}

			if (cmd.startsWith("IGNORE_BY")) {
				JFSConfig config = JFSConfig.getInstance();
				JFSFilter filter = new JFSFilter(null);
				if (element.isDirectory())
				{
					filter.setRange(FilterRange.DIRECTORIES);
				}
				else
				{
					filter.setRange(FilterRange.FILES);
				}
				if (cmd.equals("IGNORE_BY_NAME"))
				{
					filter.setFilter(element.getName());
					filter.setType(JFSFilter.FilterType.NAME);
				}
				else if (cmd.equals("IGNORE_BY_RELPATH"))
				{
					String path = element.getRelativePath();
					path = replaceString(path, "\\", "\\\\");
					filter.setFilter(path);
					filter.setType(JFSFilter.FilterType.RELATIVE_PATH);
				}
				else if (cmd.equals("IGNORE_BY_SRCPATH"))
				{
					String path = element.getSrcFile().getPath();
					path = replaceString(path, "\\", "\\\\");
					filter.setFilter(path);
					filter.setType(JFSFilter.FilterType.PATH);
				}
				else if (cmd.equals("IGNORE_BY_TRGPATH"))
				{
					String path = element.getTgtFile().getPath();
					path = replaceString(path, "\\", "\\\\");
					filter.setFilter(path);
					filter.setType(JFSFilter.FilterType.PATH);
				}
				else if (cmd.equals("IGNORE_BY_DIALOG"))
				{
					// show Dialog to set template
					String path = element.getRelativePath();
					path = replaceString(path, "\\", "\\\\");
					filter.setFilter(path);
					filter.setType(JFSFilter.FilterType.RELATIVE_PATH);
					filter = showFilterDialog(filter);
					
				}
				if (filter != null)
				{
					config.addExclude(filter);
				}
			}

		}

		table.updateUI();
	}
	
	private void showCompareDialog(String source, String target)
    {
	    // read files
	    try
        {
            BufferedReader inSource = new BufferedReader(new FileReader(new File(source)));
            StringBuffer sbSource = new StringBuffer();
            String line;
            while ((line=inSource.readLine())!=null)
            {
                sbSource.append(line).append('\n');
            }
            
            BufferedReader inTarget = new BufferedReader(new FileReader(new File(target)));
            StringBuffer sbTarget = new StringBuffer();
            while ((line=inTarget.readLine())!=null)
            {
                sbTarget.append(line).append('\n');
            }

            // show dialog
            final JFSSettings s = JFSSettings.getInstance();
            final DiffDialog dlg = new DiffDialog(parent, "Compare Dialog");
            dlg.setBounds(s.getDiffDialogX(), s.getDiffDialogY(), s
                    .getDiffDialogWidth(), s.getDiffDialogHeight());
            dlg.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e) {
                    Rectangle r = dlg.getBounds();
                    s.setDiffDialogBounds(r.x, r.y, r.width, r.height);
                }
            });
            dlg.setLeftString(sbSource.toString());
            dlg.setRightString(sbTarget.toString());
            dlg.setVisible(true);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private JFSFilter showFilterDialog(JFSFilter filter)
	{
		JFSText t = JFSText.getInstance();
		JPanel row1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel row2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel row3Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JTextField filterText = new JTextField(filter.getFilter());
		filterText.setColumns(20);
		row1Panel.add(new JLabel(t.get("profile.filter.add.regexp")));
		row1Panel.add(filterText);

		JComboBox typeCombo = new JComboBox();
		for (FilterType fType : FilterType.values()) {
			typeCombo.addItem(t.get(fType.getName()));
		}
		typeCombo.setSelectedItem(t.get(filter.getType().getName()));
		row2Panel.add(new JLabel(t.get("profile.filter.add.type")));
		row2Panel.add(typeCombo);

		JComboBox rangeCombo = new JComboBox();
		for (FilterRange fRange : FilterRange.values()) {
			rangeCombo.addItem(t.get(fRange.getName()));
		}
		rangeCombo.setSelectedItem(t.get(filter.getRange().getName()));
		row3Panel.add(new JLabel(t.get("profile.filter.add.range")));
		row3Panel.add(rangeCombo);

		JPanel panel = new JPanel(new GridLayout(3, 1));
		panel.add(row1Panel);
		panel.add(row2Panel);
		panel.add(row3Panel);

		int result = JOptionPane.showConfirmDialog(parent, panel, t
				.get("profile.filter.add.title"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.INFORMATION_MESSAGE);

		// If not canceled, add filter:
		if (result == JOptionPane.OK_OPTION) {
			JFSFilter f = new JFSFilter(filterText.getText());
			for (FilterType fType : FilterType.values()) {
				if (typeCombo.getSelectedItem().equals(
						t.get(fType.getName()))) {
					f.setType(fType);
				}
			}
			for (FilterRange fRange : FilterRange.values()) {
				if (rangeCombo.getSelectedItem().equals(
						t.get(fRange.getName()))) {
					f.setRange(fRange);
				}
			}
			return f;
		}
		else
		{
			return null;
		}
		
	}

	private String replaceString(String pattern, String what, String with)
	{
		String result = pattern;
		int length = what.length();
		int i;
		int startPos = 0;
		while ((i = result.indexOf(what, startPos))!=-1)
		{
			result = result.substring(0, i) + with + result.substring(i+length);
			startPos = i + with.length();
		}
		
		return result;
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