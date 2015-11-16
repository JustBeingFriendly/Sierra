package com.example.benjamin.sierra;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.NotificationCompat;

/**
 * Created by Stephen on 14/11/2015.
 */
public class QueueChecker extends Application {

    int notiFicationID  = 1;
    NotificationManager notMan = null;

    public QueueChecker(){
        CountDown();
    }

    private void CountDown(){
        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                performNotification();
                this.start();

            }
        }.start();
    }

    private void performNotification(){
        //Intent intent = new Intent(this, FrontPage.class);
        Context cxt = getApplicationContext();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(cxt);
        builder.setContentTitle("Ready");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setTicker("Your Drink is ready");
        notMan = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notMan.notify(notiFicationID, builder.build());
    }

}
