package fi.oulu.tol.esde_2017_017.cwpclient017;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
    private Button frequencyButton;
    private EditText frequencyText;

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
        frequencyButton = (Button) inflatedView.findViewById(R.id.changeFreqButton);
        frequencyText = (EditText) inflatedView.findViewById(R.id.frequencyText);

        connectButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return changeConnectionStatus(event);
            }
        });
        frequencyButton.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return changeFrequency(event);
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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                String[] addressAndPort = prefs.getString("server_address", "").split(":");
                if (addressAndPort.length == 2) {
                    String address = addressAndPort[0];
                    int port = Integer.valueOf(addressAndPort[1]);
                    cwpControl.connect(address, port, CWPControl.DEFAULT_FREQUENCY);
                } else {
                    //???
                }
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

    private boolean changeFrequency(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int new_frequency = Integer.parseInt(frequencyText.getText().toString());
            if (new_frequency != cwpControl.getFrequency()) {
                try {
                    cwpControl.setFrequency(new_frequency);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            SharedPreferences.Editor edit = sharedPref.edit();
            edit.putString("frequency", Integer.toString(new_frequency));
            edit.commit();
        }
        return false;
    }

}
