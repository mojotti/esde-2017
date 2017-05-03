package fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol;

import java.io.IOException;
import java.util.Observer;


public interface CWPMessaging {
    public void addObserver(Observer observer);
    public void deleteObserver(Observer observer);
    public void lineUp() throws IOException;
    public void lineDown() throws IOException;
    public boolean isConnected();
    public boolean lineIsUp();
}
