package fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol;

public interface CWProtocolListener {
    public enum CWPEvent {EConnected, EChangedFrequency, ELineUp, ELineDown, EDisconnected};
    public void onEvent(CWPEvent event, int param);
}