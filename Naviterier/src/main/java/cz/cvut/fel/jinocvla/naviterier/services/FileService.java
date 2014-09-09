package cz.cvut.fel.jinocvla.naviterier.services;

import java.io.File;
import java.io.IOException;

/**
 * Created by usul on 12.4.2014.
 */
public interface FileService {

    public boolean isExternalStorageWritable();

    public boolean isExternalStorageReadable();

    public File getExternalStorageFolder(String folderName);

    public File getExternalStorageFile(String folderName, String fileName);

    public void overrideFileByString(File file, String data) throws IOException;

    public void appendStringToFile(File file, String data) throws IOException;

    public String readStringFromFile(File file) throws IOException;

    public String readStringFromFileAndClean(File file) throws IOException;

}
