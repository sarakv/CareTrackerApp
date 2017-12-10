package cs246.sara.caretrackerapp;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.gson.Gson;
import com.jaredrummler.android.colorpicker.ColorPanelView;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

/**
 * Allows the user to modify the information of a previously created button
 */
public class ModifyButtonActivity extends AppCompatActivity implements ColorPickerDialogListener {
    private ColorPanelView colorPreview = null;
    private String mButton = null;
    private ButtonInfo mInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_button);
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

    /**
     * Listener for color selection. Hides the keyboard and stores the color selection once the user
     * has picked.
     * @param v a reference to the view (required)
     */
    public void onColorPickListener(View v) {
        hideKeyboard(v);
        ColorPickerDialog.newBuilder().setColor(colorPreview.getColor()).show(ModifyButtonActivity.this);
    }

    /**
     * onClick listener for the layout containing the user input. Allows clicking on the layout
     * to show the keyboard and let the user type.
     * @param v a reference to the view (required)
     */
    public void onLabelLayoutClick(View v) {
        findViewById(R.id.user_label).performClick();
    }

    /**
     * onClick listener for the layout containing the user input. Allows clicking on the layout
     * to show the keyboard and let the user type.
     * @param v a reference to the view (required)
     */
    public void onDescLayoutClick(View v) {
        findViewById(R.id.user_description).performClick();
    }

    /**
     * onClick listener for the ok button. Applies modifications to button as long the user provided
     * changes
     * @param v a reference to the view (required)
     */
    public void onOkListener(View v) {
        // obtain user input
        final String newButtonName = ((EditText) findViewById(R.id.user_label)).getText().toString();
        final String newButtonDesc = ((EditText) findViewById(R.id.user_description)).getText().toString();
        final int newButtonColor = colorPreview.getColor();
        // make sure the user provided input
        if (newButtonName.length() != 0 || newButtonDesc.length() != 0) {
            modifyButton(newButtonName, newButtonDesc, newButtonColor);
            // Return to MainActivity
            setResult(RESULT_OK);
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

    /**
     * onClick listener for the delete button. Removes button from the app.
     * @param v a reference to the view (required)
     */
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
                        setResult(RESULT_OK);
                        finish();
                    }
                }).show();
    }

    /**
     * Deletes button from the app
     */
    private void deleteButton() {
        MyPreferences.remove(ModifyButtonActivity.this, MainActivity.BUTTON_TAG + mInfo.getId());
    }

    /**
     * onClick listener for the cancel button. Returns to MainActivity.
     * @param v a reference to the view (required)
     */
    public void onCancelListener(View v) {
        // Return to MainActivity
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Helper function to hide the keyboard from the screen
     * @param v a reference to the view (required)
     */
    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    /**
     * Initialize activity properties
     */
    private void init() {
        // obtain information from button to be modified
        Gson gson = new Gson();
        mInfo = gson.fromJson(mButton, ButtonInfo.class);
        colorPreview = (ColorPanelView) findViewById(R.id.color_preview);

        // display the information from the button
        colorPreview.setColor(mInfo.getColor());
        ((EditText) findViewById(R.id.user_label)).setText(mInfo.getLabel());
        EditText desc = (EditText) findViewById(R.id.user_description);
        desc.setText(mInfo.getDescription());

        // set a listener to hide the keyboard when the user presses enter
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

        // hide the keyboard if user clicks outside
        desc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
    }

    /**
     * Applies modifications to button
     * @param label the entry name
     * @param desc the entry description
     * @param color the color to display
     */
    private void modifyButton(String label, String desc, int color){
        mInfo.setLabel(label);
        mInfo.setColor(color);
        mInfo.setDescription(desc);
        Gson gson = new Gson();
        String btnJson = gson.toJson(mInfo);
        MyPreferences.setString(this, MainActivity.BUTTON_TAG + mInfo.getId(), btnJson);
    }
}
