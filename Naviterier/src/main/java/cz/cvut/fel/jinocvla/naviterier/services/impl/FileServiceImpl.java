package cz.cvut.fel.jinocvla.naviterier.services.impl;

import android.os.Environment;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import cz.cvut.fel.jinocvla.naviterier.services.FileService;

/**
 * Created by usul on 12.4.2014.
 */
public class FileServiceImpl implements FileService {

    @Override
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public File getExternalStorageFolder(String folderName) {
        return Environment.getExternalStoragePublicDirectory(
                folderName);
    }

    @Override
    public File getExternalStorageFile(String folderName, String fileName) {
        return new File(Environment.getExternalStoragePublicDirectory(
                folderName), fileName);
    }

    @Override
    public void overrideFileByString(File file, String data) throws IOException {
        FileUtils.writeStringToFile(file, data);
    }

    @Override
    public void appendStringToFile(File file, String data) throws IOException {
        String tmp = FileUtils.readFileToString(file);
        tmp += data;
        FileUtils.writeStringToFile(file, tmp);
    }

    @Override
    public String readStringFromFile(File file) throws IOException {
        return FileUtils.readFileToString(file);
    }

    @Override
    public String readStringFromFileAndClean(File file) throws IOException {
        String tmp = FileUtils.readFileToString(file);
        FileUtils.forceDelete(file);
        return tmp;
    }
}
