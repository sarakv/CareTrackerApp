package cs246.sara.caretrackerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Gathers conversation data between user and client to send to Google Sheet
 */
public class ConversationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
    }

    /**
     * onClick Listener for cancel button
     * @param v instance of the clicked view (required)
     */
    public void onCancelListener(View v) {
        // Return to MainActivity
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * onClick Listener for ok button
     * @param v instance of the clicked view (required)
     */
    public void onOkClick(View v) {
        // obtain timestamp from system
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);
        final String timestamp = sdf.format(new Timestamp(System.currentTimeMillis()));
        // obtain input from user
        final String saidToClient = ((EditText) findViewById(R.id.cTOclient)).getText().toString();
        final String clientSaid = ((EditText) findViewById(R.id.cFROMclient)).getText().toString();
        // gather data to send
        final SheetData sheetData = new SheetData(
                MyPreferences.getString(this, MainActivity.DISPLAY_NAME, ""),  // user
                timestamp,
                "Conversation", // label
                "", // description (intentionally empty)
                saidToClient, // said to client
                clientSaid, // what client said
                "" // image link
        );
        // Make sure user provided content to send
        if (saidToClient.length() != 0 || clientSaid.length() != 0) {
            Gson gson = new Gson();
            String dataJson = gson.toJson(sheetData);
            Intent intent = new Intent();
            intent.putExtra(MainActivity.SHEET_DATA, dataJson);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            new AlertDialog.Builder( ConversationActivity.this )
                    .setTitle("Error")
                    .setMessage("Please enter a label and a description!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        }
    }
}
