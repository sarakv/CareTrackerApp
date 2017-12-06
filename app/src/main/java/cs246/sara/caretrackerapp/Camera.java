package cs246.sara.caretrackerapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class Camera extends AppCompatActivity {

    public static final int CAMERA_REQUEST = 10;
    private ImageView imgPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Get access to the image view
        imgPhoto = (ImageView) findViewById(R.id.photo);
    }

    /**
     * Takes a photo.
     *
     * @param v
     */
    public void takePhoto(View v) {
        Toast.makeText(this, "clicked", Toast.LENGTH_LONG).show();

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                Bitmap cameraImage = (Bitmap) data.getExtras().get("data");

                imgPhoto.setImageBitmap(cameraImage);
            }
        }
    }
}
