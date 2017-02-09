package edu.game.checkers.core;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;

import edu.board.checkers.R;
import edu.game.checkers.logic.Board;

public class OptionsActivity extends AppCompatActivity {

    public static final String OPTIONS_FILE = "Options";
    public static final String OPTIONS_KEY = "options";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_options);

        SharedPreferences preferences = getSharedPreferences(OPTIONS_FILE, MODE_PRIVATE);
        final CheckBox checkboxCapture = (CheckBox) findViewById(R.id.checkbox_backward);
        final CheckBox checkboxFlying = (CheckBox) findViewById(R.id.checkbox_flying);
        final CheckBox checkboxOptimal = (CheckBox) findViewById(R.id.checkbox_optimal);

        int options = preferences.getInt(OPTIONS_KEY, 0);

        checkboxCapture.setChecked(Board.isOptionEnabled(options, Board.backwardCapture));
        checkboxFlying.setChecked(Board.isOptionEnabled(options, Board.flyingKing));
        checkboxOptimal.setChecked(Board.isOptionEnabled(options, Board.optimalCapture));
    }

    public void onCheckBoxClick(View view) {
        final CheckBox checkBox = (CheckBox) findViewById(view.getId());

        SharedPreferences preferences = getSharedPreferences(OPTIONS_FILE, MODE_PRIVATE);
        int options = preferences.getInt(OPTIONS_KEY, 0);
        Editor editor = preferences.edit();

        switch(checkBox.getId())
        {
            case R.id.checkbox_backward:
                if(checkBox.isChecked())
                    editor.putInt(OPTIONS_KEY, options | Board.backwardCapture);
                else
                    editor.putInt(OPTIONS_KEY, options & ~Board.backwardCapture);
                break;
            case R.id.checkbox_flying:
                if(checkBox.isChecked())
                    editor.putInt(OPTIONS_KEY, options | Board.flyingKing);
                else
                    editor.putInt(OPTIONS_KEY, options & ~Board.flyingKing);
                break;
            case R.id.checkbox_optimal:
                if(checkBox.isChecked())
                    editor.putInt(OPTIONS_KEY, options | Board.optimalCapture);
                else
                    editor.putInt(OPTIONS_KEY, options & ~Board.optimalCapture);
                break;
        }

        editor.apply();
    }
}
