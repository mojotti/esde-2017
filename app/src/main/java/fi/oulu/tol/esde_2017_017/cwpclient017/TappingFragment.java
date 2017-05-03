package fi.oulu.tol.esde_2017_017.cwpclient017;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.Observable;

import fi.oulu.tol.esde_2017_017.cwpclient017.model.CWPModel;


public class TappingFragment extends Fragment implements java.util.Observer {

    public ImageButton statusIndicator;

    public TappingFragment() {
        // Required empty public constructor
    }

    public void update(Observable obs, Object obj) {
        if(obj == CWPModel.CWPState.LineDown) {
            statusIndicator.setImageResource(R.drawable.receiving);
        }
        if(obj == CWPModel.CWPState.LineUp) {
            statusIndicator.setImageResource(R.drawable.idle);
        }
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
                return changeIndicatorStatus(event, statusIndicator);
            }
        });
        return inflatedView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private boolean changeIndicatorStatus(MotionEvent event, ImageButton statusIndicator) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            statusIndicator.setImageResource(R.drawable.receiving);
        }
        else if (event.getAction() == MotionEvent.ACTION_UP)
            statusIndicator.setImageResource(R.drawable.idle);
        return false;
    }
}
