package cs246.sara.caretrackerapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;



public class Camera2 extends AppCompatActivity {

    // Values for the actions
    public static final int CAMERA_REQUEST = 10;

    // Get the variable to hold the picture
    private ImageView imgPhoto;

    // Get the photo
    public Bitmap cameraImage;

    // The new button
    private Button uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);

        // Get access to the image view
        imgPhoto = (ImageView) findViewById(R.id.photo);
    }

    public void takePhoto(View v) {

        // A toast to confirm that the camera has started
        Toast.makeText(this, "clicked", Toast.LENGTH_LONG).show();

        // An intent to start the camera (the system camera by default)
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Start the camera
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {

                // Put the photo into a bitmap variable
                cameraImage = (Bitmap) data.getExtras().get("data");

                // Draw the photo on the image view (The blue square)
                imgPhoto.setImageBitmap(cameraImage);
            }
        }

        // Start the call
        uploadPhoto();
    }

    public void uploadPhoto() {

        // Assign the button to the view
        uploadButton = (Button) findViewById(R.id.button2);

        // Add event listener (onClick)
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // New activity
                Intent uploader = new Intent(Camera2.this, uploader.class);

                // Start the activity
                startActivity(uploader);
            }
        });
    }
}
