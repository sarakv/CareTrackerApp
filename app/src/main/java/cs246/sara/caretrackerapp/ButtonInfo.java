package cs246.sara.caretrackerapp;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Groups information necessary to generate a button.
 * Facilitates using gson and restoring from gson.
 */
public class ButtonInfo {
    private int id;
    private String label;
    private int color;
    private String description;
    private LinearLayout.LayoutParams params;

    public ButtonInfo() {
        //intentionally empty, needed for gson
    }

    // getters and setters
    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public int getColor() {
        return color;
    }

    public String getDescription() {
        return description;
    }

    public LinearLayout.LayoutParams getParams() {
        return params;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setParams(LinearLayout.LayoutParams params) {
        this.params = params;
    }

    /**
     * Returns a button instance from the information stored in the ButtonInfo
     * @param context necessary context to inflate the view
     * @return the button reference
     */
    public Button getButton(Context context) {
        Button myButton = new Button(context);
        myButton.setText(label);
        myButton.setBackgroundColor(color);
        myButton.setLayoutParams(params);
        myButton.setId(View.generateViewId());
        myButton.setTag(this);

        return myButton;
    }

}
