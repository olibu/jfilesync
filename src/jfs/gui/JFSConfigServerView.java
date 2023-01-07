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

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import jfs.conf.JFSConfig;
import jfs.conf.JFSText;

/**
 * This dialog manages server settings.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSConfigServerView.java,v 1.10 2006/01/05 18:27:25 heidrich
 *          Exp $
 */
public class JFSConfigServerView extends JDialog implements ActionListener {
	/** The UID. */
	private static final long serialVersionUID = 49L;

	/** The configuration object to modify. */
	private final JFSConfig config;

	/** The base directory. */
	private JTextField base;

	/** The server passphrase. */
	private JPasswordField passphrase;

	/** The server port. */
	private SpinnerNumberModel port;

	/** The server timeout. */
	private SpinnerNumberModel timeout;

	/** The server shut down. */
	private JCheckBox shutDown;

	/**
	 * Initializes the config view.
	 * 
	 * @param dialog
	 *            The main frame.
	 * @param config
	 *            The configuration to change.
	 */
	public JFSConfigServerView(JDialog dialog, JFSConfig config) {
		super(dialog, true);
		this.config = config;

		// Get the translation object:
		JFSText t = JFSText.getInstance();

		// Create the modal dialog:
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle(t.get("profile.server.title"));
		setResizable(false);

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		// Create options panel:
		JLabel baseLabel = new JLabel(t.get("profile.server.baseDir"));
		base = new JTextField(config.getServerBase(), 20);

		JLabel passphraseLabel = new JLabel(t.get("profile.server.passPhrase"));
		passphrase = new JPasswordField(config.getServerPassPhrase(), 15);

		JLabel portLabel = new JLabel(t.get("profile.server.port"));
		port = new SpinnerNumberModel(config.getServerPort(), 0, 1000000, 1);
		JSpinner portSpinner = new JSpinner(port);

		JLabel timeoutLabel = new JLabel(t.get("profile.server.timeout"));
		timeout = new SpinnerNumberModel(config.getServerTimeout(), 0, 3600000,
				1000);
		JSpinner timeoutSpinner = new JSpinner(timeout);

		shutDown = new JCheckBox(t.get("profile.server.shutdown"), config
				.getServerShutDown());

		JPanel row1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		row1Panel.add(baseLabel);
		row1Panel.add(base);
		row1Panel.add(JFSSupport.getButton("profile.server.baseDir.browse",
				"BROWSE", this));

		JPanel row2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		row2Panel.add(passphraseLabel);
		row2Panel.add(passphrase);

		JPanel row3Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		row3Panel.add(portLabel);
		row3Panel.add(portSpinner);

		JPanel row4Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		row4Panel.add(timeoutLabel);
		row4Panel.add(timeoutSpinner);

		JPanel row5Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		row5Panel.add(shutDown);

		JPanel optionsPanel = new JPanel(new GridLayout(5, 1));
		optionsPanel
				.setBorder(new TitledBorder(t.get("profile.option.heading")));
		optionsPanel.add(row1Panel);
		optionsPanel.add(row2Panel);
		optionsPanel.add(row3Panel);
		optionsPanel.add(row4Panel);
		optionsPanel.add(row5Panel);

		// Create buttons in a separate panel:
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(JFSSupport.getButton("button.ok", "button.ok", this));
		buttonPanel.add(JFSSupport.getButton("button.cancel", "button.cancel",
				this));

		// Add all panels:
		cp.add(optionsPanel, BorderLayout.NORTH);
		cp.add(buttonPanel, BorderLayout.SOUTH);

		// Pack and activate dialog:
		pack();
		JFSSupport.center(dialog, this);
		this.setVisible(true);
	}

	/**
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		JFSText t = JFSText.getInstance();
		String cmd = event.getActionCommand();

		if (cmd.equals("button.cancel") || cmd.equals("button.ok")) {
			setVisible(false);
			dispose();
		}

		if (cmd.equals("button.ok")) {
			config.setServerPort(port.getNumber().intValue());
			config.setServerTimeout(timeout.getNumber().intValue());
			config.setServerBase(base.getText());
			config
					.setServerPassPhrase(String.valueOf(passphrase
							.getPassword()));
			config.setServerShutDown(shutDown.isSelected());
		}

		if (cmd.equals("BROWSE")) {
			/*
            int returnVal;
			JFileChooser chooser = new JFileChooser(base.getText());
			chooser.setApproveButtonText(t.get("button.select"));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			// Get source file:
			chooser.setDialogTitle(t.get("profile.server.getBase"));
			returnVal = chooser.showOpenDialog(this);

			// If not canceled, get target file:
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				base.setText(chooser.getSelectedFile().getPath());
			}
			*/
            String selFile = UIHelper.showOpenDialog(this, base.getText(), true, t.get("profile.server.getBase"), t.getLocale(), null, UIHelper.TYPE_OPEN, t.get("button.select"));
            //chooser.setApproveButtonText(t.get("button.select"));

            // If not canceled, get target file:
            if (selFile != null) {
                base.setText(selFile);
            }
		}
	}
}