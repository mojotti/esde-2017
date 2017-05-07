package fi.oulu.tol.esde_2017_017.cwpclient017.model;

import java.io.IOException;
import java.util.Observable;

import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWPControl;
import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWPMessaging;
import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWProtocolImplementation;
import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWProtocolListener;


public class CWPModel extends Observable implements CWPMessaging, CWPControl, CWProtocolListener {
    private CWProtocolImplementation protocol = new CWProtocolImplementation(this);

    public void lineUp () throws IOException {
        if (isConnected())
            protocol.lineUp();
    }

    public void lineDown() throws IOException {
        if (isConnected())
            protocol.lineDown();
    }

    public boolean lineIsUp() {
        return protocol.lineIsUp();
    }

    public boolean isConnected(){
        return protocol.isConnected();
    }

    public void connect(String serverAddr, int serverPort, int frequency) throws IOException {
        protocol.connect(serverAddr, serverPort, frequency);
    }

    public void disconnect() throws IOException {
        if (isConnected())
            protocol.disconnect();
    }

    public int getFrequency() {
        return protocol.getFrequency();
    }

    public void setFrequency(int frequency) throws IOException {
        protocol.setFrequency(frequency);
    }

    public void onEvent(CWProtocolListener.CWPEvent event, int param) {
        setChanged();
        notifyObservers(event);
    }
}
