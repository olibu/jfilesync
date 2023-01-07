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
import java.util.ArrayList;

import javax.swing.UIManager;

/**
 * Manages all JFileSync settings. The class implements the singleton design
 * pattern.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSSettings.java,v 1.21 2007/07/20 14:07:11 heidrich Exp $
 */
public abstract class JFSSettings {

	/** Stores the only instance of the class. */
	private static JFSSettings instance = null;

	/** The settings file containing all attribute values. */
	protected static File file = new File(JFSConst.HOME_DIR + File.separator
			+ JFSConst.SETTINGS_FILE);

	/** Determines whether the system runs in debug mode (not stored). */
	private boolean debug = JFSConst.DEBUG;

	/** Determines whether the system runs in GUI mode (not stored). */
	private boolean nogui = true;

	/** The state of the JFileSync main window (maximized, iconified, etc.). */
	protected int windowState;

	/** The JFileSync main window's X coordinate. */
	protected int windowX;

	/** The JFileSync main window's Y coordinate. */
	protected int windowY;

	/** The JFileSync main window's width. */
	protected int windowWidth;

	/** The JFileSync main window's height. */
	protected int windowHeight;

	/** The last directory when opening/saving an existing profile. */
	protected File lastProfileDir;

	/** The last directory when choosing the source of a directory pair. */
	protected File lastSrcPairDir;

	/** The last directory when choosing the target of a directory pair. */
	protected File lastTgtPairDir;

	/** The last used look and feel. */
	protected String laf;

	/** Stores the currently opened profile. */
	protected File currentProfile;

	/** The last visited opened profiles. */
	protected ArrayList<File> lastOpenedProfiles = new ArrayList<File>(
			JFSConst.LAST_OPENED_PROFILES_SIZE);

	/** Should the dialog be shown on exit **/
	protected boolean isAskOnExit = true;
	
	/** Executeable for file compare **/
	protected String compareProgram = null;
	
	/** The time difference to ignore when comparing files **/
	protected long ignoreTimeDiff = 0;

    /** The JFileSync diffDialog's X coordinate. */
    protected int diffDialogX;

    /** The JFileSync diffDialog's Y coordinate. */
    protected int diffDialogY;

    /** The JFileSync diffDialog's width. */
    protected int diffDialogWidth;

    /** The JFileSync diffDialog's height. */
    protected int diffDialogHeight;

    protected boolean autoCompare = false;
	
	/**
	 * Sets some default values for the settings object and loads the settings
	 * file from the standard location.
	 */
	protected JFSSettings() {
		clean();
		load();
	}

	/**
	 * Returns the reference of the only object of the class.
	 * 
	 * @return The only instance.
	 */
	public final static JFSSettings getInstance() {
		if (instance == null)
			instance = new JFSSettingsXML();

		return instance;
	}

	/**
	 * Restores the default values.
	 */
	public final void clean() {
		// Restore default values:
		windowState = JFSConst.WINDOW_STATE;
		windowX = JFSConst.WINDOW_X;
		windowY = JFSConst.WINDOW_Y;
		windowWidth = JFSConst.WINDOW_WIDTH;
		windowHeight = JFSConst.WINDOW_HEIGHT;
		lastProfileDir = new File(JFSConst.WORKING_DIR);
		lastSrcPairDir = new File(JFSConst.WORKING_DIR);
		lastTgtPairDir = new File(JFSConst.WORKING_DIR);
		laf = UIManager.getSystemLookAndFeelClassName();
		currentProfile = null;
		lastOpenedProfiles.clear();
		isAskOnExit = JFSConst.ASK_ON_EXIT;
		compareProgram = null;
		diffDialogX = JFSConst.WINDOW_X;
		diffDialogY = JFSConst.WINDOW_Y;
		diffDialogWidth = JFSConst.WINDOW_WIDTH;
		diffDialogHeight = JFSConst.WINDOW_HEIGHT;

		// Set the look and feel when the settings object is cleaned:
		if (!nogui) {
			try {
				UIManager.setLookAndFeel(laf);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Loads a settings file.
	 */
	public abstract void load();

	/**
	 * Stores a settings file.
	 */
	public abstract void store();

	/**
	 * Returns whether the system runs in debug mode.
	 * 
	 * @return True, if the system runs in debug mode.
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Determines whether the system runs in debug mode (not stored).
	 * 
	 * @param debug
	 *            True, if the system runs in debug mode.
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Returns whether the system runs in GUI mode.
	 * 
	 * @return True, if the system runs in GUI mode.
	 */
	public boolean isNoGui() {
		return nogui;
	}

	/**
	 * Determines whether the system runs in GUI mode (not stored).
	 * 
	 * @param nogui
	 *            True, if the system runs in GUI mode.
	 */
	public void setNoGui(boolean nogui) {
		this.nogui = nogui;

		if (!nogui) {
			try {
				UIManager.setLookAndFeel(laf);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Returns the state of the JFS main window.
	 * 
	 * @return The window's state.
	 */
	public final int getWindowState() {
		return windowState;
	}

	/**
	 * Sets the state of the JFS main window.
	 * 
	 * @param i
	 *            State of the main window.
	 */
	public final void setWindowState(int i) {
		windowState = i;
	}

	/**
	 * Returns the X coordinate for the JFS main window.
	 * 
	 * @return The X coordinate.
	 */
	public final int getWindowX() {
		return windowX;
	}

	/**
	 * Returns the Y coordinate for the JFS main window.
	 * 
	 * @return The Y coordinate.
	 */
	public final int getWindowY() {
		return windowY;
	}

	/**
	 * Returns the width for the JFS main window.
	 * 
	 * @return The width.
	 */
	public final int getWindowWidth() {
		return windowWidth;
	}

	/**
	 * Returns the height for the JFS main window.
	 * 
	 * @return The height.
	 */
	public final int getWindowHeight() {
		return windowHeight;
	}

	/**
	 * Sets the bounds to store for the JFS main window.
	 * 
	 * @param x
	 *            X coordinate.
	 * @param y
	 *            Y coordinate.
	 * @param width
	 *            Width.
	 * @param height
	 *            Height.
	 */
	public final void setWindowBounds(int x, int y, int width, int height) {
		windowX = x;
		windowY = y;
		windowWidth = width;
		windowHeight = height;
	}

	/**
	 * Sets the bounds to store for the JFS main window.
	 * 
	 * @param x
	 *            X coordinate.
	 */
	public final void setWindowX(int x) {
		windowX = x;
	}

	/**
	 * Sets the bounds to store for the JFS main window.
	 * 
	 * @param y
	 *            Y coordinate.
	 */
	public final void setWindowY(int y) {
		windowY = y;
	}

	/**
	 * Sets the bounds to store for the JFS main window.
	 * 
	 * @param width
	 *            Width.
	 */
	public final void setWindowWidth(int width) {
		windowWidth = width;
	}

	/**
	 * Sets the bounds to store for the JFS main window.
	 * 
	 * @param height
	 *            Height.
	 */
	public final void setWindowHeight(int height) {
		windowHeight = height;
	}

	/**
	 * Returns the last visited directory when opening an existing profile or
	 * saving a profile.
	 * 
	 * @return The last visited directory.
	 */
	public File getLastProfileDir() {
		return lastProfileDir;
	}

	/**
	 * Sets the last visited directory when opening an existing profile or
	 * saving a profile.
	 * 
	 * @param file
	 *            The last visited directory.
	 */
	public void setLastProfileDir(File file) {
		if (file != null) {
			if (file.isFile())
				file = file.getParentFile();

			if (file != null)
				lastProfileDir = file;
		}
	}

	/**
	 * Returns the last visited source directory when choosing a pair of
	 * directories to compare.
	 * 
	 * @return The last visited source directory.
	 */
	public File getLastSrcPairDir() {
		return lastSrcPairDir;
	}

	/**
	 * Sets the last visited source directory when choosing a pair of
	 * directories to compare.
	 * 
	 * @param file
	 *            The last visited source directory.
	 */
	public void setLastSrcPairDir(File file) {
		if (file != null) {
			if (file.isFile())
				file = file.getParentFile();

			if (file != null)
				lastSrcPairDir = file;
		}
	}

	/**
	 * Returns the last visited target directory when choosing a pair of
	 * directories to compare.
	 * 
	 * @return The last visited target directory.
	 */
	public File getLastTgtPairDir() {
		return lastTgtPairDir;
	}

	/**
	 * Sets the last visited target directory when choosing a pair of
	 * directories to compare.
	 * 
	 * @param file
	 *            The last visited target directory.
	 */
	public void setLastTgtPairDir(File file) {
		if (file != null) {
			if (file.isFile())
				file = file.getParentFile();

			if (file != null)
				lastTgtPairDir = file;
		}
	}

	/**
	 * Returns the look and feel.
	 * 
	 * @return String representation of the look and feel.
	 */
	public String getLaf() {
		return laf;
	}

	/**
	 * Sets the look and feel of the settings object and updates the UI Manager.
	 * 
	 * @param laf
	 *            The look and feel to set.
	 */
	public void setLaf(String laf) {
		this.laf = laf;
		if (!nogui) {
			try {
				UIManager.setLookAndFeel(laf);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Returns the currently opened profile. Null, if the profile was not stored
	 * yet.
	 * 
	 * @return The profile.
	 */
	public final File getCurrentProfile() {
		return currentProfile;
	}

	/**
	 * Sets the currently opened profile.
	 * 
	 * @param currentProfile
	 *            The profile.
	 */
	public final void setCurrentProfile(File currentProfile) {
		this.currentProfile = currentProfile;
	}

	/**
	 * Adds a new last opened profile. The element is added to the head of the
	 * list of last opened profiles. All other profiles are shifted to the
	 * right. The element at the tail of the list is removed. If the profile to
	 * add is already part of the list it is moved to the tail.
	 * 
	 * @param profile
	 *            The profile to add.
	 */
	public void addLastOpenedProfile(File profile) {
		if (lastOpenedProfiles.contains(profile))
			lastOpenedProfiles.remove(profile);

		lastOpenedProfiles.add(0, profile);

		if (lastOpenedProfiles.size() > JFSConst.LAST_OPENED_PROFILES_SIZE)
			lastOpenedProfiles.remove(JFSConst.LAST_OPENED_PROFILES_SIZE);
	}

	/**
	 * Returns the last opened profiles.
	 * 
	 * @return List of the profiles.
	 */
	public ArrayList<File> getLastOpenedProfiles() {
		return lastOpenedProfiles;
	}
	
	/**
	 * TODO: comment
	 * @return
	 */
	public boolean isAskOnExit() {
		return isAskOnExit;
	}

	/**
	 * TODO: comment
	 * @param isAskOnExit
	 */
	public void setAskOnExit(boolean isAskOnExit) {
		this.isAskOnExit = isAskOnExit;
	}

	/**
	 * Returns the path to the executable for file compare.
	 * 
	 * @return Path to the compare program. Will be null if the property is not set.
	 */
	public String getCompareProgram() {
		return compareProgram;
	}

	/**
	 * Set the path to the executable for file compare.
	 * @param compareProgram Path to the compare excutable.
	 */
	public void setCompareProgram(String compareProgram) {
		this.compareProgram = compareProgram;
	}

	public long getIgnoreTimeDiff()
	{
		// TODO Auto-generated method stub
		return ignoreTimeDiff;
	}
	
	/**
	 * The exact time in msec to ignore when comparing files.
	 * <p>
	 * This is important if a time difference between summer
	 * and winter time occurs.
	 * 
	 * @param ignoreTimeDiff
	 */
	public void setIgnoreTimeDiff(long ignoreTimeDiff)
	{
		this.ignoreTimeDiff = ignoreTimeDiff;
	}

	   /**
     * Returns the X coordinate for the JFS main window.
     * 
     * @return The X coordinate.
     */
    public final int getDiffDialogX() {
        return diffDialogX;
    }

    /**
     * Returns the Y coordinate for the JFS main window.
     * 
     * @return The Y coordinate.
     */
    public final int getDiffDialogY() {
        return diffDialogY;
    }

    /**
     * Returns the width for the JFS main window.
     * 
     * @return The width.
     */
    public final int getDiffDialogWidth() {
        return diffDialogWidth;
    }

    /**
     * Returns the height for the JFS main window.
     * 
     * @return The height.
     */
    public final int getDiffDialogHeight() {
        return diffDialogHeight;
    }

   /**
     * Sets the bounds to store for the Diff Dialog.
     * 
     * @param x
     *            X coordinate.
     * @param y
     *            Y coordinate.
     * @param width
     *            Width.
     * @param height
     *            Height.
     */
    public final void setDiffDialogBounds(int x, int y, int width, int height) {
        diffDialogX = x;
        diffDialogY = y;
        diffDialogWidth = width;
        diffDialogHeight = height;
    }

	public boolean isAutoCompare()
	{
	    return autoCompare;
	}
	
	public void setAutoCompare(boolean b)
	{
	    autoCompare = b;
	}
}