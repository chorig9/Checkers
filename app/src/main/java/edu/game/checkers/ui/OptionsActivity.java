package edu.game.checkers.ui;

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
import edu.game.checkers.logic.Game;
import edu.game.checkers.utils.Callback0;

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

        initializeCheckboxes();
        initializeNetworkConfig();
    }

    public void onCheckBoxClick(View view) {
        final CheckBox checkBox = (CheckBox) findViewById(view.getId());

        SharedPreferences preferences = getSharedPreferences(OPTIONS_FILE, MODE_PRIVATE);
        int options = preferences.getInt(OPTIONS_KEY, 0);
        Editor editor = preferences.edit();

        switch (checkBox.getId()) {
            case R.id.checkbox_backward:
                if (checkBox.isChecked())
                    editor.putInt(OPTIONS_KEY, options | Game.backwardCapture);
                else
                    editor.putInt(OPTIONS_KEY, options & ~Game.backwardCapture);
                break;
            case R.id.checkbox_flying:
                if (checkBox.isChecked())
                    editor.putInt(OPTIONS_KEY, options | Game.flyingKing);
                else
                    editor.putInt(OPTIONS_KEY, options & ~Game.flyingKing);
                break;
            case R.id.checkbox_optimal:
                if (checkBox.isChecked())
                    editor.putInt(OPTIONS_KEY, options | Game.optimalCapture);
                else
                    editor.putInt(OPTIONS_KEY, options & ~Game.optimalCapture);
                break;
        }

        editor.apply();
    }

    private void initializeCheckboxes(){
        SharedPreferences preferences = getSharedPreferences(OPTIONS_FILE, MODE_PRIVATE);
        CheckBox checkboxCapture = (CheckBox) findViewById(R.id.checkbox_backward);
        CheckBox checkboxFlying = (CheckBox) findViewById(R.id.checkbox_flying);
        CheckBox checkboxOptimal = (CheckBox) findViewById(R.id.checkbox_optimal);

        int options = preferences.getInt(OPTIONS_KEY, 0);

        checkboxCapture.setChecked(Game.isOptionEnabled(options, Game.backwardCapture));
        checkboxFlying.setChecked(Game.isOptionEnabled(options, Game.flyingKing));
        checkboxOptimal.setChecked(Game.isOptionEnabled(options, Game.optimalCapture));
    }

    private void initializeNetworkConfig(){
        SharedPreferences preferences = getSharedPreferences(OPTIONS_FILE, MODE_PRIVATE);
        final EditText hostname = (EditText) findViewById(R.id.hostname);
        final EditText ip = (EditText) findViewById(R.id.ip);
        final EditText port = (EditText) findViewById(R.id.port);

        hostname.setText(preferences.getString(HOSTNAME_KEY, DEFAULT_HOSTNAME));
        ip.setText(preferences.getString(IP_ADDRESS_KEY, DEFAULT_IP));
        port.setText(Integer.toString(preferences.getInt(PORT_KEY, DEFAULT_PORT)));
        final Editor editor = preferences.edit();

        hostname.addTextChangedListener(new TextChangedListener(new Callback0() {
            @Override
            public void onAction() {
                editor.putString(HOSTNAME_KEY, hostname.getText().toString());
                editor.apply();
            }
        }));

        ip.addTextChangedListener(new TextChangedListener(new Callback0() {
            @Override
            public void onAction() {
                editor.putString(IP_ADDRESS_KEY, ip.getText().toString());
                editor.apply();
            }
        }));

        port.addTextChangedListener(new TextChangedListener(new Callback0() {
            @Override
            public void onAction() {
                editor.putInt(PORT_KEY, Integer.decode(port.getText().toString()));
                editor.apply();
            }
        }));
    }

    private class TextChangedListener implements TextWatcher {

        private Callback0 callback;

        TextChangedListener(Callback0 callback){
            this.callback = callback;
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            callback.onAction();
        }
    }

}
