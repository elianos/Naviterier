package cz.cvut.fel.jinocvla.naviterier.services;

import java.util.List;

import cz.cvut.fel.jinocvla.naviterier.models.Contact;
import cz.cvut.fel.jinocvla.naviterier.services.exception.FileException;
import cz.cvut.fel.jinocvla.naviterier.services.exception.MarshallingException;
import cz.cvut.fel.jinocvla.naviterier.services.exception.StorageUnavailableException;

/**
 * Created by usul on 25.4.2014.
 */
public interface ContactsService {

    public List<Contact> openContacts() throws StorageUnavailableException, FileException, MarshallingException;
}
