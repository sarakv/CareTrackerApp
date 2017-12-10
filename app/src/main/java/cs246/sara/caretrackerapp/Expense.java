package cs246.sara.caretrackerapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.gson.Gson;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class Expense extends AppCompatActivity {
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    // The button for the camera
    private ImageButton camera = null;
    private java.io.File imageToSave = null;
    private Bitmap thumbnail = null;
    private Intent resultIntent = null;
    private SheetData sheetData = null;
    private String imgTimeStamp = null;
    private String mCurrentPhotoPath = null;
    private boolean isPictureTaken = false;
    private static final String[] SCOPES = { DriveScopes.DRIVE};
    private GoogleAccountCredential mCredential = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);
        mCredential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(SCOPES));
        mCredential.setSelectedAccountName(MyPreferences.getString(this, MainActivity.PREF_ACCOUNT_NAME, ""));
        resultIntent = new Intent();
        startCamera();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CAPTURE_IMAGE:
                // Called after a photo has been taken.
                if (resultCode == Activity.RESULT_OK) {
                    imageToSave = new java.io.File(mCurrentPhotoPath);
                    Uri photoURI = FileProvider.getUriForFile(Expense.this,
                            "cs246.sara.caretrackerapp", imageToSave);
                    ((ImageView) findViewById(R.id.image_preview)).setImageURI(photoURI);
                    isPictureTaken = true;
                }
                break;
        }
    }

    private java.io.File createImageFile() throws IOException {
        String user = MyPreferences.getString(this, MainActivity.DISPLAY_NAME, "");
        java.io.File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        java.io.File image = java.io.File.createTempFile(
                user + " CareTrackerEvent " + imgTimeStamp,
                ".jpg",
                storageDir
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     *  START CAMERA
     *  Will contain the camera onClick listener. This method will also
     *  be listed and set inside the onCreate method.
     */
    private void startCamera() {
        // Get the button for the camera
        camera = (ImageButton) findViewById(R.id.imageButton2);

        // The onClick Listener
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(Expense.this.getPackageManager()) != null) {
                    // Create the File where the photo should go
                    java.io.File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File

                    }
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(Expense.this,
                                "cs246.sara.caretrackerapp", photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE);
                    } else {
                        Toast.makeText(Expense.this,
                                "Failed create temporary file to store image",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    public void onCancelListener(View v) {
        // Return to MainActivity
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onOkClick(View v) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH-mm", Locale.US);
        Date currentTime = new Timestamp(System.currentTimeMillis());
        imgTimeStamp = sdf2.format(currentTime);
        final String timestamp = sdf1.format(currentTime);
        final String user = MyPreferences.getString(this, MainActivity.DISPLAY_NAME, "");
        final String label = ((EditText) findViewById(R.id.editLabel)).getText().toString();;
        final String description = ((EditText) findViewById(R.id.expenseDescription)).getText().toString();
        final String saidToClient = "";
        final String clientSaid = "";
        final String imgLink = "";
        final String message = user + " :: "
                + timestamp
                + "\n"
                + label
                + ": "
                + description;
        if (label.length() != 0 && description.length() != 0) {
            new AlertDialog.Builder( Expense.this )
                    .setTitle("Confirm")
                    .setMessage("Sending entry:\n" + message)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sheetData = new SheetData(user, timestamp, label, description,
                                    saidToClient, clientSaid, imgLink);
                            new WriteImgToDriveTask(mCredential).execute();
                        }
                    }).show();
        } else {
            new AlertDialog.Builder( Expense.this )
                    .setTitle("Error")
                    .setMessage("Please enter a label and a description!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        }
    }

    private class WriteImgToDriveTask extends AsyncTask<Void, Void, String> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;
        private ProgressDialog mProgress;
        private boolean isCancelled = false;

        WriteImgToDriveTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("CareTrackerApp").build();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return saveFileToDrive();
            } catch (Exception e) {
                mLastError = e;
                isCancelled = true;
                cancel(true);
                return null;
            }
        }

        /** Create a new file and save it to Drive. */
        private String saveFileToDrive() throws IOException {
            String filename = sheetData.getUser() + " CareTrackerEvent " + imgTimeStamp + ".jpg";
            File fileMetadata = new File();
            fileMetadata.setName(filename);
            FileContent fileContent = new FileContent("image/jpeg", imageToSave);
            File image = mService.files().create(fileMetadata, fileContent)
                    .setFields("id")
                    .execute();

            Permission userPermission = new Permission()
                    .setType("user")
                    .setRole("writer")
                    .setEmailAddress("care.tracker246@gmail.com");
            mService.permissions().create(image.getId(), userPermission).execute();

            String link = "https://drive.google.com/file/d/"
                    + image.getId()
                    + "/view?usp=sharing";
            return link;
        }

        @Override
        protected void onPreExecute() {
            mProgress = new ProgressDialog(Expense.this);
            mProgress.setMessage("Saving image to Google Drive...");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String result) {
            mProgress.dismiss();
            Gson gson = new Gson();
            sheetData.setImgLink(result);
            String dataJson = gson.toJson(sheetData);
            resultIntent.putExtra(MainActivity.SHEET_DATA, dataJson);
            setResult(RESULT_OK, resultIntent);
            finish();
        }

        @Override
        protected void onCancelled() {
            mProgress.dismiss();
            Toast.makeText(Expense.this,
                    "Failed to save image",
                    Toast.LENGTH_LONG).show();
        }
    }
}
