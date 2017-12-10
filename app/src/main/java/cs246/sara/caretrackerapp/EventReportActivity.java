package cs246.sara.caretrackerapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.gson.Gson;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * Gathers special event data from user to send to Google Sheet. This is an
 * alternative way of sending one-time data that may be different each time. Provides functionality
 * to capture an image that will be sent alongside the data.
 */
public class EventReportActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private ImageButton camera = null;
    private java.io.File imageToSave = null;
    private SheetData sheetData = null;
    private String imgTimeStamp = null;
    private String mCurrentPhotoPath = null;
    private boolean isPictureTaken = false;
    private static final String[] SCOPES = { DriveScopes.DRIVE};
    private GoogleAccountCredential mCredential = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_report);
        // Initialize google credential in order to store image
        mCredential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(SCOPES));
        mCredential.setSelectedAccountName(MyPreferences.getString(this, MainActivity.PREF_ACCOUNT_NAME, ""));
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
                    Uri photoURI = FileProvider.getUriForFile(EventReportActivity.this,
                            "cs246.sara.caretrackerapp", imageToSave);
                    ((ImageView) findViewById(R.id.image_preview)).setImageURI(photoURI);
                    isPictureTaken = true;
                }
                break;
        }
    }

    /**
     * Obtains a File reference to be used to store the image
     * @return the File reference where the image should be stored.
     * @throws IOException if an error occurred while creating the file
     */
    private java.io.File createImageFile() throws IOException {
        // obtain user name
        String user = MyPreferences.getString(this, MainActivity.DISPLAY_NAME, "");
        // obtain path of where to save file
        java.io.File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // filename = {user name} CareTracker Event yyyy-MM-dd hh-mm.jpg
        String filename = user + " CareTrackerEvent " + imgTimeStamp;
        String extension = ".jpg";
        // create file reference
        java.io.File image = java.io.File.createTempFile(
                filename,
                extension,
                storageDir
        );
        // save path of File reference for later
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     *  Initializes the camera functionality
     */
    private void startCamera() {
        // Get the button for the camera
        camera = (ImageButton) findViewById(R.id.imageButton2);
        // Set the onClick Listener
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to ask the OS to take a picture
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Check that device is able to take a picture
                if (intent.resolveActivity(EventReportActivity.this.getPackageManager()) != null) {
                    // Create the File where the photo should go
                    java.io.File photoFile = null;
                    try {
                        // Get location to save picture.
                        photoFile = createImageFile();
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(EventReportActivity.this,
                                    "cs246.sara.caretrackerapp", photoFile);
                            // Ask the system to take the picture
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE);
                        } else {
                            Toast.makeText(EventReportActivity.this,
                                    "Failed create temporary file to store image",
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Toast.makeText(EventReportActivity.this,
                                "Failed create temporary file to store image",
                                Toast.LENGTH_LONG).show();
                    }

                } else {
                    // Device cannot take pictures...
                    Toast.makeText(EventReportActivity.this,
                            "Cannot take pictures on this device...",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    /**
     * onClick Listener for cancel button
     * @param v reference to the view that was clicked (required)
     */
    public void onCancelListener(View v) {
        // Return to MainActivity
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * onClick Listener for ok button
     * @param v reference to the view that was clicked (required)
     */
    public void onOkClick(View v) {
        // obtain timestamp for log and for image
        Date currentTime = new Timestamp(System.currentTimeMillis());
        // formatting for Google Sheet
        SimpleDateFormat sdf1 = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);
        // formatting for image filename
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH-mm", Locale.US);

        imgTimeStamp = sdf2.format(currentTime);

        //obtain user input
        final String label = ((EditText) findViewById(R.id.editLabel)).getText().toString();;
        final String description = ((EditText) findViewById(R.id.expenseDescription)).getText().toString();

        // gather data to send
        sheetData = new SheetData(
                MyPreferences.getString(this, MainActivity.DISPLAY_NAME, ""),
                sdf1.format(currentTime), //timestamp
                label,
                description,
                "", // said to client (intentionally empty)
                "",   // what client said (intentionally empty)
                "" // initialize link to image (will be modified later by separate task)
        );

        // make sure there user provided content to send
        if (label.length() != 0 && description.length() != 0) {
            new WriteImgToDriveTask(mCredential).execute();
        } else {
            new AlertDialog.Builder( EventReportActivity.this )
                    .setTitle("Error")
                    .setMessage("Please enter a label and a description!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        }
    }

    /**
     * Writes an image to google drive and returns the sheet data to main activity
     */
    private class WriteImgToDriveTask extends AsyncTask<Void, Void, String> {
        private com.google.api.services.drive.Drive mService = null;
        private ProgressDialog mProgress;

        /**
         * Initialize task with the account that will send the data
         * @param credential the google account to use
         */
        WriteImgToDriveTask(GoogleAccountCredential credential) {
            // Initialize required components to send information
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            // initialize drive api service
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("CareTrackerApp").build();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return saveImgToDrive();
            } catch (Exception e) {
                cancel(true);
                Toast.makeText(EventReportActivity.this,
                        "Failed to save image.",
                        Toast.LENGTH_LONG).show();
                return null;
            }
        }

        /**
         * Saves the image on Google Drive to the shared CareTrackerApp folder.
         *
         * @return link to the file
         * @throws IOException If an error occurs while writing the file
         */
        private String saveImgToDrive() throws IOException {
            // Generate file name for Google Drive
            String filename = sheetData.getUser() + " CareTrackerEvent " + imgTimeStamp + ".jpg";
            File fileMetadata = new File();
            fileMetadata.setName(filename);
            String pageToken = null;
            // Obtain reference to the CareTrackerApp folder
            FileList result = mService.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder' and sharedWithMe and name contains 'CareTrackerApp'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
            String sharedWithMe = null;
            for (File file : result.getFiles()) {
                sharedWithMe = file.getId();
            }
            if (sharedWithMe != null) {
                fileMetadata.setParents(Collections.singletonList(sharedWithMe));
            }
            FileContent fileContent = new FileContent("image/jpeg", imageToSave);

            // send the image
            File image = mService.files().create(fileMetadata, fileContent)
                    .setFields("id")
                    .execute();

            // just in case we couldn't save to the CareTrackerApp folder, set sharing permissions
            Permission userPermission = new Permission()
                    .setType("user")
                    .setRole("writer")
                    .setEmailAddress("care.tracker246@gmail.com");
            mService.permissions().create(image.getId(), userPermission).execute();

            // generate link to image
            String link = "https://drive.google.com/file/d/"
                    + image.getId()
                    + "/view?usp=sharing";
            return link;
        }

        @Override
        protected void onPreExecute() {
            mProgress = new ProgressDialog(EventReportActivity.this);
            mProgress.setMessage("Saving image to Google Drive...");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String result) {
            mProgress.dismiss();
            // Prepare data to return to MainActivity
            Gson gson = new Gson();
            sheetData.setImgLink(result);
            String dataJson = gson.toJson(sheetData);
            Intent intent = new Intent();
            intent.putExtra(MainActivity.SHEET_DATA, dataJson);
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        protected void onCancelled() {
            mProgress.dismiss();
            Toast.makeText(EventReportActivity.this,
                    "Saving image cancelled...",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
