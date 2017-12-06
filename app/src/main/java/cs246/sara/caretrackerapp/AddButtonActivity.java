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

public class AddButtonActivity extends AppCompatActivity implements ColorPickerDialogListener {
    ColorPanelView colorPreview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_button);
        init();
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        colorPreview.setColor(color);
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void createButton(String label, String desc, int color){
        ButtonInfo newButtonInfo = new ButtonInfo();
        newButtonInfo.label = label;
        newButtonInfo.color = color;
        newButtonInfo.description = desc;
        newButtonInfo.params = (LinearLayout.LayoutParams) ((Button)findViewById(R.id.btn_newBtn)).getLayoutParams();
        //newButtonInfo.params.setMargins(0,(int)(getResources().getDisplayMetrics().density * 12 + 0.5f), 0, 0 );
        Gson gson = new Gson();
        String btnJson = gson.toJson(newButtonInfo);
        int numButtons = MyPreferences.getInt(this, MainActivity.NUM_BUTTONS, 0);
        MyPreferences.setString(this, MainActivity.BUTTON_TAG + numButtons, btnJson);
        numButtons++;
        MyPreferences.setInt(this,MainActivity.NUM_BUTTONS, numButtons);
    }

    private void init() {
        colorPreview = (ColorPanelView) findViewById(R.id.color_preview);
        colorPreview.setColor(ContextCompat.getColor(this, R.color.defaultColor));
        View desc = findViewById(R.id.user_description);
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

    public void onColorPickListener(View v) {
        hideKeyboard(v);
        ColorPickerDialog.newBuilder().setColor(colorPreview.getColor()).show(AddButtonActivity.this);
    }

    public void onOkListener(View v) {
        final String newButtonName = ((EditText) findViewById(R.id.user_label)).getText().toString();
        final String newButtonDesc = ((EditText) findViewById(R.id.user_description)).getText().toString();
        final int newButtonColor = colorPreview.getColor();

        if (newButtonName.length() != 0 || newButtonDesc.length() != 0) {
            //TODO store button info
            createButton(newButtonName, newButtonDesc, newButtonColor);
            // Return to MainActivity
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            new AlertDialog.Builder( AddButtonActivity.this )
                .setTitle("Error")
                .setMessage("Please enter a label and a description!")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
        }
    }

    public void onCancelListener(View v) {
        // Return to MainActivity
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    public void onLabelLayoutClick(View v) {
        findViewById(R.id.user_label).performClick();
    }

    public void onDescLayoutClick(View v) {
        findViewById(R.id.user_description).performClick();
    }
}
