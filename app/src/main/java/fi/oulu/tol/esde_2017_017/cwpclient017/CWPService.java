package fi.oulu.tol.esde_2017_017.cwpclient017;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWPControl;
import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWPMessaging;
import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWProtocolImplementation;
import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWProtocolListener;
import fi.oulu.tol.esde_2017_017.cwpclient017.model.CWPModel;
import fi.oulu.tol.esde_2017_017.cwpclient017.model.Signaller;

public class CWPService extends Service implements CWPProvider, Observer {
    public CWPModel cwpModel = null;
    public CWPBinder cwpBinder = new CWPBinder();
    private Signaller signaller;
    private int clientCounter = 0;
    public CWPService() {
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg == CWProtocolListener.CWPEvent.ELineUp && clientCounter == 0) {
            showNotification();
        }
    }

    private void showNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.receiving)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText(getResources().getString(R.string.line_up));
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(111, mBuilder.build());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        cwpModel = new CWPModel();
        cwpModel.addObserver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            cwpModel.disconnect();
            cwpModel = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        //super.onBind(intent);
        return cwpBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public CWPControl getControl() {
        return cwpModel;
    }

    public CWPMessaging getMessaging() {
        return cwpModel;
    }

    public class CWPBinder extends Binder {
        CWPService getService() {
            return CWPService.this;
        }
    }

    public void startUsing() {
        clientCounter++;
        if (clientCounter == 1) {
            signaller = new Signaller();
            cwpModel.addObserver(signaller);
        }
    }

    public void stopUsing() {
        clientCounter--;
        if (clientCounter == 0) {
            cwpModel.deleteObserver(signaller);
            signaller = null;
        }
    }
}
