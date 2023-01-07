package jfs.gui;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jfs.conf.JFSText;
import jfs.conf.JFSFilter.FilterRange;
import jfs.conf.JFSFilter.FilterType;

public class JFSConfigFilterAddDialog extends JPanel {

	private static final long serialVersionUID = -5662583431118019806L;
	
	private JComboBox<String> typeCombo;
	private JComboBox<String> rangeCombo;
	private JTextField filterText;

	public JFSConfigFilterAddDialog() {
		this("", FilterType.NAME, FilterRange.ALL);
	}
	
	public JFSConfigFilterAddDialog(String filter, FilterType relativePath,
			FilterRange filterRange) {
		super();

		JFSText t = JFSText.getInstance();
		JPanel row1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel row2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel row3Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		filterText = new JTextField();
		filterText.setColumns(20);
		filterText.setText(filter);
		row1Panel.add(new JLabel(t.get("profile.filter.add.regexp")));
		row1Panel.add(filterText);

		typeCombo = new JComboBox<String>();
		for (FilterType fType : FilterType.values()) {
			typeCombo.addItem(t.get(fType.getName()));
		}
		typeCombo.setSelectedItem(t.get(relativePath.getName()));
		row2Panel.add(new JLabel(t.get("profile.filter.add.type")));
		row2Panel.add(typeCombo);

		rangeCombo = new JComboBox<String>();
		for (FilterRange fRange : FilterRange.values()) {
			rangeCombo.addItem(t.get(fRange.getName()));
		}
		rangeCombo.setSelectedItem(t.get(filterRange.getName()));
		row3Panel.add(new JLabel(t.get("profile.filter.add.range")));
		row3Panel.add(rangeCombo);

		this.setLayout(new GridLayout(3, 1));
		this.add(row1Panel);
		this.add(row2Panel);
		this.add(row3Panel);	}

	public String getFilterText() {
		return filterText.getText();
	}

	public FilterRange getFilterRange() {
		for (FilterRange fRange : FilterRange.values()) {
			if (rangeCombo.getSelectedItem().equals(JFSText.getInstance().get(fRange.getName()))) {
				return fRange;
			}
		}
		return null;
	}

	public FilterType getFilterType() {
		for (FilterType fType : FilterType.values()) {
			if (typeCombo.getSelectedItem().equals(JFSText.getInstance().get(fType.getName()))) {
				return fType;
			}
		}
		return null;
	}
}
