package cz.cvut.fel.jinocvla.naviterier.services;

import android.app.Activity;
import android.widget.ArrayAdapter;

import java.util.List;

import cz.cvut.fel.jinocvla.naviterier.models.MapScheme;
import cz.cvut.fel.jinocvla.naviterier.models.Scheme;
import cz.cvut.fel.jinocvla.naviterier.services.exception.FileException;
import cz.cvut.fel.jinocvla.naviterier.services.exception.MarshallingException;
import cz.cvut.fel.jinocvla.naviterier.services.exception.StorageUnavailableException;
import cz.cvut.fel.jinocvla.naviterier.services.exception.UpdateException;

/**
 * Created by usul on 25.4.2014.
 */
public interface MapService {

    public List<MapScheme> getMapList() throws StorageUnavailableException, FileException, MarshallingException, UpdateException;

    public void tryDownloadFile(final List<MapScheme> list, final ArrayAdapter<MapScheme> adapter);

    public void tryUpdateFile(final List<MapScheme> list, final ArrayAdapter<MapScheme> adapter);

    public List<MapScheme> tryUpdateList() throws MarshallingException;

    public Long downloadMap(final MapScheme mapScheme, Activity activity) throws StorageUnavailableException;

    public Scheme openMap(MapScheme mapScheme) throws StorageUnavailableException, FileException, MarshallingException;
}
