package cs246.sara.caretrackerapp;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks, View.OnClickListener,
    View.OnLongClickListener {

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    static final int REQUEST_ABOUT_VIEW = 1004;
    static final int REQUEST_NEW_BUTTON = 1005;
    static final int REQUEST_MODIFY_BUTTON = 1006;
    static final int REQUEST_DATA = 1007;
    static final String SHEET_DATA = "!SD#";
    static final String MODIFY_ID = "!MID#";
    static final String DISPLAY_NAME = "!DN#";
    static final String ID_DATE = "!IDD#";
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

    LinearLayout buttonsLayout = null;

    String displayName = "";
    private int rowNumber = 3;
    Menu mMenu = null;

    String[] vals = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    public void onAddNoteListener(View v) {
        Intent intent = new Intent(this, Note.class);
        startActivityForResult(intent, REQUEST_DATA);
    }

    public void onAddExpenseListener(View v) {
        Intent intent = new Intent(this, Expense.class);
        startActivityForResult(intent, REQUEST_DATA);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        mMenu = menu;
        if (spreadsheetId == null) {
            mMenu.findItem(R.id.action_addButton).setVisible(false);
            mMenu.findItem(R.id.action_signOut).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_addButton:
                startActivityForResult(new Intent(this, AddButtonActivity.class), REQUEST_NEW_BUTTON);
                break;
            case R.id.action_tutorial:
                //TODO
                break;
            case R.id.action_about:
                //TODO
                startActivityForResult(new Intent(this, About.class), REQUEST_ABOUT_VIEW);
                break;
            case R.id.action_signOut:
                mCredential.setSelectedAccount(null);
                MyPreferences.remove(MainActivity.this, PREF_ACCOUNT_NAME);
                MyPreferences.remove(MainActivity.this, SPREADSHEET_ID);
                MyPreferences.remove(MainActivity.this, DISPLAY_NAME);
                spreadsheetId = null;
                updateUI(false);
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
                        MyPreferences.setString(MainActivity.this, PREF_ACCOUNT_NAME, accountName);
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
                if (resultCode == RESULT_OK) {
                    spreadsheetId = MyPreferences.getString(this, SPREADSHEET_ID, null);
                    updateUI(spreadsheetId != null);
                }
                break;
            case REQUEST_MODIFY_BUTTON:
                // intentionally empty
                break;
            case REQUEST_DATA:
                if (resultCode == RESULT_OK) {
                    String dataJson = data.getStringExtra(SHEET_DATA);
                    Gson gson = new Gson();
                    SheetData sheetData = gson.fromJson(dataJson, SheetData.class);
                    sendActionToSheet(sheetData);
                }
                break;
            default:
        }
    }

    @Override
    public void onClick(View v) {
        final ButtonInfo info = (ButtonInfo) v.getTag();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);
        final SheetData data = new SheetData(displayName, sdf.format(new Timestamp(System.currentTimeMillis())),
        info.label, info.description, "", "", "");
        final String message = data.user + " :: "
                + data.timestamp
                + "\n"
                + data.label
                + ": "
                + data.description;
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
                        sendActionToSheet(data);
                    }
                }).show();
    }

    @Override
    public boolean onLongClick(View v) {
        Intent intent = new Intent(this, ModifyButtonActivity.class);
        Gson gson = new Gson();
        String btnJson = gson.toJson((ButtonInfo)v.getTag());
        intent.putExtra(MODIFY_ID, btnJson);
        startActivityForResult(intent, REQUEST_MODIFY_BUTTON);
        return true;
    }

    private void init() {
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

        mCredential.setSelectedAccountName(MyPreferences.getString(this, PREF_ACCOUNT_NAME, null));
        displayName = MyPreferences.getString(this, DISPLAY_NAME, "");
        spreadsheetId = MyPreferences.getString(this, SPREADSHEET_ID, null);
        if (mCredential.getSelectedAccountName() != null) {
            if (spreadsheetId != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);
                try {
                    Date idDate = sdf.parse(MyPreferences.getString(MainActivity.this,ID_DATE, null));
                    if (DateUtils.isToday(idDate.getTime())) {
                        updateUI(true);
                    } else {
                        getResultsFromApi();
                    }
                } catch (ParseException e) {
                    getResultsFromApi();
                }
            } else {
                getResultsFromApi();
            }
        } else {
            updateUI(false);
        }
    }

    private void sendActionToSheet(SheetData data) {
        vals = data.getValues();
        new ReadFromSheetTask(1, mCredential).execute("Sheet1!Z1:Z1", spreadsheetId);
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
        }
    }

    private void getSpreadSheetId() {
        new ReadFromSheetTask(mCredential).execute("Sheet1!A1:A1", pointerSheetId);
    }

    private void updateUI(boolean isLoggedIn) {
        if (isLoggedIn) {
            if (mMenu != null){
                mMenu.findItem(R.id.action_addButton).setVisible(true);
                mMenu.findItem(R.id.action_signOut).setVisible(true);
            }
            findViewById(R.id.layout_login).setVisibility(View.GONE);
            findViewById(R.id.layout_main).setVisibility(View.VISIBLE);
            restoreButtons();
        } else {
            if (mMenu != null) {
                mMenu.findItem(R.id.action_addButton).setVisible(false);
                mMenu.findItem(R.id.action_signOut).setVisible(false);
            }
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
                myButton.setOnLongClickListener(this);
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
            String accountName = MyPreferences.getString(MainActivity.this, PREF_ACCOUNT_NAME, null);
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
        private boolean isCancelled = false;
        private int id;
        private ProgressDialog mProgress;

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
            mProgress = new ProgressDialog(MainActivity.this);
            mProgress.setMessage("Loading...");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String result) {
            mProgress.dismiss();
            if (!isCancelled) {
                handleReadResult(id, result);
            }
        }

        @Override
        protected void onCancelled() {
            isCancelled = true;
            mProgress.dismiss();
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
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);
                String timestamp = sdf.format(new Timestamp(System.currentTimeMillis()));
                MyPreferences.setString(MainActivity.this, ID_DATE, timestamp );
                updateUI(true);
                new DownloadDisplayName().execute();
                break;
            case 1:
                rowNumber = Integer.parseInt(result);
                new WriteToSheetTask(mCredential).execute(vals);
                break;
            default:
        }
    }

    private class DownloadDisplayName extends AsyncTask<Void, Void, String> {
        ProgressDialog mProgress;

        @Override
        protected String doInBackground(Void... args) {
            String response = "";
            if (mCredential.getSelectedAccountName() != null) {
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
                    displayName = "";
                    cancel(true);
                    return null;
                }
            } else {
                cancel(true);
                return null;
            }

            return response;
        }

        @Override
        protected void onPreExecute() {
            mProgress = new ProgressDialog(MainActivity.this);
            mProgress.setMessage("Obtaining profile name...");
            // Initialize credentials and service object.
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String result) {
            mProgress.dismiss();
            if (result != null) {
                try {
                    JSONObject obj = new JSONObject(result);
                    displayName = (String) obj.get("name");
                    MyPreferences.setString(MainActivity.this, DISPLAY_NAME, displayName);
                } catch (JSONException e) {
                    e.printStackTrace();
                    displayName = "";
                }
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.dismiss();
        }
    }

    private class WriteToSheetTask extends AsyncTask<String, Void, Void> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        private ProgressDialog mProgress;

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
            mProgress = new ProgressDialog(MainActivity.this);
            mProgress.setMessage("Sending Action...");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Void v) {
            mProgress.dismiss();
        }

        @Override
        protected void onCancelled() {
            mProgress.dismiss();
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
