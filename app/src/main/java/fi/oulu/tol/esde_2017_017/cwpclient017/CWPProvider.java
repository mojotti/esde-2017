package fi.oulu.tol.esde_2017_017.cwpclient017;

import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWPControl;
import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWPMessaging;


public interface CWPProvider {
    CWPMessaging getMessaging();
    CWPControl getControl();
}
