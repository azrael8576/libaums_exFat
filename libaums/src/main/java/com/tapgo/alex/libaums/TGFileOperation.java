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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.github.mjdev.libaums.LogUtil;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileInputStream;

/**
 * List adapter to represent the contents of an {@link UsbFile} directory.
 *
 * @author mjahnen
 *
 */
public class TGFileOperation {


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
    private final int EXFAT_TYPE_IN_ABSTRACTFILESYSTEM = 96919910;

    private List<UsbFile> files;
    private HashMap<String, UsbFile> filesMap;
    private UsbFile currentDir;
    private FileSystem currentFs;
    private Context context;

    private LayoutInflater inflater;

//    public TGFileOperation(FileSystem currentFs) throws IOException {
//        this.currentFs = currentFs;
//
//        UsbFile dir = currentFs.getRootDirectory();
//        currentDir = dir;
//        files = new ArrayList<UsbFile>();
//        filesMap = new HashMap<String, UsbFile>();
//
//        refresh();
//    }

    public TGFileOperation(FileSystem currentFs, UsbFile dir) throws IOException, TGFileOperationException {
        this.currentFs = currentFs;

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
        LogUtil.writeLog("refresh()CurrentDir: " + currentDir.getName());
        files = Arrays.asList(currentDir.listFiles());
        Collections.sort(files, comparator);

        for (UsbFile file: files) {
            LogUtil.writeLog("refresh()PutMap: " + file.getName());
            filesMap.put(file.getName(), file);
        }
    }

    public int getCount() {
        return files.size();
    }

    public UsbFile getFile(String pathname) throws TGFileOperationException, IOException {
        if ("/".equals(pathname)) {
            throw new TGFileOperationException("Failed to get file.");
        }
        String currentPath;
        StringTokenizer st = new StringTokenizer(pathname, "/");
        ArrayList<String> subPathList = new ArrayList();
        while (st.hasMoreTokens()){
            subPathList.add(st.nextToken());
        }
        currentPath = subPathList.get(0);
        LogUtil.writeLog("in: " + currentPath);
        if (filesMap.containsKey(currentPath)) {
            currentDir = filesMap.get(currentPath);
            String subPath = "";
            if (subPathList.size() > 1){
                subPathList.remove(0);
                for (String date: subPathList) {
                    subPath += "/" + date;
                }
                LogUtil.writeLog("[subPath]: " + subPath);
                refresh();
                return getFile(subPath);
            } else if (!currentDir.isDirectory()){
                return currentDir;
            }
        }
        throw new TGFileOperationException("Failed to find file: " + pathname);
    }

    public ArrayList<UsbFile> getFileListIn(String dirPathname) throws TGFileOperationException, IOException {
        ArrayList<UsbFile> result = new ArrayList<>();
        if ("/".equals(dirPathname)) {
            currentDir.listFiles();
        }
        String currentPath;
        StringTokenizer st = new StringTokenizer(dirPathname, "/");
        ArrayList<String> subPathList = new ArrayList();
        while (st.hasMoreTokens()){
            subPathList.add(st.nextToken());
        }
        currentPath = subPathList.get(0);
        LogUtil.writeLog("in: " + currentPath);
        if (filesMap.containsKey(currentPath)) {
            currentDir = filesMap.get(currentPath);
            String subPath = "";
            if (subPathList.size() > 1){
                subPathList.remove(0);
                for (String date: subPathList) {
                    subPath += "/" + date;
                }
                LogUtil.writeLog("[subPath]: " + subPath);
                refresh();
                return getFileListIn(subPath);
            } else {
                for (UsbFile file:currentDir.listFiles()) {
                    if (!("._").equals(file.getName().substring(0,2))){
                        result.add(file);
                    }
                }
                return result;
            }
        }
        throw new TGFileOperationException("Failed to find dir: " + dirPathname);
    }

//    public void fileCopy(Context context, UsbFile usbFile, String toPath) throws TGFileOperationException, IOException {
//        LogUtil.writeLog("copy file to: " + toPath);
//        Path path = Paths.get(toPath);
//        if (Files.exists(path)) {
//            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + usbFile.getName());
//            boolean res = file.createNewFile();
//            if (!res) {
//                throw new TGFileOperationException("已經存在此檔案: " + usbFile.getName());
//            } else {
//                CopyTaskParam param = new CopyTaskParam();
//                param.from = usbFile;
//                param.to = file;
//                this.context = context;
//                new CopyTask().execute(param);
//            }
//        }
//    }

    public void fileCopyToPhone(Context context, ArrayList<UsbFile> usbFiles, String toPath) throws TGFileOperationException, IOException {
//        parserPath(toPath);
        LogUtil.writeLog("copy files to: " + toPath);
        Path path = Paths.get(toPath);
        if (Files.exists(path)) {
            for (UsbFile usbFile: usbFiles) {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + usbFile.getName());
                boolean res = file.createNewFile();
                if (!res && !usbFile.isDirectory()) {
                    throw new TGFileOperationException("File already exists: " + usbFile.getName());
                } else if (!usbFile.isDirectory()) {
                    CopyTaskParam param = new CopyTaskParam();
                    param.from = usbFile;
                    param.to = file;
                    this.context = context;
                    if (usbFiles.size()==1) {
                        new CopyTask().execute(param);
                    }
                    else {
                        param.multifile = true;
                        new CopyTask().execute(param);
                    }
                }
            }
        } else{
            throw new TGFileOperationException("Can't find toPath" + toPath);
        }
    }

    public void parserPath(String toPath) {
        StringTokenizer st = new StringTokenizer(toPath, "/");

        while (st.hasMoreTokens()){
//            LogUtil.writeLog(st.nextToken());
        }
    }

    /**
     * Class to hold the files for a copy task. Holds the source and the
     * destination file.
     *
     * @author mjahnen
     *
     */
    private static class CopyTaskParam {
        /* package */UsbFile from;
        /* package */File to;
        Boolean multifile = false;
    }

    /**
     * Asynchronous task to copy a file from the mass storage device connected
     * via USB to the internal storage.
     *
     * @author mjahnen
     *
     */
    private class CopyTask extends AsyncTask<CopyTaskParam, Integer, Boolean> {

        //        private ProgressDialog dialog;
        private CopyTaskParam param;

        public CopyTask() {
//            dialog = new ProgressDialog(MainActivity.this);
//            dialog.setTitle("Copying file");
//            dialog.setMessage("Copying a file to the internal storage, this can take some time!");
//            dialog.setIndeterminate(false);
//            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//            dialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
//            dialog.show();
        }

        @Override
        protected Boolean doInBackground(CopyTaskParam... params) {
            long time = System.currentTimeMillis();
            param = params[0];
            try {
                OutputStream out = new BufferedOutputStream(new FileOutputStream(param.to));
                InputStream inputStream = new UsbFileInputStream(param.from);
                byte[] bytes = new byte[currentFs.getChunkSize()];
                int count;
                long total = 0;

//                Log.d(TAG, "Copy file with length: " + param.from.getLength());

                while ((count = inputStream.read(bytes)) != -1){
                    out.write(bytes, 0, count);
                    total += count;
                    int progress = (int) total;
                    if(param.from.getLength() > Integer.MAX_VALUE) {
                        progress = (int) (total / 1024);
                    }
                    publishProgress(progress);
                }

                out.close();
                inputStream.close();
            } catch (IOException e) {
//                Log.e(TAG, "error copying!", e);
                try {
                    throw new TGFileOperationException("error copying! file name = " + param.from.getName());
                } catch (TGFileOperationException ex) {
                    ex.printStackTrace();
                }
                LogUtil.writeLog("error copying!" + e);
                return false;
            }
//            Log.d(TAG, "copy time: " + (System.currentTimeMillis() - time));
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
//            dialog.dismiss();
            if (result) {
                Toast.makeText(context, "success copy : " + param.to.getAbsolutePath(), Toast.LENGTH_LONG).show();
                LogUtil.writeLog("success copy : " + param.to.getAbsolutePath());
            }
            if (!param.multifile) {
                Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW);
                File file = new File(param.to.getAbsolutePath());
                String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri
                        .fromFile(file).toString());
                String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        extension);

                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    myIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    uri = FileProvider.getUriForFile(context,
                            context.getApplicationContext().getPackageName() + ".provider",
                            file);
                } else {
                    uri = Uri.fromFile(file);
                }
                myIntent.setDataAndType(uri, mimetype);
                try {
                    context.startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    LogUtil.writeLog("Could no find an app for that file!");
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int max = (int) param.from.getLength();
            if(param.from.getLength() > Integer.MAX_VALUE) {
                max = (int) (param.from.getLength() / 1024);
            }
//            dialog.setMax(max);
//            dialog.setProgress(values[0]);
        }

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