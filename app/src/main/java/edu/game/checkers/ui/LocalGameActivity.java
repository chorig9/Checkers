package edu.game.checkers.ui;

import android.os.Bundle;
import android.widget.TextView;

import edu.board.checkers.R;

public class LocalGameActivity extends GameActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initGame();
        TextView title = (TextView) findViewById(R.id.name_header);
        String header = getString(R.string.name_header) + "local";
        title.setText(header);
    }
}
