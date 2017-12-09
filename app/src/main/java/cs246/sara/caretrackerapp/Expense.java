package cs246.sara.caretrackerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.gson.Gson;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

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
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);
        startCamera();
    }

    public void onOkClick(View v) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US);
        final String timestamp = sdf.format(new Timestamp(System.currentTimeMillis()));
        final String user = MyPreferences.getString(this, MainActivity.DISPLAY_NAME, "");
        final String label = ((EditText) findViewById(R.id.editLabel)).getText().toString();;
        final String description = ((EditText) findViewById(R.id.expenseDescription)).getText().toString();;
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
}
