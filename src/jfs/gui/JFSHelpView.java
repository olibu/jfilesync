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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jfs.conf.JFSConst;
import jfs.conf.JFSLog;
import jfs.conf.JFSText;

/**
 * This dialog is responsible for providing available help topics and their
 * content.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSHelpView.java,v 1.19 2013/06/19 09:36:27 heidrich Exp $
 */
public class JFSHelpView extends JDialog implements ActionListener,
		HyperlinkListener, ListSelectionListener {
	/** The UID. */
	private static final long serialVersionUID = 52L;

	/** The available help topics. */
	private JList<JFSHelpTopic> topicsList;

	/** The available help topics. */
	private TreeSet<JFSHelpTopic> topics = new TreeSet<JFSHelpTopic>();

	/** The editor pane with the content on html file. */
	private JEditorPane content;

	/** The backward history of viewed files. */
	private Vector<URL> bwdHistory = new Vector<URL>(JFSConst.HELP_HISTORY_SIZE);

	/** The forward history of viewed files. */
	private Vector<URL> fwdHistory = new Vector<URL>(JFSConst.HELP_HISTORY_SIZE);

	/** The backward button. */
	private JButton bwdButton;

	/** The forward button. */
	private JButton fwdButton;

	/**
	 * Initializes the help view.
	 * 
	 * @param frame
	 *            The main frame.
	 */
	public JFSHelpView(JFrame frame) {
		// This dialog is not modal:
		super(frame, false);

		// Get the translation object:
		JFSText t = JFSText.getInstance();

		// Create the dialog:
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(600, 500);
		setTitle(t.get("menu.helpTopics"));
		JFSSupport.center(frame, this);

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		// Create navigation bar:
		JToolBar bar = new JToolBar();
		bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
		bar.add(JFSSupport.getButton("jfs.icon.nav.home", "HOME", this,
				"help.nav.home"));
		bar.addSeparator();
		bwdButton = JFSSupport.getButton("jfs.icon.nav.bwd", "BWD", this,
				"help.nav.bwd");
		bwdButton.setEnabled(false);
		bar.add(bwdButton);
		fwdButton = JFSSupport.getButton("jfs.icon.nav.fwd", "FWD", this,
				"help.nav.fwd");
		fwdButton.setEnabled(false);
		bar.add(fwdButton);

		// Create help topic panel:
		JPanel topicPanel = new JPanel(new GridLayout(1, 1));
		topicPanel.setBorder(new TitledBorder(t.get("help.topics.title")));
		String[] helpTopics = JFSConst.getInstance().getStringArray(
				"jfs.help.topics");
		for (int i = 0; i < helpTopics.length; i++)
			topics.add(new JFSHelpTopic(helpTopics[i]));
		JFSHelpTopic[] topicsArray = new JFSHelpTopic[0];
		topicsList = new JList<JFSHelpTopic>(topics.toArray(topicsArray));
		topicsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		topicsList.addListSelectionListener(this);
		topicPanel.add(new JScrollPane(topicsList));

		// Create content area:
		content = new JEditorPane();
		content.setEditable(false);
		content.addHyperlinkListener(this);
		JFSConst c = JFSConst.getInstance();
		setContent(c.getResourceUrl(c.getString("jfs.help.startTopic")));
		JScrollPane scrollPane = new JScrollPane(content);

		// Create buttons in a separate panel:
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(JFSSupport.getButton("button.close", "button.close",
				this));

		// Add all panels:
		cp.add(bar, BorderLayout.NORTH);
		cp.add(topicPanel, BorderLayout.WEST);
		cp.add(scrollPane, BorderLayout.CENTER);
		cp.add(buttonPanel, BorderLayout.SOUTH);

		// Activate dialog:
		JFSSupport.center(frame, this);
		this.setVisible(true);
	}

	/**
	 * Sets the content of the editor pane.
	 * 
	 * @param url
	 *            The URL to view in the editor pane.
	 */
	private final void setContent(URL url) {
		// Get the filename from the translation object:
		JFSText t = JFSText.getInstance();

		try {
			content.setPage(url);
			// Update toc selection:
			topicsList.removeListSelectionListener(this);
			topicsList.clearSelection();
			for (JFSHelpTopic top : topics) {
				try {
					if (top.getUrl().toURI().equals(url.toURI()))
						topicsList.setSelectedValue(top, true);
				} catch (URISyntaxException e) {
					continue;
				}
			}
			topicsList.addListSelectionListener(this);
		} catch (IOException e) {
			JFSLog.getErr().getStream().println(t.get("error.io") + " " + url);
		}
	}

	/**
	 * Adds an URL to a history. If the URL to add is already the last element
	 * nothing is done, else the URL is added and if the size of the history is
	 * greater than the maximum size, the first element is removed.
	 * 
	 * @param v
	 *            The history to add to.
	 * @param url
	 *            The URL to add.
	 */
	private final static void addUrl(Vector<URL> v, URL url) {
		// If URL is already first stop:
		try {
			//performance: compare by URI, not URL. URL might do DNS lookups.
			if (v.size() > 0 && v.lastElement().toURI().equals(url.toURI()))
				return;
		} catch (URISyntaxException e) {
			return;
		}

		// Add new URL and cut vector:
		v.add(url);
		if (v.size() > JFSConst.HELP_HISTORY_SIZE)
			v.remove(0);
	}

	/**
	 * Updates the contents of the help view by adding the old URL to the
	 * history and setting the contents of the help view to the new URL if both
	 * URL differ. Furthermore, the buttons for moving forwards and backwards
	 * are disabled and enabled accordingly.
	 * 
	 * @param v
	 *            The history to which the old URL is added.
	 * @param newUrl
	 *            The new URL that is used to update the contents.
	 */
	private final void update(Vector<URL> v, URL newUrl) {
		// Update contents:
		URL oldUrl = content.getPage();
		try {
			if (!oldUrl.toURI().equals(newUrl.toURI())) {
				addUrl(v, content.getPage());
				setContent(newUrl);
			}
		} catch (URISyntaxException e) {
		}

		// Update button state:
		if (bwdHistory.isEmpty())
			bwdButton.setEnabled(false);
		else
			bwdButton.setEnabled(true);
		if (fwdHistory.isEmpty())
			fwdButton.setEnabled(false);
		else
			fwdButton.setEnabled(true);
	}

	/**
	 * Sets a new URL for the contents of the help view by updating the backward
	 * history and clearing the forward history if the new and old URL of the
	 * contents are not the same.
	 * 
	 * @param newUrl
	 *            The new URL that is used to update the contents.
	 */
	private final void setNewUrl(URL newUrl) {
		URL oldUrl = content.getPage();
		try {
			if (!oldUrl.toURI().equals(newUrl.toURI())) {
				fwdHistory.clear();
				update(bwdHistory, newUrl);
			}
		} catch (URISyntaxException e) {
			return;
		}
	}

	/**
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		String cmd = event.getActionCommand();

		if (cmd.equals("HOME")) {
			JFSConst c = JFSConst.getInstance();
			setNewUrl(c.getResourceUrl(c.getString("jfs.help.startTopic")));
		}

		if (cmd.equals("BWD")) {
			if (!bwdHistory.isEmpty()) {
				URL last = bwdHistory.remove(bwdHistory.size() - 1);
				update(fwdHistory, last);
			}
		}

		if (cmd.equals("FWD")) {
			if (!fwdHistory.isEmpty()) {
				URL last = fwdHistory.remove(fwdHistory.size() - 1);
				update(bwdHistory, last);
			}
		}

		if (cmd.equals("button.close")) {
			setVisible(false);
		}
	}

	/**
	 * @see HyperlinkListener#hyperlinkUpdate(HyperlinkEvent)
	 */
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			setNewUrl(e.getURL());
		}
	}

	/**
	 * @see ListSelectionListener#valueChanged(ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (e.getFirstIndex() != -1 && e.getLastIndex() != -1
				&& !e.getValueIsAdjusting()) {
			// Display the content of the current topic:
			JFSHelpTopic topic = (JFSHelpTopic) topicsList.getSelectedValue();
			if (topic != null) {
				setNewUrl(topic.getUrl());
			}
		}
	}
}