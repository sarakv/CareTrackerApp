package cs246.sara.caretrackerapp;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class ButtonInfo {

    public String label;
    public int color;
    public String description;
    public LinearLayout.LayoutParams params;

    public ButtonInfo() {
        //intentionally empty, needed for gson
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
