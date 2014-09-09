package cz.cvut.fel.jinocvla.naviterier.services.impl;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import cz.cvut.fel.jinocvla.naviterier.R;
import cz.cvut.fel.jinocvla.naviterier.activities.main.SchemeActivity;
import cz.cvut.fel.jinocvla.naviterier.models.Constants;
import cz.cvut.fel.jinocvla.naviterier.models.Scheme;
import cz.cvut.fel.jinocvla.naviterier.services.SchemeStorageService;

/**
 * Created by usul on 23.3.2014.
 */
public class SchemeStorageServiceImpl implements SchemeStorageService {

    private static SchemeStorageServiceImpl INSTANCE;

    private Activity activity;

    private Gson gson;

    private SchemeStorageServiceImpl(Activity activity) {
        this.activity = activity;
        this.gson = new Gson();
    }

    public static SchemeStorageServiceImpl getInstance(Activity activity) {
        if (INSTANCE == null) {
            INSTANCE = new SchemeStorageServiceImpl(activity);
        }
        return INSTANCE;
    }

    public void storeContext(SchemeActivity schemeActivity, SchemeActivity.State state) {
        String context;
        switch (state) {
            case COMPLETE:
                context = Constants.COMPLETE_NAV;
                break;
            case PROGRESS:
                context = Constants.PROGRESS_NAV;
                break;
            default:
                context = Constants.PROGRESS_NAV;
        }


        // Save to property
        SharedPreferences sharedPref = activity.getSharedPreferences(context, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();


        if (schemeActivity.getScheme() == null) {
            editor.remove(activity.getString(R.string.scheme_name));
        } else {
            editor.putString(activity.getString(R.string.scheme_name), gson.toJson(schemeActivity.getScheme()));
        }

        editor.putString(activity.getString(R.string.scheme_type), activity.getString(R.string.outdoor));

        if (schemeActivity.getFrom() == null) {
            editor.remove(activity.getString(R.string.scheme_from));
        } else {
            editor.putLong(activity.getString(R.string.scheme_from), schemeActivity.getFrom());
        }

        if (schemeActivity.getTo() == null) {
            editor.remove(activity.getString(R.string.scheme_to));
        } else {
            editor.putLong(activity.getString(R.string.scheme_to), schemeActivity.getTo());
        }

        if (schemeActivity.getCurrent() == null) {
            editor.remove(activity.getString(R.string.current_step));
        } else {
            editor.putInt(activity.getString(R.string.current_step), schemeActivity.getCurrent());
        }

        if (schemeActivity.getClassName() == null) {
            editor.remove(activity.getString(R.string.class_name));
        } else {
            editor.putString(activity.getString(R.string.class_name), schemeActivity.getClassName().getName());
        }
        editor.commit();
    }

    @Override
    public void storeCurrentPosition(SchemeActivity schemeActivity, SchemeActivity.State state) {
        String context;
        switch (state) {
            case COMPLETE:
                context = Constants.COMPLETE_NAV;
                break;
            case PROGRESS:
                context = Constants.PROGRESS_NAV;
                break;
            default:
                context = Constants.PROGRESS_NAV;
        }


        // Save to property
        SharedPreferences sharedPref = activity.getSharedPreferences(context, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (schemeActivity.getCurrent() == null) {
            editor.remove(activity.getString(R.string.current_step));
        } else {
            editor.putInt(activity.getString(R.string.current_step), schemeActivity.getCurrent());
        }


        editor.commit();
    }

    public void loadContext(SchemeActivity schemeActivity, SchemeActivity.State state) {
        String context;
        switch (state) {
            case COMPLETE:
                context = Constants.COMPLETE_NAV;
                break;
            case PROGRESS:
                context = Constants.PROGRESS_NAV;
                break;
            default:
                context = Constants.PROGRESS_NAV;
        }


        // Save to property
        SharedPreferences sharedPref = activity.getSharedPreferences(context, Context.MODE_PRIVATE);

        String scheme = sharedPref.getString(activity.getString(R.string.scheme_name), "");
        if (!scheme.isEmpty()) {
            schemeActivity.setScheme(gson.fromJson(scheme, Scheme.class));
        }
        Long tmp = sharedPref.getLong(activity.getString(R.string.scheme_from), -1l);
        if (tmp != -1l) {
            schemeActivity.setFrom(tmp);
        }

        tmp = sharedPref.getLong(activity.getString(R.string.scheme_to), -1l);
        if (tmp != -1l) {
            schemeActivity.setTo(tmp);
        }

        Integer itmp = sharedPref.getInt(activity.getString(R.string.current_step), -1);
        if (itmp != -1) {
            schemeActivity.setCurrent(itmp);
        }

    }

    public boolean isSchemeStored(SchemeActivity.State state) {
        String context;
        switch (state) {
            case COMPLETE:
                context = Constants.COMPLETE_NAV;
                break;
            case PROGRESS:
                context = Constants.PROGRESS_NAV;
                break;
            default:
                context = Constants.PROGRESS_NAV;
        }


        // Save to property
        SharedPreferences sharedPref = activity.getSharedPreferences(context, Context.MODE_PRIVATE);


        String class_name = sharedPref.getString(activity.getString(R.string.class_name), "");
        return !class_name.isEmpty();
    }


    public Class navClass(SchemeActivity.State state) {
        String context;
        switch (state) {
            case COMPLETE:
                context = Constants.COMPLETE_NAV;
                break;
            case PROGRESS:
                context = Constants.PROGRESS_NAV;
                break;
            default:
                context = Constants.PROGRESS_NAV;
        }


        // Save to property
        SharedPreferences sharedPref = activity.getSharedPreferences(context, Context.MODE_PRIVATE);


        String class_name = sharedPref.getString(activity.getString(R.string.class_name), "");
        if (!class_name.isEmpty()) {
            try {
                return Class.forName(class_name);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
