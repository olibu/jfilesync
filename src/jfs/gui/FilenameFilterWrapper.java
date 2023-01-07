package jfs.gui;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.filechooser.FileFilter;

public class FilenameFilterWrapper implements FilenameFilter
{
    private FileFilter m_filter;
    public FilenameFilterWrapper(FileFilter filter)
    {
        m_filter = filter;
    }
    public boolean accept(File dir, String name)
    {
        return m_filter.accept(new File(dir, name));
    }
    

}
