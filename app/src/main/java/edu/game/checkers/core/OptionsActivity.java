package edu.game.checkers.core;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;

import edu.board.checkers.R;
import edu.game.checkers.logic.Board;

public class OptionsActivity extends AppCompatActivity {

    public static final String OPTIONS_FILE = "Options";
    public static final String OPTIONS_KEY = "options";
    public static final String HOSTNAME_KEY = "hostname";
    public static final String IP_ADDRESS_KEY = "ip_address";
    public static final String PORT_KEY = "port";

    public static final int DEFAULT_PORT = 5222;
    public static final String DEFAULT_IP = "89.40.127.125";
    public static final String DEFAULT_HOSTNAME = "example.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_options);

        final SharedPreferences preferences = getSharedPreferences(OPTIONS_FILE, MODE_PRIVATE);
        final CheckBox checkboxCapture = (CheckBox) findViewById(R.id.checkbox_backward);
        final CheckBox checkboxFlying = (CheckBox) findViewById(R.id.checkbox_flying);
        final CheckBox checkboxOptimal = (CheckBox) findViewById(R.id.checkbox_optimal);

        int options = preferences.getInt(OPTIONS_KEY, 0);

        checkboxCapture.setChecked(Board.isOptionEnabled(options, Board.backwardCapture));
        checkboxFlying.setChecked(Board.isOptionEnabled(options, Board.flyingKing));
        checkboxOptimal.setChecked(Board.isOptionEnabled(options, Board.optimalCapture));

        final EditText hostname = (EditText) findViewById(R.id.hostname);
        final EditText ip = (EditText) findViewById(R.id.ip);
        final EditText port = (EditText) findViewById(R.id.port);

        hostname.setText(preferences.getString(HOSTNAME_KEY, DEFAULT_HOSTNAME));
        ip.setText(preferences.getString(IP_ADDRESS_KEY, DEFAULT_IP));
        port.setText(preferences.getInt(PORT_KEY, DEFAULT_PORT));
        final Editor editor = preferences.edit();

        View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                EditText editText = (EditText) v;
                switch (editText.getId()) {
                    case R.id.hostname:
                        editor.putString(HOSTNAME_KEY, hostname.getText().toString());
                        break;
                    case R.id.ip:
                        editor.putString(IP_ADDRESS_KEY, ip.getText().toString());
                        break;
                    case R.id.port:
                        editor.putInt(PORT_KEY, Integer.decode(port.getText().toString()));
                        break;
                }
                editor.apply();
            }
        };

        hostname.setOnFocusChangeListener(focusChangeListener);
        ip.setOnFocusChangeListener(focusChangeListener);
        port.setOnFocusChangeListener(focusChangeListener);
    }

    public void onCheckBoxClick(View view) {
        final CheckBox checkBox = (CheckBox) findViewById(view.getId());

        SharedPreferences preferences = getSharedPreferences(OPTIONS_FILE, MODE_PRIVATE);
        int options = preferences.getInt(OPTIONS_KEY, 0);
        Editor editor = preferences.edit();

        switch (checkBox.getId()) {
            case R.id.checkbox_backward:
                if (checkBox.isChecked())
                    editor.putInt(OPTIONS_KEY, options | Board.backwardCapture);
                else
                    editor.putInt(OPTIONS_KEY, options & ~Board.backwardCapture);
                break;
            case R.id.checkbox_flying:
                if (checkBox.isChecked())
                    editor.putInt(OPTIONS_KEY, options | Board.flyingKing);
                else
                    editor.putInt(OPTIONS_KEY, options & ~Board.flyingKing);
                break;
            case R.id.checkbox_optimal:
                if (checkBox.isChecked())
                    editor.putInt(OPTIONS_KEY, options | Board.optimalCapture);
                else
                    editor.putInt(OPTIONS_KEY, options & ~Board.optimalCapture);
                break;
        }

        editor.apply();
    }
}
