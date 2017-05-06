package fi.oulu.tol.esde_2017_017.cwpclient017.model;

import java.util.Observable;

import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWPControl;
import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWPMessaging;


public class CWPModel extends Observable implements CWPMessaging, CWPControl {
    public enum CWPState { Disconnected, Connected, LineUp, LineDown }
    private CWPState currentState = CWPState.Disconnected;

    private void changeStateAndNotifyObservers(CWPState currentState) {
        setChanged();
        notifyObservers(currentState);
    }

    public void lineUp(){
        currentState = CWPState.LineUp;
        changeStateAndNotifyObservers(currentState);
    }

    public void lineDown(){
        currentState = CWPState.LineDown;
        changeStateAndNotifyObservers(currentState);
    }

    public boolean lineIsUp() {
        return true;  // placeholder
    }

    public boolean isConnected(){
        return true;  // placeholder
    }

    public void connect(String serverAddr, int serverPort, int frequency) {
        currentState = CWPState.Connected;
        changeStateAndNotifyObservers(currentState);
    }

    public void disconnect() {
        currentState = CWPState.Disconnected;
        changeStateAndNotifyObservers(currentState);
    }

    public int frequency() {
        return 0;
    }

    public void setFrequency(int frequency) {
        frequency = CWPControl.DEFAULT_FREQUENCY;
    }
}
