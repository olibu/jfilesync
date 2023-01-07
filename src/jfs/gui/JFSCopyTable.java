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

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import jfs.conf.JFSText;
import jfs.sync.JFSCopyStatement;
import jfs.sync.JFSFile;

/**
 * This class is responsible for displaying the comparison table.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSCopyTable.java,v 1.12 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSCopyTable extends AbstractTableModel {
	/** The UID. */
	private static final long serialVersionUID = 46L;

	/** The object with information that should be displayed by the table. */
	private Vector<JFSCopyStatement> v;

	/** The corresponding JTable. */
	private final JTable table;

	/**
	 * The default constructor just performs some initialization work.
	 * 
	 * @param v
	 *            The vector with all copy statements.
	 */
	public JFSCopyTable(Vector<JFSCopyStatement> v) {
		this.v = v;

		// Create column model:
		JFSText t = JFSText.getInstance();
		DefaultTableColumnModel cm = new DefaultTableColumnModel();

		TableColumn source = new TableColumn(0, 100);
		source.setHeaderValue(t.get("report.table.src"));
		cm.addColumn(source);

		TableColumn target = new TableColumn(1, 100);
		target.setHeaderValue(t.get("report.table.tgt"));
		cm.addColumn(target);

		// Create table:
		table = new JTable(this, cm);
		table.getTableHeader().setReorderingAllowed(false);
	}

	/**
	 * @see TableModel#getRowCount()
	 */
	public final int getRowCount() {
		return v.size();
	}

	/**
	 * @see TableModel#getColumnCount()
	 */
	public final int getColumnCount() {
		return 2;
	}

	/**
	 * @see TableModel#getValueAt(int, int)
	 */
	public final Object getValueAt(int row, int column) {
		if (row < 0 || row >= getRowCount() || column < 0
				|| column >= getColumnCount())
			return null;

		JFSFile file;
		switch (column) {
		case 0:
			file = v.get(row).getSrc();
			break;
		default:
			file = v.get(row).getTgt();
			break;
		}

		if (file != null)
			return file.getPath();
		else
			return "";
	}

	/**
	 * @see TableModel#getColumnClass(int)
	 */
	public final Class<?> getColumnClass(int column) {
		return String.class;
	}

	/**
	 * @return Returns the JTable object.
	 */
	public final JTable getJTable() {
		return table;
	}
}