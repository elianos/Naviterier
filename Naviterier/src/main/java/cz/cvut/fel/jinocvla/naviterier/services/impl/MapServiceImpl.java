package cz.cvut.fel.jinocvla.naviterier.services.impl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.widget.ArrayAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.PersistenceException;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.IOException;
import java.util.List;

import cz.cvut.fel.jinocvla.naviterier.R;
import cz.cvut.fel.jinocvla.naviterier.models.MapScheme;
import cz.cvut.fel.jinocvla.naviterier.models.MapsList;
import cz.cvut.fel.jinocvla.naviterier.models.Scheme;
import cz.cvut.fel.jinocvla.naviterier.services.FileService;
import cz.cvut.fel.jinocvla.naviterier.services.MapService;
import cz.cvut.fel.jinocvla.naviterier.services.exception.FileException;
import cz.cvut.fel.jinocvla.naviterier.services.exception.MarshallingException;
import cz.cvut.fel.jinocvla.naviterier.services.exception.StorageUnavailableException;
import cz.cvut.fel.jinocvla.naviterier.services.exception.UpdateException;

/**
 * Created by usul on 7.3.14.
 */
public class MapServiceImpl implements MapService {

    public static final String OUTDOOR_CONTEXT = "outdoor";

    public static final String INDOOR_CONTEXT = "indoor";

    private final String NAVITERIER_BASE_URL = "http://naviterier.appspot.com/";

    private final String BASE_DIR = "naviterier";

    private final String MAP_DIR = "maps";

    private final String MAP_LIST = "map_list.xml";

    private final String SERVICE_MAP_LIST = "map_list_service.xml";

    private final Activity activity;

    private final String folder;

    private final String context;

    private Serializer serializer;

    private final FileService fileService;

    private DownloadManager dm;


    public MapServiceImpl(Activity activity, String context) {
        this.activity = activity;
        this.context = context;
        this.folder = BASE_DIR + "/" + context;
        this.fileService = new FileServiceImpl();
    }


    private Serializer getSerializer() {
        if (this.serializer == null) {
            this.serializer  = new Persister();
        }
        return serializer;
    }



    public List<MapScheme> getMapList() throws StorageUnavailableException, FileException, MarshallingException, UpdateException {
        // Get the directory for the user's public pictures directory.
        if (!fileService.isExternalStorageReadable()) {
            throw new StorageUnavailableException(activity, activity.getString(R.string.stora_unavailable), activity.getString(R.string.sdcard_unreadable));
        }
        File file = fileService.getExternalStorageFile(folder, MAP_LIST);

        File dir = fileService.getExternalStorageFolder(folder);

        if (!dir.exists()) {
            if (!fileService.isExternalStorageWritable()) {
                throw new StorageUnavailableException(activity, activity.getString(R.string.stora_unavailable), activity.getString(R.string.sdcard_unwritable));
            }
            dir.mkdirs();
            fileService.getExternalStorageFolder(folder + "/" + MAP_DIR).mkdirs();

            throw new FileException(activity, activity.getString(R.string.file_not_exist), activity.getString(R.string.maps_list_unavailable));
        }

        if (!file.exists()) {
            file = fileService.getExternalStorageFile(folder, SERVICE_MAP_LIST);

            if (!file.exists()) {
                throw new FileException(activity, activity.getString(R.string.file_not_exist), activity.getString(R.string.maps_list_unavailable));
            } else {
                throw new UpdateException(activity, null, activity.getString(R.string.updating_list));
            }
        }



        return serializeSchemeList(file);
    }



    private List<MapScheme> serializeSchemeList(File file) throws MarshallingException {

        MapsList mapList = null;
        Serializer serializer = getSerializer();

        try {
            mapList = serializer.read(MapsList.class, file);
        } catch (PersistenceException e){
            throw new MarshallingException(activity, activity.getString(R.string.marshalling_exception), activity.getString(R.string.list_invalid_xml));
        } catch (Exception e) {
            e.printStackTrace();
        }
        File local_map;
        for(MapScheme mapScheme : mapList.getMapSchemeList()) {
            local_map = fileService.getExternalStorageFile(folder + "/" + MAP_DIR, mapScheme.getFileName());
            if (local_map.exists()) {
                mapScheme.setLocaleCopy(true);
            }
        }

        mapList.sort();

        return mapList.getMapSchemeList();
    }

    private List<MapScheme> serializeSchemeList(String file) throws MarshallingException {
        MapsList mapList = null;
        Serializer serializer = getSerializer();

        try {
            mapList = serializer.read(MapsList.class, file);
        } catch (PersistenceException e){
            e.printStackTrace();
            throw new MarshallingException(activity, activity.getString(R.string.marshalling_exception), activity.getString(R.string.list_invalid_xml));
        } catch (Exception e) {
            e.printStackTrace();
        }
        File local_map;
        for(MapScheme mapScheme : mapList.getMapSchemeList()) {
            local_map = fileService.getExternalStorageFile(folder + "/" + MAP_DIR, mapScheme.getFileName());
            if (local_map.exists()) {
                mapScheme.setLocaleCopy(true);
            }
        }

        return mapList.getMapSchemeList();
    }


    public void tryDownloadFile(final List<MapScheme> list, final ArrayAdapter<MapScheme> adapter) {

        final ProgressDialog progressDialog = new ProgressDialog(activity);

        progressDialog.setMessage(activity.getString(R.string.downloading_map_list));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        RequestQueue requestQueue = Volley.newRequestQueue(activity);

        String url = NAVITERIER_BASE_URL + context;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    File file = fileService.getExternalStorageFile(folder, SERVICE_MAP_LIST);
                    try {
                        fileService.overrideFileByString(file, response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    list.addAll(serializeSchemeList(response));
                    adapter.notifyDataSetChanged();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
                    alertDialog.setTitle(R.string.download_complete);
                    alertDialog.setMessage(R.string.map_scheme_downloaded);
                    alertDialog.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }
                    );
                    alertDialog.show();

                } catch (MarshallingException e) {
                    e.getFinishDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                FileException fileException = new FileException(activity, activity.getString(R.string.download_failed), activity.getString(R.string.unable_to_download_scheme));
                fileException.getFinishDialog();
            }
        });

        requestQueue.add(stringRequest);
        progressDialog.show();
    }


    public void tryUpdateFile(final List<MapScheme> list, final ArrayAdapter<MapScheme> adapter) {
        RequestQueue requestQueue = Volley.newRequestQueue(activity);

        String url = NAVITERIER_BASE_URL + context;

        final ProgressDialog progressDialog = new ProgressDialog(activity);

        progressDialog.setMessage(activity.getString(R.string.updating_map_list));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    File file = fileService.getExternalStorageFile(folder, SERVICE_MAP_LIST);
                    try {
                        fileService.overrideFileByString(file, response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    list.clear();
                    list.addAll(serializeSchemeList(response));
                    adapter.notifyDataSetChanged();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
                    alertDialog.setTitle(R.string.update_complete);
                    alertDialog.setMessage(R.string.map_scheme_updated);
                    alertDialog.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }
                    );
                    alertDialog.show();

                } catch (MarshallingException e) {
                    e.getFinishDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    progressDialog.dismiss();
                    File file = fileService.getExternalStorageFile(folder, SERVICE_MAP_LIST);
                    list.addAll(serializeSchemeList(file));
                    adapter.notifyDataSetChanged();

                } catch (MarshallingException e) {
                    e.printStackTrace();
                }
            }
        });

        requestQueue.add(stringRequest);
        progressDialog.show();
    }

    public List<MapScheme> tryUpdateList() throws MarshallingException {
        File file = fileService.getExternalStorageFile(folder, SERVICE_MAP_LIST);

        return serializeSchemeList(file);
    }


    public Long downloadMap(final MapScheme mapScheme, Activity activity) throws StorageUnavailableException {
        if (!fileService.isExternalStorageReadable()) {
            throw new StorageUnavailableException(activity, activity.getString(R.string.stora_unavailable), activity.getString(R.string.sdcard_unreadable));
        }
        if (!fileService.isExternalStorageWritable()) {
            throw new StorageUnavailableException(activity, activity.getString(R.string.stora_unavailable), activity.getString(R.string.sdcard_unwritable));
        }
        dm = (DownloadManager) activity.getSystemService(activity.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(mapScheme.getUrl()));

        request.setAllowedOverRoaming(false)
                .setDestinationInExternalPublicDir(folder + "/" + MAP_DIR,
                        mapScheme.getFileName());

        return dm.enqueue(request);
    }

    public Scheme openMap(MapScheme mapScheme) throws StorageUnavailableException, FileException, MarshallingException {
        if (!fileService.isExternalStorageReadable()) {
            throw new StorageUnavailableException(activity, activity.getString(R.string.stora_unavailable), activity.getString(R.string.sdcard_unreadable));
        }
        if (!fileService.isExternalStorageWritable()) {
            throw new StorageUnavailableException(activity, activity.getString(R.string.stora_unavailable), activity.getString(R.string.sdcard_unwritable));
        }
        File local_map = fileService.getExternalStorageFile(folder + "/" + MAP_DIR, mapScheme.getFileName());

        if (!local_map.exists()) {
            throw new FileException(activity, activity.getString(R.string.file_not_exist), activity.getString(R.string.map_not_exist));
        }
        Serializer serializer = getSerializer();
        Scheme scheme = null;
        try {
            scheme = serializer.read(Scheme.class, local_map);
        } catch (PersistenceException e){
            e.printStackTrace();
            throw new MarshallingException(activity, activity.getString(R.string.marshalling_exception), activity.getString(R.string.map_invalid_xml));
        } catch (Exception e) {

            e.printStackTrace();
        }

        scheme.setFileName(mapScheme.getFileName());

        scheme.sort();

        return scheme;
    }
}
