package fi.oulu.tol.esde_2017_017.cwpclient017.model;

import android.util.Log;

import java.util.Observable;
import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWPMessaging;


public class CWPModel extends Observable implements CWPMessaging {
    public enum CWPState { Disconnected, Connected, LineUp, LineDown }
    private CWPState currentState = CWPState.Connected;

    public void lineUp(){
        if (lineIsUp()) {
            currentState = CWPState.LineUp;
            setChanged();
            notifyObservers(currentState);
        }
    }

    public void lineDown(){
        if (!lineIsUp()) {
            currentState = CWPState.LineDown;
            setChanged();
            notifyObservers(currentState);
        }
    }

    public boolean lineIsUp() {
        return true;  // placeholder
    }

    public boolean isConnected(){
        return true;  // placeholder
    }
}
