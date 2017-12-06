package cs246.sara.caretrackerapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Expense extends AppCompatActivity {

    // Get the button for the camera
    View camButton = findViewById(R.id.imageButton2);
    private Button camera = (Button) camButton;

    /**
     *  START CAMERA
     *  Will contain the Camera onClick listener. This method will also
     *  be listed and set inside the onCreate method.
     */
    private void startCamera() {

        // The onClick Listener
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Expense.this, Camera.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        // Remember to initialize the camera function
        startCamera();
    }
}
