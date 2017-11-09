package cs246.sara.caretrackerapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {
    public class ButtonInfo {
        public String text;
        //public Drawable background;
        public String description;
        public ViewGroup.LayoutParams params;

        public ButtonInfo() {
            //intentionally empty
        }

        public ButtonInfo(Button button) {
            text = button.getText().toString();
            //background = button.getBackground().getCurrent();
            description = button.getTag().toString();
            params = button.getLayoutParams();
        }

        public Button getButton(Context context) {
            Button myButt = new Button(context);
            myButt.setText(text);
            myButt.setTag(description);
            //myButt.setBackground(background);
            myButt.setLayoutParams(params);
            myButt.setId(View.generateViewId());
            return myButt;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button testButton = (Button) findViewById(R.id.button);

        Gson gson = new Gson();
        String btnJson = gson.toJson(new ButtonInfo(testButton));


        SharedPreferences.Editor editor = getSharedPreferences("myPref", MODE_PRIVATE).edit();
        editor.putString("button0", btnJson);
        editor.apply();


        SharedPreferences prefs = getSharedPreferences("myPref", MODE_PRIVATE);
        btnJson = prefs.getString("button0", "testBtn2");

        Button myButt = gson.fromJson(btnJson, ButtonInfo.class).getButton(this);

        testButton.setText("changed");

        LinearLayout layout = (LinearLayout) findViewById(R.id.testLayout);
        layout.addView(myButt);
     }
}
