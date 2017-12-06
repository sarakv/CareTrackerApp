package cs246.sara.caretrackerapp;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.drive.Metadata;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks, View.OnClickListener {

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    static final int REQUEST_NEW_BUTTON = 9876;
    static final String SPREADSHEET_ID = "!SID#";
    static final String NUM_BUTTONS = "!NB#";
    static final String BUTTON_TAG = "!B#";

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS, Scopes.PROFILE};

    //REPLACE WITH CHRISTINA'S PERMANENT SHEET ID
    private static final String pointerSheetId = "1b326p7-twc-D7Opfxgl9wgfBOa0E6-JYwlWhgjf6wqA";
    String spreadsheetId = null;

    GoogleAccountCredential mCredential;
    String mOutputText;
    ProgressDialog mProgress;

    LinearLayout buttonsLayout = null;

    String displayName = "";

    boolean isReadyToWrite = false;
    private int rowNumber = 3;
    private int inUse = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

    }

    public void onAddNoteListener(View v) {
        Intent intent = new Intent(this, Note.class);
        startActivity(intent);
    }

    public void onAddExpenseListener(View v) {
        Intent intent = new Intent(this, Expense.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_addButton:
                startActivityForResult(new Intent(this, AddButtonActivity.class), REQUEST_NEW_BUTTON);
                break;
            case R.id.action_tutorial:
                break;
            case R.id.action_about:
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }


    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mOutputText =
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.";
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
            case REQUEST_NEW_BUTTON:
                spreadsheetId = MyPreferences.getString(this, SPREADSHEET_ID, null);
                updateUI(spreadsheetId != null);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        final ButtonInfo info = (ButtonInfo) v.getTag();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);
        final String timestamp = sdf.format(new Timestamp(System.currentTimeMillis()));
        final String user = displayName;
        final String label = info.label;
        final String description = info.description;
        final String convoTo = "";   //TODO set to correct value
        final String convoFrom = ""; //TODO set to correct value
        final String imgLink = "";   //TODO set to correct value
        final String message = displayName + " :: "
                + timestamp
                + "\n"
                + info.label
                + ": "
                + info.description;
        new AlertDialog.Builder( MainActivity.this )
                .setTitle("Confirm")
                .setMessage("Sending action:\n" + message)
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendActionToSheet(user, timestamp, label, description, convoTo, convoFrom, imgLink);
                    }
                }).show();


    }

    private void init() {
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Sheets API ...");
        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        buttonsLayout = (LinearLayout) findViewById(R.id.layout_buttons);
        findViewById(R.id.button_signIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                mOutputText = "";
                getResultsFromApi();
                v.setEnabled(true);
            }
        });
    }

    private void sendActionToSheet(String... values) {
        isReadyToWrite = false;
        new ReadFromSheetTask(1, mCredential).execute("Sheet1!Z1:Z1", spreadsheetId);
        new WriteToSheetTask(mCredential).execute(values);
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            mOutputText = "No network connection available.";
        } else {
            getSpreadSheetId();
            new DownloadDisplayName().execute();
        }
    }

    private void getSpreadSheetId() {
        new ReadFromSheetTask(mCredential).execute("Sheet1!A1:A1", pointerSheetId);
    }

    private void updateUI(boolean isLoggedIn) {
        if (isLoggedIn) {
            findViewById(R.id.layout_login).setVisibility(View.GONE);
            findViewById(R.id.layout_main).setVisibility(View.VISIBLE);
            restoreButtons();
        } else {
            findViewById(R.id.layout_main).setVisibility(View.GONE);
            findViewById(R.id.layout_login).setVisibility(View.VISIBLE);
        }
    }

    private void restoreButtons() {
        buttonsLayout.removeAllViews();
        int numButtons = MyPreferences.getInt(this, NUM_BUTTONS, 0);

        for (int i = 0; i < numButtons; i++) {
            String btnJson = MyPreferences.getString(this, BUTTON_TAG + i, null);
            if (btnJson != null) {
                Gson gson = new Gson();
                Button myButton = gson.fromJson(btnJson, ButtonInfo.class).getButton(this);
                myButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

                myButton.setOnClickListener(this);
                buttonsLayout.addView(myButton);
            }
        }
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private class ReadFromSheetTask extends AsyncTask<String, Void, String> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        private int id;

        ReadFromSheetTask(GoogleAccountCredential credential) {
            id = 0;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("CareTrackerApp")
                    .build();
        }

        ReadFromSheetTask(int callerID, GoogleAccountCredential credential) {
            id = callerID;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("CareTrackerApp")
                    .build();
        }

        protected String doInBackground(String... params) {
            try {
                return getCellDataFromApi(params[0], params[1]);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private String getCellDataFromApi(String cell, String sheetID) throws IOException {
            ValueRange response = mService.spreadsheets().values()
                    .get(sheetID, cell)
                    .execute();
            List<List<Object>> values = response.getValues();
            String result = null;
            if (values != null && values.get(0) != null) {
                result = (String) values.get(0).get(0);
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            mOutputText = "";
            inUse++; //TODO gate with semaphore
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String result) {
            inUse--; //TODO gate with semaphore
            if (inUse == 0) {
                mProgress.hide();
            }
            handleReadResult(id, result);
        }

        @Override
        protected void onCancelled() {
            inUse--; //TODO gate with semaphore
            if (inUse == 0) {
                mProgress.hide();
            }
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    mOutputText = "The following error occurred:\n"
                            + mLastError.getMessage();
                }
            } else {
                mOutputText = "Request cancelled.";
            }
        }
    }

    private void handleReadResult(int callerID, String result) {
        switch (callerID) {
            case 0:
                spreadsheetId = result;
                MyPreferences.setString(MainActivity.this, SPREADSHEET_ID, result);
                updateUI(true);
                break;
            case 1:
                rowNumber = Integer.parseInt(result);
                isReadyToWrite = true;
                break;
            default:
        }
    }

    private class DownloadDisplayName extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... args) {
            String response = "";
            try {
                URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + mCredential.getToken());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream content = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }
            } catch (Exception e) {
                e.printStackTrace();
                displayName = "";
            }
            return response;
        }

        @Override
        protected void onPreExecute() {
            inUse++; //TODO gate with semaphore
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String result) {
            inUse--; //TODO gate with semaphore
            if (inUse == 0) {
                mProgress.hide();
            }
            try {
                JSONObject obj = new JSONObject(result);
                displayName = (String) obj.get("name");
            } catch (JSONException e) {
                e.printStackTrace();
                displayName = "";
            }
        }

        @Override
        protected void onCancelled() {
            inUse--; //TODO gate with semaphore
            if (inUse == 0) {
                mProgress.hide();
            }
        }
    }

    private class WriteToSheetTask extends AsyncTask<String, Void, Void> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        WriteToSheetTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("CareTrackerApp")
                    .build();
        }

        protected Void doInBackground(String... params) {
            try {
                return writeRowDataToApi(params);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private Void writeRowDataToApi(String... values) throws IOException {
            int timeOut = 0;
            while (!isReadyToWrite) {
                try {
                    Thread.sleep(200);
                    timeOut++;
                    if (timeOut == 25)
                        return null;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            String rowRange = String.format(Locale.US, "Sheet1!A%d:G%d", rowNumber, rowNumber);
            List<List<Object>> vals = Arrays.asList(
                    Arrays.asList(
                            (Object[])values
                    )
            );
            ValueRange data = new ValueRange();
            data.setValues(vals);
            mService.spreadsheets().values().update(spreadsheetId, rowRange, data)
                    .setValueInputOption(getString(R.string.value_input_option))
                    .execute();
            rowRange = "Sheet1!Z1:Z1";
            rowNumber++;
            vals = Arrays.asList(
                    Arrays.asList(
                            (Object) String.format(Locale.US,"%d", rowNumber)
                    )
            );
            data = new ValueRange();
            data.setValues(vals);
            mService.spreadsheets().values().update(spreadsheetId, rowRange, data)
                    .setValueInputOption(getString(R.string.value_input_option))
                    .execute();
            return null;
        }

        @Override
        protected void onPreExecute() {
            inUse++; //TODO gate with semaphore
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Void v) {
            inUse--; //TODO gate with semaphore
            if (inUse == 0) {
                mProgress.hide();
            }
        }

        @Override
        protected void onCancelled() {
            inUse--; //TODO gate with semaphore
            if (inUse == 0) {
                mProgress.hide();
            }
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    //mOutputText.setText("The following error occurred:\n"
                  //          + mLastError.getMessage());
                }
            } else {
                //mOutputText.setText("Request cancelled.");
            }
        }
    }
}
