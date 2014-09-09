package cz.cvut.fel.jinocvla.naviterier.services.impl;

import android.app.Activity;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.PersistenceException;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.List;

import cz.cvut.fel.jinocvla.naviterier.R;
import cz.cvut.fel.jinocvla.naviterier.models.Contact;
import cz.cvut.fel.jinocvla.naviterier.models.Contacts;
import cz.cvut.fel.jinocvla.naviterier.services.ContactsService;
import cz.cvut.fel.jinocvla.naviterier.services.FileService;
import cz.cvut.fel.jinocvla.naviterier.services.exception.FileException;
import cz.cvut.fel.jinocvla.naviterier.services.exception.MarshallingException;
import cz.cvut.fel.jinocvla.naviterier.services.exception.StorageUnavailableException;

/**
 * Created by usul on 23.3.2014.
 */
public class ContactsServiceImpl implements ContactsService {

    public static final String NAVIGATION_DIR = "naviterier";

    public static final String CONTACTS = "contacts.xml";

    private final Activity activity;

    private final FileService fileService;

    public ContactsServiceImpl(Activity activity) {
        this.activity = activity;
        this.fileService = new FileServiceImpl();
    }


    public List<Contact> openContacts() throws StorageUnavailableException, FileException, MarshallingException {
        if (!fileService.isExternalStorageReadable()) {
            throw new StorageUnavailableException(activity, activity.getString(R.string.stora_unavailable), activity.getString(R.string.sdcard_unreadable));
        }
        if (!fileService.isExternalStorageWritable()) {
            throw new StorageUnavailableException(activity, activity.getString(R.string.stora_unavailable), activity.getString(R.string.sdcard_unwritable));
        }

        File contacts_file = fileService.getExternalStorageFile(NAVIGATION_DIR, CONTACTS);

        if (!contacts_file.exists()) {
            throw new FileException(activity, activity.getString(R.string.file_not_exist), activity.getString(R.string.contacts_not_exist));
        }
        Serializer serializer = new Persister();
        Contacts contacts = null;
        try {
            contacts = serializer.read(Contacts.class, contacts_file);
        } catch (PersistenceException e){
            e.printStackTrace();
            throw new MarshallingException(activity, activity.getString(R.string.marshalling_exception), activity.getString(R.string.contact_xml_invalid));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contacts.getContactList();
    }

}
