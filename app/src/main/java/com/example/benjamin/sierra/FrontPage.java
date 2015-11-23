package com.example.benjamin.sierra;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FrontPage extends AppCompatActivity {

    final private String LANAddress = "http://192.168.0.107:8888";
    final private String ClientID = "Mr_Android";
    private int notificationID = 0;
    private NotificationManager nMananger = null;
    private NotificationCompat.Builder notificationBuilder;
    private Button btnPour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_front_page);
        btnPour = (Button)findViewById(R.id.btnPour);
        notificationBuilder = new NotificationCompat.Builder(this);

        final Button makedrink = (Button) findViewById(R.id.make_drink);

        if(notificationID != 0)
            enablePourButton();
        else {
            disablePourButton();
            getFirstInQueue();
        }

        makedrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent drinklistintent = new Intent(FrontPage.this, DrinkList.class);
                drinklistintent.putExtra("LANAddress", LANAddress);
                drinklistintent.putExtra("ClientID", ClientID);
                startActivity(drinklistintent);
            }
        });


        final Button myqueue = (Button) findViewById(R.id.queue);
        myqueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent queueintent = new Intent(FrontPage.this, QueuePage.class);
                queueintent.putExtra("LANAddress", LANAddress);
                queueintent.putExtra("ClientID", ClientID);
                startActivity(queueintent);
            }
        });

        btnPour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginToPour();
            }
        });

        CountDown();
    }



    private void CountDown(){
        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                    int num = (int) millisUntilFinished / 1000;
                    if(num % 2 == 0)
                        btnPour.setTextColor(Color.RED);
                    else
                        btnPour.setTextColor(Color.YELLOW);
            }

            @Override
            public void onFinish() {
                getFirstInQueue();
                this.start();
            }
        }.start();
    }

    private void getFirstInQueue(){
        final String firstInQueueURL = LANAddress + "/firstInQueue";
        GetFirstInQueueAsyncTask getFirst = new GetFirstInQueueAsyncTask();
        getFirst.execute(firstInQueueURL);
    }

    private void createNotification(String nameOfDrink){
        Intent intent = new Intent(this, FrontPage.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        notificationBuilder.setContentIntent(pIntent);
        notificationBuilder.setContentTitle("Drink Mixer");
        notificationBuilder.setContentText(nameOfDrink + " is ready to pour");
        notificationBuilder.setTicker(nameOfDrink + " is ready to pour");
        notificationBuilder.setDefaults(Notification.DEFAULT_ALL);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.addAction(R.drawable.shotglass, "Pour", null);
        notificationBuilder.setAutoCancel(true);
    }

    private void performNotification(int orderID){
        Notification notification = notificationBuilder.build();
        nMananger = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        /*if (notificationID != 0) {
            nMananger.notify(orderID, notification);
        }*/
        nMananger.notify(orderID, notification);
        notificationID = orderID;
        enablePourButton();

    }

    private void disablePourButton(){
        btnPour.setEnabled(false);
        btnPour.setAlpha(0.0f);

        TextView txtToPour = (TextView) findViewById(R.id.txtDrinkReadyToPour);
        txtToPour.setAlpha(0.0f);
    }

    private void enablePourButton() {
        btnPour.setEnabled(true);
        btnPour.setAlpha(1.0f);

        TextView txtToPour = (TextView) findViewById(R.id.txtDrinkReadyToPour);
        txtToPour.setAlpha(1.0f);

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
                jObj.put("UserID", ClientID);

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
        String UserID = "";
        int orderID = 0;

        try {
            jObject = new JSONObject(currentDrink);
            UserID = jObject.getString("UserID");
            drinkName = jObject.getString("Drink");
            orderID = jObject.getInt("OrderID");
        }catch (Exception e){
            Log.e("JSON ERRORZ", "Cannot Parse JSON", e);
        }

        if (UserID.equals(ClientID)) {
            createNotification(drinkName);
            if (notificationID != orderID) {
                performNotification(orderID);
                //TextView txtToPour = (TextView) findViewById(R.id.txtDrinkReadyToPour);
                //txtToPour.setText(drinkName + " Ready to Pour");
                Button btnPour = (Button)findViewById(R.id.btnPour);
                btnPour.setText(drinkName + "\nPress to Pour");
            }
        }else {
            try {
                nMananger.cancelAll();
            }
            catch (NullPointerException e){
                Log.e("NotificationMan Error", "This always does this on new launch, so chill", e);
            }
            disablePourButton();
        }
    }

    private void beginToPour(){
        disablePourButton();
        nMananger.cancel(notificationID);
        final String pourDrinkURL = LANAddress +"/pourDrink";
        PourDrinkAsyncTask pourTheDrink = new PourDrinkAsyncTask();
        pourTheDrink.execute(pourDrinkURL);

    }

    private class PourDrinkAsyncTask extends AsyncTask<String, Void, String> {

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
                jObj.put("OrderID", notificationID );

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
                parseJSONFromPourDrinkAsyncTask(result);
            }
        }
    }

    private void parseJSONFromPourDrinkAsyncTask(String serverResponse){
        JSONObject jObject;
        String serverSays = "";

        try {
            jObject = new JSONObject(serverResponse);
            serverSays = jObject.getString("serverSays");
        }catch (Exception e){
            Log.e("JSON ERRORZ", "Cannot Parse JSON", e);
        }

        /*if (serverSays.equals("Success pouring Drink")) {

        }
        else if (serverSays.equals("You've had enough for one night")){

        }*/
    }
}