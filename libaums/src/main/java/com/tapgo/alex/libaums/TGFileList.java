/*
 * (C) Copyright 2014 mjahnen <jahnen@in.tum.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.tapgo.alex.libaums;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;

/**
 * List adapter to represent the contents of an {@link UsbFile} directory.
 *
 * @author mjahnen
 *
 */
public class TGFileList {

    /**
     * Class to compare {@link UsbFile}s. If the {@link UsbFile} is an directory
     * it is rated lower than an file, ie. directories come first when sorting.
     */
    private Comparator<UsbFile> comparator = new Comparator<UsbFile>() {

        @Override
        public int compare(UsbFile lhs, UsbFile rhs) {

            if (lhs.isDirectory() && !rhs.isDirectory()) {
                return -1;
            }

            if (rhs.isDirectory() && !lhs.isDirectory()) {
                return 1;
            }

            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    };

    private List<UsbFile> files;
    private HashMap<String, UsbFile> filesMap;
    private UsbFile currentDir;

    private LayoutInflater inflater;

    public TGFileList(FileSystem currentFs) throws IOException {
        UsbFile dir = currentFs.getRootDirectory();
        currentDir = dir;
        files = new ArrayList<UsbFile>();
        filesMap = new HashMap<String, UsbFile>();

        refresh();
    }

    /**
     * Reads the contents of the directory and notifies that the View shall be
     * updated.
     *
     * @throws IOException
     *             If reading contents of a directory fails.
     */
    public void refresh() throws IOException {
        files = Arrays.asList(currentDir.listFiles());
        Collections.sort(files, comparator);

        for (UsbFile file: files) {
            filesMap.put(file.getAbsolutePath(), file);
        }
    }

    public int getCount() {
        return files.size();
    }

    public String getList() {
        String fileList = "";
        for (UsbFile file: files) {
            fileList += file.getAbsolutePath() + "\n";
        }

        return fileList;
    }

    public UsbFile getItem(int position) {
        return files.get(position);
    }

    public HashMap getFilesMap() {
        return filesMap;
    }

    /**
     *
     * @return the directory which is currently be shown.
     */
    public UsbFile getCurrentDir() {
        return currentDir;
    }

}