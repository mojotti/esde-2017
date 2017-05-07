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
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWPControl;
import fi.oulu.tol.esde_2017_017.cwpclient017.cwprotocol.CWProtocolListener;
import fi.oulu.tol.esde_2017_017.cwpclient017.model.CWPModel;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class ControlFragment extends Fragment implements Observer {

    private ToggleButton connectButton;
    private CWPControl cwpControl;

    public ControlFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg == CWProtocolListener.CWPEvent.EConnected || arg == CWProtocolListener.CWPEvent.EDisconnected) {
            Toast.makeText(getActivity().getApplicationContext(),
                    arg.toString().substring(1,arg.toString().length()-2) + "ing...",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_control, container, false);
        connectButton = (ToggleButton) inflatedView.findViewById(R.id.connectButton);

        connectButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return changeConnectionStatus(event);
            }
        });
        return inflatedView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentActivity activity = getActivity();
        CWPProvider cwpProvider = (CWPProvider)activity;
        cwpControl = cwpProvider.getControl();
        cwpControl.addObserver(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cwpControl.deleteObserver(this);
        cwpControl = null;
    }

    private boolean changeConnectionStatus(MotionEvent event) {
        if (!cwpControl.isConnected() && event.getAction() == MotionEvent.ACTION_DOWN) {
            try {
                cwpControl.connect("Jee", 1, 1);
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }
        else if (cwpControl.isConnected() && event.getAction() == MotionEvent.ACTION_DOWN)
            try {
                cwpControl.disconnect();
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        return false;
    }
}
