package edu.game.checkers.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertDialog extends Dialog {

    private Activity activity;

    public AlertDialog(Context activity){
        super(activity);
        this.activity = (Activity) activity;
    }

    public void createExitDialog(String title, String msg){
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

    public void createInfoDialog(String title, String msg){
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

    public void createQuestionDialog(String title, String msg, OnClickListener listener){
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "OK", listener);
        alertDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "NO", listener);
        alertDialog.setOwnerActivity(activity);
        alertDialog.show();
    }

}
