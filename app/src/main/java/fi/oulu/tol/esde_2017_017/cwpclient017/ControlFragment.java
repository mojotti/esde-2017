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
    private String currentFrequency;

    public ControlFragment() {
        // Required empty public constructor
    }

    public void setControl (CWPControl control) {
        cwpControl = control;
        control.addObserver(this);
        updateView(getView());
    }

    public void updateView(View view) {
        if (view != null) {
            connectButton = (ToggleButton) view.findViewById(R.id.connectButton);
            if (cwpControl != null)
                connectButton.setChecked(cwpControl.isConnected());

            frequencyButton = (Button) view.findViewById(R.id.changeFreqButton);
            frequencyText = (EditText) view.findViewById(R.id.frequencyText);

            connectButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return changeConnectionStatus(event);
                }
            });
            frequencyButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return changeFrequency(event);
                }
            });

            SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            currentFrequency = (shared.getString("frequency", ""));
            frequencyText.setText(currentFrequency);
        }
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
    public void onStart() {
        super.onStart();
        updateView(getView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (cwpControl != null) {
            cwpControl.deleteObserver(this);
            cwpControl = null;
        }
    }

    private boolean changeConnectionStatus(MotionEvent event) {
        if (cwpControl != null && !cwpControl.isConnected() && event.getAction() == MotionEvent.ACTION_DOWN) {
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
        else if (cwpControl != null && cwpControl.isConnected() && event.getAction() == MotionEvent.ACTION_DOWN)
            try {
                cwpControl.disconnect();
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        return false;
    }

    private boolean changeFrequency(MotionEvent event) {
        if (cwpControl != null && event.getAction() == MotionEvent.ACTION_DOWN) {
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
            frequencyText.setText(Integer.toString(new_frequency));
        }
        return false;
    }

}
