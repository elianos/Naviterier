package cz.cvut.fel.jinocvla.naviterier.services.impl;

import android.app.Activity;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

import cz.cvut.fel.jinocvla.naviterier.services.TextToSpeechService;

/**
 * Created by usul on 13.3.14.
 */
public class TextToSpeechServiceImpl implements TextToSpeech.OnInitListener, TextToSpeechService{

    private final Activity activity;

    private TextToSpeech tts;

    public TextToSpeechServiceImpl(Activity activity) {
        this.activity = activity;
        this.tts = new TextToSpeech(activity, this);
    }


    public void read(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


    public void destroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            } else {

            }

        } else {

        }
    }
}
