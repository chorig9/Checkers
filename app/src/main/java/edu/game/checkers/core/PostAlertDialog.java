package edu.game.checkers.core;

import android.content.Context;
import android.os.Handler;

public class PostAlertDialog extends AlertDialog {

    private Handler handler;

    public PostAlertDialog(Context activity, Handler handler){
        super(activity);
        this.handler = handler;
    }

    public void createExitDialog(final String title, final String msg){
        handler.post(new Runnable() {
            @Override
            public void run() {
                PostAlertDialog.super.createExitDialog(title, msg);
            }
        });
    }

    public void createInfoDialog(final String title, final String msg){
        handler.post(new Runnable() {
            @Override
            public void run() {
                PostAlertDialog.super.createInfoDialog(title, msg);
            }
        });
    }

    public void createQuestionDialog(final String title, final String msg, final OnClickListener listener){
        handler.post(new Runnable() {
            @Override
            public void run() {
                PostAlertDialog.super.createQuestionDialog(title, msg, listener);
            }
        });
    }

}
