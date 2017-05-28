package fi.oulu.tol.esde_2017_017.cwpclient017.model;

import android.media.AudioManager;
import android.media.ToneGenerator;

import java.util.Observable;
import java.util.Observer;


public class Signaller implements Observer {
    ToneGenerator toneGenerator = null;

    public Signaller () {
        toneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, 100);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg == CWPModel.CWPEvent.ELineUp)
            startDTMFTone();
        else
            stopDTMFTone();
    }

    private void startDTMFTone() {
        toneGenerator.startTone(ToneGenerator.TONE_DTMF_1);
    }

    private void stopDTMFTone() {
        toneGenerator.stopTone();
    }

    @Override
    public void finalize() {
        try {
            super.finalize();
            stopDTMFTone();
        } catch (Throwable e){}
    }
}
