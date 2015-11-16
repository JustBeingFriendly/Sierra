package com.example.benjamin.sierra;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FrontPage extends AppCompatActivity {

    private int notificationID;
    private NotificationManager nMananger = null;
    final private String LANAddress = "http://192.168.0.107:8081";
    NotificationCompat.Builder notificationBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);
        CountDown();
        notificationBuilder = new NotificationCompat.Builder(this);
        final Button makedrink = (Button) findViewById(R.id.make_drink);
        notificationID  = 0;
        makedrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent drinklistintent = new Intent(FrontPage.this, DrinkList.class);
                startActivity(drinklistintent);
            }
        });


        final Button myqueue = (Button) findViewById(R.id.queue);
        myqueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent queueintent = new Intent(FrontPage.this, QueuePage.class);
                startActivity(queueintent);
            }
        });

    }

    private void CountDown(){
        new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                //createNotification();
                final String firstInQueueURL = LANAddress + "/firstInQueue";
                GetFirstInQueueAsyncTask getFirst = new GetFirstInQueueAsyncTask();
                getFirst.execute(firstInQueueURL);
                this.start();
            }
        }.start();
    }

    private void createNotification(String nameOfDrink){
        notificationBuilder.setContentTitle("Drink Mixer");
        notificationBuilder.setContentText(nameOfDrink + " is ready to pour");
        notificationBuilder.setTicker(nameOfDrink + " is ready to pour");
        notificationBuilder.setDefaults(Notification.DEFAULT_ALL);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.addAction(R.drawable.shotglass, "Pour", null);
        notificationBuilder.setAutoCancel(true);
        //notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("YEEHAW"));
    }

    private void performNotification(int orderID){
        Notification notification = notificationBuilder.build();
        nMananger = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        nMananger.notify(orderID, notification);
        notificationID = orderID;

        //Intent intent = new Intent(this, FrontPage.class);
        /*Intent intent = new Intent();
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Context cxt = getApplicationContext();
        Notification noti = new Notification.Builder(cxt)
                .setTicker("TickerTitle")
                .setContentTitle("Content Title")
                .setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setContentTitle("Ready");
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setTicker("Your Drink is ready");
        notMan = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notMan.notify(notiFicationID++, notificationBuilder.build());*/
    }


    private class GetFirstInQueueAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... stringArray){ //theURL is the parameter connectionString from cts.execute
            String strToReturn= "";
            try {
                URL url = new URL(stringArray[0]); //the method is expecting an array, as there is only one parameter passed we can set it to the first entry in the array 0
                HttpURLConnection httpConnect = (HttpURLConnection) url.openConnection();

                httpConnect.setRequestMethod("PUT");
                httpConnect.setDoOutput(true);
                httpConnect.setRequestProperty("Content-Type", "application/json");

                JSONObject jObj = new JSONObject();
                jObj.put("UserID", "Mr_Android");

                try{
                    DataOutputStream dOutStream = new DataOutputStream(httpConnect.getOutputStream());
                    dOutStream.writeBytes(jObj.toString());
                    dOutStream.flush();
                    dOutStream.close();

                    InputStream is = new BufferedInputStream(httpConnect.getInputStream());
                    InputStreamReader inStrRead = new InputStreamReader(is); //For reading the input from the input stream
                    StringBuilder sb = new StringBuilder();

                    //Perform the reconstruction of input data into the stringbuilder
                    int data = inStrRead.read();
                    while(data != -1){
                        char current = (char) data;
                        data = inStrRead.read();
                        sb.append(current);
                    }

                    strToReturn = sb.toString(); //Cast the stringbuilder to a string
                    sb.setLength(0);
                    is.close(); //Close the InputStream
                }
                catch (Exception e){
                    Log.e("DAMMIT", "WE has an error", e);
                }
                finally {
                    httpConnect.disconnect(); //Disconnect from web server
                }
            }
            catch (Exception e){
                Log.e("Outer Tried", "WE has an error", e);
            }
            return strToReturn;
        }

        //Once the asynchronous call has finished change the text on the button
        @Override
        protected void onPostExecute(String result){
            if (!result.isEmpty()) {
                parseJSONFromGetQueueAsyncTask(result);
            }
        }
    }

    private void parseJSONFromGetQueueAsyncTask(String currentDrink){
        JSONObject jObject;
        String drinkName = "";
        int orderID = 0;
        //StringBuilder sb = new StringBuilder();
        try {
            jObject = new JSONObject(currentDrink);

            drinkName = jObject.getString("Drink");
            orderID = jObject.getInt("OrderID");
            //sb.append(" OrderID: ");
            //sb.append(jObject.getString("OrderID"));

        }catch (Exception e){
            Log.e("JSON ERRORZ", "Cannot array", e);
        }

        createNotification(drinkName);
        if(notificationID != orderID){
            performNotification(orderID);
        }
    }

}