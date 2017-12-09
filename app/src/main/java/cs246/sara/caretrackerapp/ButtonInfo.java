package cs246.sara.caretrackerapp;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class ButtonInfo {
    private int id;
    private String label;
    private int color;
    private String description;
    private LinearLayout.LayoutParams params;

    public ButtonInfo() {
        //intentionally empty, needed for gson
    }

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
