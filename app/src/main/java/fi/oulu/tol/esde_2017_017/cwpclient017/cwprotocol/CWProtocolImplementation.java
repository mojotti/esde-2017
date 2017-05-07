package fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.util.Log;

public class CWProtocolImplementation implements CWPControl, CWPMessaging, Runnable {
    public enum CWPState { Disconnected, Connected, LineUp, LineDown }
    private CWPState currentState = CWPState.Disconnected;
    private CWPState nextState = currentState;
    private int frequency = DEFAULT_FREQUENCY;
    private CWPConnectionReader connectionReader = null;
    private int messageValue;
    private Handler receiveHandler = new Handler();
    private CWProtocolListener listener = null;

    public CWProtocolImplementation (CWProtocolListener l) {
        listener = l;
    }

    public void addObserver(Observer observer) {}
    public void deleteObserver(Observer observer) {}
    public void connect(String serverAddr, int serverPort, int frequency) throws IOException {
        connectionReader = new CWPConnectionReader(this);
        connectionReader.startReading();
        listener.onEvent(CWProtocolListener.CWPEvent.EConnected, 0);
    }
    public void disconnect() throws IOException {
        try {
            connectionReader.stopReading();
            connectionReader = null;
            listener.onEvent(CWProtocolListener.CWPEvent.EDisconnected, 0);
        } catch (InterruptedException e) {}
    }
    public boolean isConnected() {
        return currentState != CWPState.Disconnected;
    }
    public void setFrequency(int f) throws IOException {
        frequency = f;
    }
    public int getFrequency() {
        return frequency;
    }

    public void lineUp() throws IOException {
        currentState = CWPState.LineUp;
        listener.onEvent(CWProtocolListener.CWPEvent.ELineUp, 0);
    }
    public void lineDown() throws IOException {
        currentState = CWPState.LineDown;
        listener.onEvent(CWProtocolListener.CWPEvent.ELineDown, 0);
    }
    public boolean lineIsUp() {
        return currentState == CWPState.LineUp;
    }

    public CWPState getCurrentState() {
        return currentState;
    }

    @Override
    public void run() {
        Log.d("Main Thread: ", "State is changing. New state: " + nextState);
        switch (nextState) {
            case Connected:
                currentState = nextState;
                listener.onEvent(CWProtocolListener.CWPEvent.EConnected, 0);
                break;
            case Disconnected:
                currentState = nextState;
                listener.onEvent(CWProtocolListener.CWPEvent.EDisconnected, 0);
                break;
            case LineUp:
                currentState = nextState;
                listener.onEvent(CWProtocolListener.CWPEvent.ELineUp, 0);
                break;
            case LineDown:
                currentState = nextState;
                listener.onEvent(CWProtocolListener.CWPEvent.ELineDown, 0);
                break;
        }
    }

    private class CWPConnectionReader extends Thread {
        private boolean isRunning = false;
        private Runnable myProcessor = null;
        private static final String TAG = "CWPReader";

        // Used before networking for timing cw signals
        private Timer myTimer = null;
        private TimerTask myTimerTask = null;

        CWPConnectionReader(Runnable processor) {
            myProcessor = processor;
        }
        void startReading() {
            isRunning = true;
            start();
        }

        void stopReading() throws InterruptedException {
            myTimer.cancel();
            isRunning = false;
            myTimer = null;
            myTimerTask = null;
            currentState = CWPState.Disconnected;
        }
        private void doInitialize() throws InterruptedException {
            currentState = CWPState.Connected;
            myTimer = new Timer();
            myTimerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (lineIsUp())
                            changeProtocolState(CWPState.LineDown, 0);
                        else
                            changeProtocolState(CWPState.LineUp, 0);
                    } catch (InterruptedException e) {

                    }
                }
            };
            myTimer.scheduleAtFixedRate(myTimerTask, 1000, 5000);
        }

        private void changeProtocolState(CWPState state, int param) throws InterruptedException{
            Log.d(TAG, "Change protocol state to: " +  state);
            nextState = state;
            messageValue = param;
            receiveHandler.post(myProcessor);
        }

        @Override
        public void run() {
            try {
                doInitialize();
                //super.run();
                changeProtocolState(CWPState.LineDown, 0);

                while(isRunning) {

                }
            } catch (InterruptedException e) {

            }
        }
    }
}
