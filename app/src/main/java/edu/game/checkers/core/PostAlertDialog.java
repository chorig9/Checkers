package edu.game.checkers.core;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

public class PostAlertDialog extends Dialog {

    private Activity activity;
    private Handler handler;

    public PostAlertDialog(Context activity, Handler handler){
        super(activity);
        this.handler = handler;
        this.activity = (Activity) activity;
    }

    public void createExitDialog(final String title, final String msg){
        handler.post(new Runnable() {
            @Override
            public void run() {
                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(activity).create();
                alertDialog.setTitle(title);
                alertDialog.setMessage(msg);
                alertDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                (activity).finish();
                            }
                        });
                alertDialog.setOwnerActivity(activity);
                alertDialog.show();
            }
        });
    }

    public void createInfoDialog(final String title, final String msg){
        handler.post(new Runnable() {
            @Override
            public void run() {
                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(activity).create();
                alertDialog.setTitle(title);
                alertDialog.setMessage(msg);
                alertDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.setOwnerActivity(activity);
                alertDialog.show();
            }
        });
    }

    public void createQuestionDialog(final String title, final String msg, final OnClickListener listener){
        handler.post(new Runnable() {
            @Override
            public void run() {
                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(activity).create();
                alertDialog.setTitle(title);
                alertDialog.setMessage(msg);
                alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "OK", listener);
                alertDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "NO", listener);
                alertDialog.setOwnerActivity(activity);
                alertDialog.show();
            }
        });
    }

}
