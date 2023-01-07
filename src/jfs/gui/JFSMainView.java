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
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import jfs.conf.JFSConfig;
import jfs.conf.JFSConfigObserver;
import jfs.conf.JFSConst;
import jfs.conf.JFSLog;
import jfs.conf.JFSLogObserver;
import jfs.conf.JFSSettings;
import jfs.conf.JFSSyncMode;
import jfs.conf.JFSSyncModes;
import jfs.conf.JFSText;
import jfs.conf.JFSViewMode;
import jfs.conf.JFSViewModes;
import jfs.plugins.JFSPlugin;
import jfs.plugins.JFSPluginRepository;
import jfs.server.JFSServer;
import jfs.server.JFSServerFactory;
import jfs.sync.JFSProgress;
import jfs.sync.JFSSynchronization;
import jfs.sync.JFSTable;

/**
 * This class represents the main Java Swing frame of the JFS application.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSMainView.java,v 1.39 2007/02/26 18:49:10 heidrich Exp $
 */
public class JFSMainView extends WindowAdapter implements ActionListener,
		ComponentListener, JFSConfigObserver, JFSLogObserver {

	/** Stores the corresponding frame. */
	private JFrame frame = null;

	/** The synchronization table itself in form of a JTable object. */
	private JTable syncTable;

	/** Group with all possible views of the shown files. */
	private ButtonGroup viewGroup;

	/** Group with all possible synchronization modes. */
	private ButtonGroup syncGroup;

	/** Stores the overall size state (displayed at the bottom of the frame). */
	private JLabel stateOverallSize;

	/** Stores the overall size state (displayed at the bottom of the frame). */
	private JLabel stateViewSize;

	/** Stores the overall size state (displayed at the bottom of the frame). */
	private JLabel stateDuration;

	/**
	 * Stores the overall synchronization mode (displayed at the bottom of the
	 * frame).
	 */
	private JLabel stateSyncMode;

	/** The check box indicating whether a server is active or not. */
	private JCheckBoxMenuItem serverItem;

	/** The toogle button indicating whether a server is active or not. */
	private JToggleButton serverButton;

	/** The last opened profiles. */
	private JMenuItem[] lastOpenedProfiles = new JMenuItem[JFSConst.LAST_OPENED_PROFILES_SIZE];

	/** Shown if an unread error is available in the error log. */
	private JPanel errorLog;

	/** The progress view. */
	private JFSProgressView progressView;

	/** The used help view. */
	private JFSHelpView helpView = null;

	/** The used assistant view. */
	private JFSAssistantView assistantView = null;

	/**
	 * Start the application with a certain task object.
	 * 
	 * @param loadDefaults
	 *            Determine whether the last loaded configuration should be
	 *            loaded at GUI startup.
	 */
	public JFSMainView(boolean loadDefaults) {
		JFSConfig config = JFSConfig.getInstance();

		// Load default configuration if option is specified:
		if (loadDefaults)
			config.loadDefaultFile();

		// Redirect error stream to log file:
		JFSLog.getErr().resetLogFile();
		JFSLog.getOut().resetLogFile();

		// Initialize attributes:
		frame = new JFrame();

		// Get translation object and set default locale:
		JFSText t = JFSText.getInstance();
		JComponent.setDefaultLocale(t.getLocale());

		// Start main window of application:
		frame.setTitle(t.get("general.appName") + " "
				+ JFSConst.getInstance().getString("jfs.version") + " - "
				+ config.getTitle());
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(this);
		frame.addWindowStateListener(this);
		frame.addComponentListener(this);

		// Add icon:
		ImageIcon jfsIcon = new ImageIcon(JFSConst.getInstance().getIconUrl(
				"jfs.icon.logo"));
		frame.setIconImage(jfsIcon.getImage());

		Container cp = frame.getContentPane();

		// Initialize JTable:
		JFSTableView jfsSyncTable = new JFSTableView(frame);
		syncTable = jfsSyncTable.getJTable();

		// Create main menus:
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu(t.get("menu.file"));
		JMenu viewMenu = new JMenu(t.get("menu.view"));
		JMenu modeMenu = new JMenu(t.get("menu.mode"));
		JMenu toolsMenu = new JMenu(t.get("menu.tools"));
		JMenu plugInsMenu = new JMenu(t.get("menu.plugIn"));
		JMenu helpMenu = new JMenu(t.get("menu.help"));

		// Create file menu:
		fileMenu.add(JFSSupport.getMenuItem("menu.new", "NEW", this,
				"jfs.icon.new"));
		fileMenu.add(JFSSupport.getMenuItem("menu.open", "OPEN", this,
				"jfs.icon.open"));
		fileMenu.add(JFSSupport.getMenuItem("menu.save", "SAVE", this,
				"jfs.icon.save"));
		fileMenu.add(JFSSupport.getMenuItem("menu.saveAs", "SAVE_AS", this,
				"jfs.icon.saveAs"));
		fileMenu.addSeparator();
		fileMenu.add(JFSSupport
				.getMenuItem("menu.reset", "general.reset", this));
		fileMenu.addSeparator();
		for (int i = 0; i < JFSConst.LAST_OPENED_PROFILES_SIZE; i++) {
			lastOpenedProfiles[i] = JFSSupport.getMenuItem("", "OPEN_" + i,
					this);
			fileMenu.add(lastOpenedProfiles[i]);
		}
		updateLastOpenedProfiles();
		fileMenu.addSeparator();
		fileMenu.add(JFSSupport.getMenuItem("menu.exit", "EXIT", this));

		// Create view menu:
		viewGroup = new ButtonGroup();
		byte view = config.getView();
		for (JFSViewMode mode : JFSViewModes.getInstance().getModes()) {
			viewMenu.add(JFSSupport.getRadioButtonMenuItem(viewGroup, mode
					.getAlias(), "VIEW_" + mode.getId(), view == mode.getId(),
					this));
		}

		// Create mode menu:
		syncGroup = new ButtonGroup();
		byte sync = config.getSyncMode();
		for (JFSSyncMode mode : JFSSyncModes.getInstance().getModes()) {
			modeMenu.add(JFSSupport.getRadioButtonMenuItem(syncGroup, mode
					.getAlias(), "SYNCMODE_" + mode.getId(), sync == mode
					.getId(), this));
		}

		// Create tools menu:
		toolsMenu.add(JFSSupport.getMenuItem("menu.assistant", "ASSISTANT",
				this, "jfs.icon.assistant"));
		toolsMenu.addSeparator();
		toolsMenu.add(JFSSupport.getMenuItem("menu.options", "OPTIONS", this,
				"jfs.icon.profile"));
		toolsMenu.add(JFSSupport.getMenuItem("menu.compare", "COMPARE", this,
				"jfs.icon.compare"));
		toolsMenu.add(JFSSupport.getMenuItem("menu.synchronize", "SYNCHRONIZE",
				this, "jfs.icon.synchronize"));
		toolsMenu.addSeparator();
		toolsMenu.add(JFSSupport.getMenuItem("menu.history", "HISTORY", this));
		toolsMenu.addSeparator();
		serverItem = JFSSupport.getCheckBoxMenuItem("menu.server",
				"server.title", false, this);
		toolsMenu.add(serverItem);
		toolsMenu.addSeparator();
		toolsMenu.add(JFSSupport.getMenuItem("menu.outLog", "OUTPUT_LOG", this,
				"jfs.icon.log"));
		toolsMenu.add(JFSSupport.getMenuItem("menu.errLog", "error.log", this,
				"jfs.icon.log"));

		// Create plug-ins menu:
		for (JFSPlugin plugin : JFSPluginRepository.getInstance().getPlugins()) {
			plugInsMenu.add(JFSSupport.getMenuItem(plugin.getId(), "PLUGIN_"
					+ plugin.getId(), this, "jfs.icon.plugin"));
		}

		// Create help menu:
		helpMenu.add(JFSSupport.getMenuItem("menu.helpTopics",
				"jfs.help.topics", this, "jfs.icon.help"));
		helpMenu.addSeparator();
		helpMenu.add(JFSSupport.getMenuItem("menu.info", "INFO", this));

		// Add menues to menu bar:
		menubar.add(fileMenu);
		menubar.add(viewMenu);
		menubar.add(modeMenu);
		menubar.add(toolsMenu);
		menubar.add(plugInsMenu);
		menubar.add(helpMenu);
		frame.setJMenuBar(menubar);

		// Create control panel:
		JToolBar bar = new JToolBar();
		bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
		bar.add(JFSSupport.getButton("jfs.icon.new", "NEW", this, "menu.new"));
		bar.add(JFSSupport
				.getButton("jfs.icon.open", "OPEN", this, "menu.open"));
		bar.add(JFSSupport
				.getButton("jfs.icon.save", "SAVE", this, "menu.save"));
		bar.add(JFSSupport.getButton("jfs.icon.saveAs", "SAVE_AS", this,
				"menu.saveAs"));
		bar.addSeparator();
		bar.add(JFSSupport.getButton("jfs.icon.profile", "OPTIONS", this,
				"menu.options"));
		bar.add(JFSSupport.getButton("jfs.icon.compare", "COMPARE", this,
				"menu.compare"));
		bar.add(JFSSupport.getButton("jfs.icon.synchronize", "SYNCHRONIZE",
				this, "menu.synchronize"));
		bar.addSeparator();
		serverButton = JFSSupport.getToggleButton("jfs.icon.server",
				"server.title", false, this, "menu.server");
		bar.add(serverButton);
		bar.addSeparator();
		JButton assistant = JFSSupport.getButton("jfs.icon.assistant",
				"ASSISTANT", this, "menu.assistant");
		assistant.setText(t.get("menu.assistant"));
		bar.add(assistant);

		// Status label:
		JPanel statePanel = new JPanel(new GridLayout(1, 4));
		stateOverallSize = new JLabel();
		statePanel.add(stateOverallSize);
		stateViewSize = new JLabel();
		statePanel.add(stateViewSize);
		stateDuration = new JLabel();
		statePanel.add(stateDuration);
		stateSyncMode = new JLabel();
		statePanel.add(stateSyncMode);

		// South panel:
		JPanel south = new JPanel(new BorderLayout());
		errorLog = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		errorLog.add(new JLabel(t.get("error.log")));
		errorLog.add(JFSSupport.getButton("jfs.icon.error", "error.log", this,
				"error.log"));
		south.add(errorLog, BorderLayout.NORTH);
		south.add(statePanel, BorderLayout.SOUTH);
		errorLog.setVisible(false);

		// Add elements to content pane:
		cp.add(bar, BorderLayout.NORTH);
		cp.add(new JScrollPane(syncTable), BorderLayout.CENTER);
		cp.add(south, BorderLayout.SOUTH);

		// Set some frame attributes:
		JFSSettings s = JFSSettings.getInstance();
		frame.setBounds(s.getWindowX(), s.getWindowY(), s.getWindowWidth(), s
				.getWindowHeight());
		frame.setVisible(true);

		// The state has to be set after making the frame visable
		// because otherwise the state change is ignored by some
		// window managers:
		frame.setExtendedState(s.getWindowState());

		// Create the progress view for the algorithm, which is displayed
		// for every comparison and synchronization later:
		progressView = new JFSProgressView(this);
		JFSProgress.getInstance().attach(progressView);

		// Register at configuration object, question view, and error log:
		config.attach(this);
		JFSSynchronization.getInstance().getQuestion().setOracle(
				new JFSQuestionView(this));
		JFSLog.getErr().attach(this);
	}

	/**
	 * Returns the reference of the used frame.
	 * 
	 * @return The frame of the main view.
	 */
	public final JFrame getFrame() {
		return frame;
	}

	/**
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		String cmd = event.getActionCommand();

		actionPerformed(cmd);
	}

	/**
	 * Action Listener of the main frame.
	 * 
	 * @param cmd
	 *            The transmitted command string.
	 */
	public void actionPerformed(String cmd) {
		// Get translation objects, configuration, settings, and task:
		JFSText t = JFSText.getInstance();
		JFSConfig config = JFSConfig.getInstance();
		JFSSettings s = JFSSettings.getInstance();

		if (cmd.equals("NEW") || cmd.equals("OPEN") || cmd.startsWith("OPEN_")) {
			int result = JOptionPane.OK_OPTION;

			// Check whether changes need to be stored:
			if (!config.isCurrentProfileStored()) {
				JLabel msg = new JLabel(t.get("message.store"));
				result = JOptionPane.showConfirmDialog(frame, msg, t
						.get("general.warning"), JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);
			}

			if (result == JOptionPane.OK_OPTION) {

				if (cmd.equals("NEW")) {
					// Clean the configuration settings and update the
					// observers:
					s.setCurrentProfile(null);
					config.clean();
					config.fireUpdate();
					new JFSConfigView(frame);
				}

				if (cmd.equals("OPEN")) {
					File last = s.getLastProfileDir();
					/* old open dialog
					JFileChooser chooser = new JFileChooser(last);
					JFSConfigFileFilter filter = new JFSConfigFileFilter();
					chooser.setFileFilter(filter);

					int returnVal = chooser.showOpenDialog(frame);
					s.setLastProfileDir(chooser.getCurrentDirectory());

					if (returnVal == JFileChooser.APPROVE_OPTION) {
						// Show error message if opening failed:
						if (!config.load(chooser.getSelectedFile())) {
							JLabel label = new JLabel(t
									.get("error.profile.load"));
							JOptionPane.showMessageDialog(frame, label, t
									.get("error.window.title"),
									JOptionPane.ERROR_MESSAGE);
							actionPerformed("NEW");
						}
					}
					*/
                    JFSConfigFileFilter filter = new JFSConfigFileFilter();
					String fileS = UIHelper.showOpenDialog(frame, last.getAbsolutePath(), false, t.get("menu.open"), t.getLocale(), filter, UIHelper.TYPE_OPEN, null);
                    if (fileS != null) {
                        File file = new File(fileS);
                        s.setLastProfileDir(file.getParentFile());
                        // Show error message if opening failed:
                        if (!config.load(file)) {
                            JLabel label = new JLabel(t
                                    .get("error.profile.load"));
                            JOptionPane.showMessageDialog(frame, label, t
                                    .get("error.window.title"),
                                    JOptionPane.ERROR_MESSAGE);
                            actionPerformed("NEW");
                        }
                        else
                        {
                            if (JFSSettings.getInstance().isAutoCompare())
                            {
                                actionPerformed("COMPARE");
                            }
                        }
                    }
					
					updateLastOpenedProfiles();
				}

				if (cmd.startsWith("OPEN_")) {
					// Cut "OPEN_":
					int i = Integer.parseInt(cmd.substring(5));
					// Show error message if opening failed:
					if (!config.load(s.getLastOpenedProfiles().get(i))) {
						JLabel label = new JLabel(t.get("error.profile.load"));
						JOptionPane.showMessageDialog(frame, label, t
								.get("error.window.title"),
								JOptionPane.ERROR_MESSAGE);
						actionPerformed("NEW");
					}
                    else
                    {
                        if (JFSSettings.getInstance().isAutoCompare())
                        {
                            actionPerformed("COMPARE");
                        }
                    }
					updateLastOpenedProfiles();
				}
			}
		}

		if (cmd.equals("SAVE") || cmd.equals("SAVE_AS")) {
			boolean success = true;

			// Store the configuration at once only if an appropriate
			// configuration file exists and the command equals 'SAVE':
			if (cmd.equals("SAVE") && (s.getCurrentProfile() != null)) {
				success = config.store(s.getCurrentProfile());
			} else {
				File last = s.getLastProfileDir();
				/*
				JFileChooser chooser = new JFileChooser(last);
				JFSConfigFileFilter filter = new JFSConfigFileFilter();
				chooser.setFileFilter(filter);

				int returnVal = chooser.showSaveDialog(frame);
				s.setLastProfileDir(chooser.getCurrentDirectory());

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					int result = JOptionPane.OK_OPTION;
					File selectedFile = chooser.getSelectedFile();

					// If file has no extension add ".xml":
					if (selectedFile.getName().indexOf(".") == -1) {
						selectedFile = new File(selectedFile.getParentFile(),
								selectedFile.getName() + ".xml");
					}

					// If file already exists ask for overwriting:
					if (selectedFile.exists()) {
						JLabel msg = new JLabel(t.get("message.replace"));
						result = JOptionPane.showConfirmDialog(frame, msg, t
								.get("general.warning"),
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.WARNING_MESSAGE);
					}

					if (result == JOptionPane.OK_OPTION) {
						success = config.store(selectedFile);
					}
				}
                */

				JFSConfigFileFilter filter = new JFSConfigFileFilter();

                String fileS = UIHelper.showOpenDialog(frame, last.getAbsolutePath(), false, t.get("menu.save"), t.getLocale(), filter, UIHelper.TYPE_SAVE, null);
                
                if (fileS != null) {
                    File file = new File(fileS);
                    s.setLastProfileDir(file.getParentFile());

                    // If file has no extension add ".xml":
                    if (file.getName().indexOf(".") == -1) {
                        file = new File(file.getParentFile(),
                                file.getName() + ".xml");
                    }

                    // If file already exists ask for overwriting:
                    int result = JOptionPane.OK_OPTION;
                    if (file.exists()) {
                        JLabel msg = new JLabel(t.get("message.replace"));
                        result = JOptionPane.showConfirmDialog(frame, msg, t
                                .get("general.warning"),
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                    }

                    if (result == JOptionPane.OK_OPTION) {
                        success = config.store(file);
                    }
                }
				
			}

			// Show error message if saving failed:
			if (!success) {
				JLabel label = new JLabel(t.get("error.profile.store"));
				JOptionPane.showMessageDialog(frame, label, t
						.get("error.window.title"), JOptionPane.ERROR_MESSAGE);
			}
			updateLastOpenedProfiles();
		}

		if (cmd.equals("OPTIONS")) {
			new JFSConfigView(frame);
		}

		if (cmd.startsWith("VIEW")) {
			// Cut "VIEW_":
			String view = cmd.substring(5);
			config.setView(Byte.parseByte(view));
			config.fireConfigUpdate();
		}

		if (cmd.startsWith("SYNCMODE")) {
			// Cut "SYNCMODE_":
			String syncMode = cmd.substring(9);
			config.setSyncMode(Byte.parseByte(syncMode));
			config.fireConfigUpdate();
		}

		if (cmd.equals("SYNCHRONIZE")) {
			// Compute synchronization list:
			JFSSynchronization.getInstance().computeSynchronizationLists();

			// Show list of files to copy and to delete:
			JFSConfirmationView cv = new JFSConfirmationView(frame);

			// If the user confirms the lists, start synchronization as new
			// thread:
			if (cv.getResult() == JOptionPane.OK_OPTION) {
				progressView.synchronizeInThread();

				// Show lists of failed copy and delete statements:
				new JFSReportView(frame);
			}

			// Test whether the local server was shut down after
			// synchronization:
			if (!JFSServerFactory.getInstance().isServerAlive()) {
				serverItem.setSelected(false);
				serverButton.setSelected(false);
			}
		}

		if (cmd.equals("COMPARE")) {
			// Start comparison as new thread:
			progressView.compareInThread();
		}

		if (cmd.equals("server.title")) {
			JFSServerFactory factory = JFSServerFactory.getInstance();
			if (!factory.isServerAlive()) {
				JLabel msg = new JLabel(t.get("server.start"));
				int result = JOptionPane.showConfirmDialog(frame, msg, t
						.get("general.warning"), JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (result == JOptionPane.OK_OPTION) {
					JFSServer server = factory.getServer();
					server.start();
					if (factory.isServerAlive()) {
						serverItem.setSelected(true);
						serverButton.setSelected(true);
					}
				} else {
					serverItem.setSelected(false);
					serverButton.setSelected(false);
				}
			} else {
				JLabel msg = new JLabel(t.get("server.stop"));
				int result = JOptionPane.showConfirmDialog(frame, msg, t
						.get("general.warning"), JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (result == JOptionPane.OK_OPTION) {
					JFSServer server = factory.getServer();
					server.stopServer();
					serverItem.setSelected(false);
					serverButton.setSelected(false);
				} else {
					if (factory.isServerAlive()) {
						serverItem.setSelected(true);
						serverButton.setSelected(true);
					}
				}
			}
		}

		if (cmd.equals("error.log")) {
			new JFSLogView(frame, JFSLogView.ERR);
		}

		if (cmd.equals("OUTPUT_LOG")) {
			new JFSLogView(frame, JFSLogView.OUT);
		}

		if (cmd.equals("general.reset")) {
			JLabel msg = new JLabel(t.get("message.reset"));
			int result = JOptionPane.showConfirmDialog(frame, msg, t
					.get("general.warning"), JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				s.clean();
				config.clean();
				config.fireUpdate();
				JFSLog.getErr().resetLogFile();
				JFSLog.getOut().resetLogFile();
				frame.setBounds(s.getWindowX(), s.getWindowY(), s
						.getWindowWidth(), s.getWindowHeight());
				frame.setExtendedState(s.getWindowState());
				updateLastOpenedProfiles();
			}
		}

		if (cmd.equals("EXIT")) {
			int result = JOptionPane.OK_OPTION;
			if (s.isAskOnExit())
			{
				// Ask for exiting the program:
				JLabel msg = new JLabel(t.get("message.exit"));
//				result = JOptionPane.showConfirmDialog(frame, msg, t
//						.get("general.warning"), JOptionPane.OK_CANCEL_OPTION,
//						JOptionPane.WARNING_MESSAGE);
				Object[] options = {t.get("general.true"),
						t.get("button.always"),
						t.get("general.false")};

				result = JOptionPane.showOptionDialog(frame, msg, t
						.get("general.warning"), JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			}

			if (result == 1)
			{
				s.setAskOnExit(false);
			}
			
			if (result != 2) {
				// Store last entered profile data:
				config.storeDefaultFile();

				// Store settings:
				s.store();

				// Close window and go back to main program:
				frame.setVisible(false);
				frame.dispose();

				// Exit application:
				System.exit(0);
			}
		}

		if (cmd.equals("INFO")) {
			new JFSInfoView(this);
		}

		if (cmd.equals("jfs.help.topics")) {
			if (helpView == null)
				helpView = new JFSHelpView(frame);
			else
				helpView.setVisible(true);
		}

		if (cmd.startsWith("PLUGIN_")) {
			String pluginId = cmd.substring(7);
			JFSPluginRepository.getInstance().getPlugin(pluginId).init(frame);
		}

		if (cmd.equals("ASSISTANT")) {
			if (assistantView == null)
				assistantView = new JFSAssistantView(this);
			else
				assistantView.setVisible(true);
		}

		if (cmd.equals("HISTORY")) {
			new JFSHistoryManagerView(this.getFrame());
		}
	}

	/**
	 * Updates the content of the main frame.
	 */
	public final void update() {
		JFSText t = JFSText.getInstance();
		stateOverallSize.setText(t.get("statePanel.overallSize") + " "
				+ JFSTable.getInstance().getTableSize());
		stateViewSize.setText(t.get("statePanel.viewSize") + " "
				+ JFSTable.getInstance().getViewSize());
		stateDuration.setText(t.get("statePanel.duration") + " "
				+ JFSProgress.getInstance().getDuration());
		updateComparisonTable();
	}

	/**
	 * Updates the content of the comparison table.
	 */
	public final void updateComparisonTable() {
		syncTable.clearSelection();
		syncTable.revalidate();
		syncTable.repaint();
	}

	/**
	 * Updates the list of last profiles.
	 */
	public final void updateLastOpenedProfiles() {
		JFSSettings s = JFSSettings.getInstance();
		ArrayList<File> profiles = s.getLastOpenedProfiles();
		for (int i = 0; i < profiles.size(); i++) {
			String name = profiles.get(i).getName();
			if (name.length() > 30)
				name = name.substring(0, 26) + "...";
			lastOpenedProfiles[i].setText((i + 1) + ". " + name);
			lastOpenedProfiles[i].setVisible(true);
		}
		for (int i = profiles.size(); i < JFSConst.LAST_OPENED_PROFILES_SIZE; i++) {
			lastOpenedProfiles[i].setText("");
			lastOpenedProfiles[i].setVisible(false);
		}
	}

	/**
	 * @see JFSConfigObserver#updateConfig(JFSConfig)
	 */
	public final void updateConfig(JFSConfig config) {
		// Update title:
		JFSText t = JFSText.getInstance();
		frame.setTitle(t.get("general.appName") + " "
				+ JFSConst.getInstance().getString("jfs.version") + " - "
				+ config.getTitle());

		// Update view selection:
		JRadioButtonMenuItem button;
		Enumeration<AbstractButton> views = viewGroup.getElements();

		while (views.hasMoreElements()) {
			button = (JRadioButtonMenuItem) views.nextElement();

			if (button.getActionCommand().equals("VIEW_" + config.getView()))
				button.setSelected(true);
			else
				button.setSelected(false);
		}

		// Update snyc mode selection:
		Enumeration<AbstractButton> syncModes = syncGroup.getElements();
		JFSSyncMode mode = JFSSyncModes.getInstance().getCurrentMode();
		stateSyncMode.setText(t.get("statePanel.syncMode") + " "
				+ JFSText.getInstance().get(mode.getAlias()));

		while (syncModes.hasMoreElements()) {
			button = (JRadioButtonMenuItem) syncModes.nextElement();

			if (button.getActionCommand().equals(
					"SYNCMODE_" + config.getSyncMode()))
				button.setSelected(true);
			else
				button.setSelected(false);
		}

		// Update state panel:
		update();
	}

	/**
	 * @see JFSConfigObserver#updateComparison(JFSConfig)
	 */
	public final void updateComparison(JFSConfig config) {
		update();
	}

	/**
	 * @see JFSConfigObserver#updateServer(JFSConfig)
	 */
	public final void updateServer(JFSConfig config) {
		update();
	}

	/**
	 * @see java.awt.event.WindowListener#windowClosing(WindowEvent)
	 */
	public void windowClosing(WindowEvent arg0) {
		super.windowClosing(arg0);
		actionPerformed("EXIT");
	}

	/**
	 * @see java.awt.event.WindowStateListener#windowStateChanged(WindowEvent)
	 */
	public void windowStateChanged(WindowEvent arg0) {
		super.windowStateChanged(arg0);
		int state = arg0.getNewState();

		// If state "iconified" is ignored and not stored in the settings
		// object:
		if (state != Frame.ICONIFIED) {
			JFSSettings settings = JFSSettings.getInstance();
			settings.setWindowState(state);
		}
	}

	/**
	 * @see ComponentListener#componentHidden(ComponentEvent)
	 */
	public void componentHidden(ComponentEvent arg0) {
	}

	/**
	 * @see ComponentListener#componentMoved(ComponentEvent)
	 */
	public void componentMoved(ComponentEvent arg0) {
		componentResized(arg0);
	}

	/**
	 * @see ComponentListener#componentResized(ComponentEvent)
	 */
	public void componentResized(ComponentEvent arg0) {
		JFSSettings settings = JFSSettings.getInstance();
		int state = frame.getExtendedState();
		Rectangle r = frame.getBounds();

		if (state == Frame.NORMAL) {
			settings.setWindowBounds(r.x, r.y, r.width, r.height);
		} else if (state == Frame.MAXIMIZED_VERT) {
			settings.setWindowX(r.x);
			settings.setWindowWidth(r.width);
		} else if (state == Frame.MAXIMIZED_HORIZ) {
			settings.setWindowY(r.y);
			settings.setWindowHeight(r.height);
		}
	}

	/**
	 * @see ComponentListener#componentShown(ComponentEvent)
	 */
	public void componentShown(ComponentEvent arg0) {
	}

	/**
	 * @see JFSLogObserver#update(JFSLog)
	 */
	public void update(JFSLog log) {
		errorLog.setVisible(log.hasUnreadLogMessages());
	}
}