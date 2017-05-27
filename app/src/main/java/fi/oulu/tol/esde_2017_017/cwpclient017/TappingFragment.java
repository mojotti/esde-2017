package fi.oulu.tol.esde_2017_017.cwpclient017;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWPMessaging;
import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWProtocolListener;
import fi.oulu.tol.esde_2017_017.cwpclient017.model.CWPModel;


public class TappingFragment extends Fragment implements Observer {

    public ImageButton statusIndicator;
    public CWPMessaging cwpMessaging;

    public TappingFragment() {
        // Required empty public constructor
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg == CWProtocolListener.CWPEvent.ELineUp) // TODO: fix this. use Message class instead
            statusIndicator.setImageResource(R.drawable.receiving);
        else if (arg == CWProtocolListener.CWPEvent.ELineDown || arg == CWProtocolListener.CWPEvent.EConnected)
            statusIndicator.setImageResource(R.drawable.idle);
        else if (arg == CWProtocolListener.CWPEvent.EDisconnected)
            statusIndicator.setImageResource(R.drawable.offline);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_tapping, container, false);
        statusIndicator = (ImageButton)inflatedView.findViewById(R.id.statusIndicator);

        statusIndicator.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return changeMessagingStatus(event);
            }
        });
        return inflatedView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentActivity activity = getActivity();
        CWPProvider cwpProvider = (CWPProvider)activity;
        cwpMessaging = cwpProvider.getMessaging();
        cwpMessaging.addObserver(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cwpMessaging.deleteObserver(this);
        cwpMessaging = null;
    }

    private boolean changeMessagingStatus(MotionEvent event) {
        if (cwpMessaging != null && event.getAction() == MotionEvent.ACTION_DOWN) {
            try {
                cwpMessaging.lineUp();
            } catch (IOException ie) {
                Toast.makeText(getActivity().getApplicationContext(),
                        "Could not send message to server: " + ie.getLocalizedMessage(),
                        Toast.LENGTH_LONG).show();
                ie.printStackTrace();
            }
        }
        else if (cwpMessaging != null && event.getAction() == MotionEvent.ACTION_UP)
            try {
                cwpMessaging.lineDown();
            } catch (IOException ie) {
                Toast.makeText(getActivity().getApplicationContext(),
                        "Could not send message to server: " + ie.getLocalizedMessage(),
                        Toast.LENGTH_LONG).show();
                ie.printStackTrace();
            }
        return false;
    }
}
