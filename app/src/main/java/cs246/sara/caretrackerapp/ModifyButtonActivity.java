package cs246.sara.caretrackerapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.jaredrummler.android.colorpicker.ColorPanelView;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

public class ModifyButtonActivity extends AppCompatActivity implements ColorPickerDialogListener {
    ColorPanelView colorPreview = null;
    String mButton = null;
    ButtonInfo mInfo = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_button);
        setTitle(R.string.title_modify);
        mButton = getIntent().getStringExtra(MainActivity.MODIFY_ID);
        init();
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        colorPreview.setColor(color);
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    public void onColorPickListener(View v) {
        hideKeyboard(v);
        ColorPickerDialog.newBuilder().setColor(colorPreview.getColor()).show(ModifyButtonActivity.this);
    }

    public void onLabelLayoutClick(View v) {
        findViewById(R.id.user_label).performClick();
    }

    public void onDescLayoutClick(View v) {
        findViewById(R.id.user_description).performClick();
    }

    public void onOkListener(View v) {
        final String newButtonName = ((EditText) findViewById(R.id.user_label)).getText().toString();
        final String newButtonDesc = ((EditText) findViewById(R.id.user_description)).getText().toString();
        final int newButtonColor = colorPreview.getColor();

        if (newButtonName.length() != 0 || newButtonDesc.length() != 0) {
            modifyButton(newButtonName, newButtonDesc, newButtonColor);
            // Return to MainActivity
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            new AlertDialog.Builder( ModifyButtonActivity.this )
                    .setTitle("Error")
                    .setMessage("Please enter a label and a description!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        }
    }

    public void onDeleteListener(View v) {
        new AlertDialog.Builder( ModifyButtonActivity.this )
                .setTitle("Confirm")
                .setMessage("Are you sure you want to delete this button?\nWARNING! This action cannot be undone.")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteButton();
                    }
                }).show();
    }

    private void deleteButton() {
        //TODO implement
    }

    public void onCancelListener(View v) {
        // Return to MainActivity
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void init() {
        Gson gson = new Gson();
        mInfo = gson.fromJson(mButton, ButtonInfo.class);
        colorPreview = (ColorPanelView) findViewById(R.id.color_preview);
        colorPreview.setColor(mInfo.color);
        ((EditText) findViewById(R.id.user_label)).setText(mInfo.label);
        EditText desc = (EditText) findViewById(R.id.user_description);
        desc.setText(mInfo.description);
        desc.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch(keyCode){
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            hideKeyboard(v);
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
        desc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
    }

    //TODO update
    private void modifyButton(String label, String desc, int color){
        mInfo.label = label;
        mInfo.color = color;
        mInfo.description = desc;
        Gson gson = new Gson();
        String btnJson = gson.toJson(mInfo);
        MyPreferences.setString(this, MainActivity.BUTTON_TAG + mInfo.id, btnJson);
    }
}
