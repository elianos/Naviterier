package cz.cvut.fel.jinocvla.naviterier.services;

import android.app.Activity;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/**
 * Created by usul on 13.3.14.
 */
public interface TextToSpeechService {

    public void read(String text);

    public void destroy();


}
