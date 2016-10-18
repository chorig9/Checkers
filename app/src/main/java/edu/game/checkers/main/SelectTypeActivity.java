package edu.game.checkers.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import java.io.IOException;

import edu.game.checkers.R;

public class SelectTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.select_type_activity);
    }

    public void selectGame(View view) {
        Intent gameIntent = new Intent(this, GameActivity.class);

        Bundle bundle = getIntent().getExtras();
        int options = bundle.getInt("options");

        gameIntent.putExtra("options", options);

        switch(view.getId())
        {
            case R.id.local:
                // tags of buttons must be the same as class names of players (PlayerLocal, PlayerNetwork)
                gameIntent.putExtra("type", view.getTag().toString());
                startActivity(gameIntent);
                break;
            case R.id.network:
                // TODO
                Intent networkActivityIntent = new Intent(this, NetworkActivity.class);
                networkActivityIntent.putExtra("options", options);
                startActivity(networkActivityIntent);
                break;
        }
    }

    public class NetworkActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //Remove notification bar
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

            setContentView(R.layout.select_type_activity);
        }

        public void connectToServer(View view) {

            Bundle bundle = getIntent().getExtras();
            int options = bundle.getInt("options");

            try {
                NetworkManager manager = new NetworkManager();

                boolean accepted = false;
                while(!accepted)
                {
                    String name = ((EditText) findViewById(R.id.username)).getText().toString();
                    manager.send(Message.HI + Message.SEPARATOR + name
                            + Message.SEPARATOR + Integer.toString(options));

                    String response = manager.receive(NetworkManager.SERVER_TIMEOUT);
                    if(response.equals(Message.OK))
                    {
                        // TODO connection succeed
                        accepted = true;
                    }
                    else
                    {
                        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                        alertDialog.setTitle("Error");
                        if(response.length() >= 2)
                            alertDialog.setMessage(Message.toArray(response)[1]);
                        else
                            alertDialog.setMessage("Unknown error");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                }
            } catch (IOException e) {

                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Connection error occurred");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }

        }
    }
}
