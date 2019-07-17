package com.arc.lucene;

import java.io.File;
import java.io.FileFilter;

public class TestFileFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
        return pathname.getName().toLowerCase().endsWith(".txt");
    }
}
