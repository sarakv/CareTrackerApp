package cs246.sara.caretrackerapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Window;

/**
 * Created by Pedro on 08/12/2017.
 * This will contain information for the "About" section of the app
 */

public class About extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the activity layout
        setContentView(R.layout.activiy_about);

        // The Display Metrics for the screen size
        DisplayMetrics dm = new DisplayMetrics();

        // Get the screen's size
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        // Get the width and height in pixels
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        // Set the display size
        getWindow().setLayout( (int)(width * .8), (int) (height * .6));
    }
}
