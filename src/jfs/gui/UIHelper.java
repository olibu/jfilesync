package jfs.gui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


public class UIHelper
{
    public static boolean isMac = false;
    static
    {
        String osname = System.getProperty("os.name");
        if (osname != null && osname.toLowerCase().indexOf("mac") != -1)
        {
            isMac = true;
        }
    };

    public static final int TYPE_OPEN = FileDialog.LOAD;
    public static final int TYPE_SAVE = FileDialog.SAVE;
    
    
    /**
     * Show open dialog.
     * 
     * @param parent           The parent dialog
     * @param file             The file the dialog should start with
     * @param directoryOnly    Only accept directories
     * @param title            Title of the dialog
     * @param locale           Locale of the dialog.
     * @return null if no file was selected.
     */
    public static String showOpenDialog(Object parent, String file, boolean directoryOnly, String title, Locale locale, FileFilter filter, int type, String buttonText)
    {
        String result = null;
        if (isMac)
        {
            System.setProperty("apple.awt.fileDialogForDirectories", ""+directoryOnly);
            FileDialog fd = null;
            if (parent instanceof Frame)
                fd = new FileDialog((Frame)parent, title, type);
            else
                fd = new FileDialog((Dialog)parent, title, type);
            
            fd.setLocale(locale);
            if (filter!=null)
            {
                fd.setFilenameFilter(new FilenameFilterWrapper(filter));
            }
            File old = new File(file);
            if (old.isDirectory())
            {
                fd.setDirectory(file);
            }
            else
            {
                fd.setDirectory(old.getParent());
                fd.setFile(old.getName());
            }
            fd.setVisible(true);
            String retval = fd.getFile();
            if (retval != null)
            {
                File dir = new File(fd.getDirectory());
                File f = new File(dir, retval);
                result = f.getAbsolutePath();
            }
        }
        else
        {
            JFileChooser fc = new JFileChooser(file);
            if (directoryOnly)
            {
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }
            if (buttonText!=null)
            {
                fc.setApproveButtonText(buttonText);
            }
            fc.setDialogTitle(title);
            if (filter!=null)
            {
                fc.setFileFilter(filter);
            }
            int retval = fc.showOpenDialog((Component)parent);
            if (retval == JFileChooser.APPROVE_OPTION && fc.getSelectedFile()!=null)
            {
                result = fc.getSelectedFile().getAbsolutePath();
            }
        }
        return result;
    }
    
}
