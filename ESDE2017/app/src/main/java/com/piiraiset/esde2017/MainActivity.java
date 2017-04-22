package com.piiraiset.esde2017;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageButton statusIndicator = (ImageButton)findViewById(R.id.statusIndicator);

        statusIndicator.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return changeIndicatorStatus(event, statusIndicator);
            }
        });
    }

    private boolean changeIndicatorStatus(MotionEvent event, ImageButton statusIndicator) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            statusIndicator.setImageResource(R.drawable.receiving);
        }
        else if (event.getAction() == MotionEvent.ACTION_UP)
            statusIndicator.setImageResource(R.drawable.idle);
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
