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

public class Note extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
    }

    public void onCancelListener(View v) {
        // Return to MainActivity
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onOkClick(View v) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);
        final String timestamp = sdf.format(new Timestamp(System.currentTimeMillis()));
        final String user = MyPreferences.getString(this, MainActivity.DISPLAY_NAME, "");
        final String label = "Conversation";
        final String description = "";
        final String saidToClient = ((EditText) findViewById(R.id.cTOclient)).getText().toString();
        final String clientSaid = ((EditText) findViewById(R.id.cFROMclient)).getText().toString();
        final String imgLink = "";
        final String message = user + " :: "
                + timestamp
                + "\n"
                + label
                + ": "
                + saidToClient
                + " - "
                + clientSaid;
        if (saidToClient.length() != 0 || clientSaid.length() != 0) {
            new AlertDialog.Builder( Note.this )
                    .setTitle("Confirm")
                    .setMessage("Sending conversation:\n" + message)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SheetData sheetData = new SheetData(user, timestamp, label, description,
                                    saidToClient, clientSaid, imgLink);
                            Gson gson = new Gson();
                            String dataJson = gson.toJson(sheetData);
                            Intent intent = new Intent();
                            intent.putExtra(MainActivity.SHEET_DATA, dataJson);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }).show();
        } else {
            new AlertDialog.Builder( Note.this )
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
