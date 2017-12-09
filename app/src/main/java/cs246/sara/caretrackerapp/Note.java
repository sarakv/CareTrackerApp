package cs246.sara.caretrackerapp;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Note extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
    }

    public void onCancelListener(View v) {
        // Return to MainActivity
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
