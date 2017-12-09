package cs246.sara.caretrackerapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;


public class Expense extends AppCompatActivity {

    // The button for the camera
    private ImageButton camera = null;

    /**
     *  START CAMERA
     *  Will contain the Camera onClick listener. This method will also
     *  be listed and set inside the onCreate method.
     */
    private void startCamera() {
        // Get the button for the camera
        camera = (ImageButton) findViewById(R.id.imageButton2);

        // The onClick Listener
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Expense.this, Camera.class);
                startActivity(intent);
            }
        });

    }

    public void onCancelListener(View v) {
        // Return to MainActivity
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);
        startCamera();
    }
}
