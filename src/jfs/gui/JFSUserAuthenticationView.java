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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import jfs.conf.JFSText;
import jfs.sync.JFSUserAuthentication;
import jfs.sync.JFSUserAuthenticationInterface;

/**
 * Asks for the user name and password using the GUI.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSUserAuthenticationView.java,v 1.1 2008/06/11 12:10:58 heidrich Exp $
 */
public class JFSUserAuthenticationView implements
		JFSUserAuthenticationInterface, ActionListener {
	/** The main view. */
	private JFSMainView mainView;

	/** The dialog. */
	private JDialog dialog;

	/** The resource label. */
	private JLabel resourceLabel = new JLabel();

	/** The user name label. */
	private JLabel userNameLabel = new JLabel();

	/** The password label. */
	private JLabel passwordLabel = new JLabel();

	/** The user name. */
	private JTextField userName = new JTextField();

	/** The password. */
	private JPasswordField password = new JPasswordField();

	/**
	 * Initializes the question view.
	 * 
	 * @param mainView
	 *            The main frame.
	 */
	public JFSUserAuthenticationView(JFSMainView mainView) {
		this.mainView = mainView;

		// Get the translation object:
		JFSText t = JFSText.getInstance();

		// Create the modal dialog:
		dialog = new JDialog(mainView.getFrame(), true);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setResizable(false);
		dialog.setTitle(t.get("userAuthenticationView.title"));

		Container cp = dialog.getContentPane();
		cp.setLayout(new BorderLayout());

		// Adapt labels and assign to panels:
		JPanel helpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		helpPanel.add(new JLabel(t.get("userAuthenticationView.resource")));

		JPanel resourcePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		resourcePanel.add(resourceLabel);

		JPanel userNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		userNamePanel.add(userNameLabel);
		userNamePanel.add(userName);
		userNameLabel.setText(t.get("userAuthenticationView.userName"));
		userName.setColumns(20);

		JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		passwordPanel.add(passwordLabel);
		passwordPanel.add(password);
		passwordLabel.setText(t.get("userAuthenticationView.password"));
		password.setColumns(20);

		JPanel optionsPanel = new JPanel(new GridLayout(4, 1));
		optionsPanel
				.setBorder(new TitledBorder(t.get("profile.option.heading")));
		optionsPanel.add(helpPanel);
		optionsPanel.add(resourcePanel);
		optionsPanel.add(userNamePanel);
		optionsPanel.add(passwordPanel);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(JFSSupport.getButton("button.close", "close", this));

		cp.add(optionsPanel, BorderLayout.NORTH);
		cp.add(buttonPanel, BorderLayout.SOUTH);
	}

	/**
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		String cmd = event.getActionCommand();

		if (cmd.equals("close")) {
			dialog.setVisible(false);
			mainView.update();
		}
	}

	/**
	 * @see JFSUserAuthenticationInterface#ask(JFSUserAuthentication)
	 */
	public void ask(JFSUserAuthentication auth) {
		userName.setText(auth.getUriUserName());
		password.setText(auth.getUriPassword());

		// Adapt fields and pack dialog:
		resourceLabel.setText(auth.getResource());

		// Make dialog window visible:
		dialog.pack();
		JFSSupport.center(mainView.getFrame(), dialog);
		dialog.setVisible(true);
	}

	/**
	 * @see JFSUserAuthenticationInterface#getUserName()
	 */
	public String getUserName() {
		if (!userName.getText().trim().equals("")) {
			return userName.getText();
		} else {
			return "anonymous";
		}
	}

	/**
	 * @see JFSUserAuthenticationInterface#getPassword()
	 */
	public String getPassword() {
		return new String(password.getPassword());
	}
}