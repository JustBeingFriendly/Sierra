package com.example.benjamin.sierra;


import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.BundleCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DrinkList extends AppCompatActivity {

    private String LANAddress;
    private String ClientID;
    private View[] orderingViews;
    private Animation txtVwFadeIn, txtVwFadeOut, orderingFadeIn, orderingFadeOut;//fadeInOut, fadeOutPreventOrder, orderFadeIn;
    private String[] drinkNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drinklist);
        Bundle extra = getIntent().getExtras();
        LANAddress = extra.getString("LANAddress");
        ClientID = extra.getString("ClientID");
        try{
            if(drinkNames.length < 0) {
                initialiseViews(drinkNames);
            }
        }catch (NullPointerException n){
            GetListOfDrinks getListOfDrinks = new GetListOfDrinks();
            final String drinksListURL = LANAddress + "/drinksList";
            getListOfDrinks.execute(drinksListURL);
        }
        setAnimationParameters();

        TextView tvSuccess = (TextView)findViewById(R.id.txtSuccess);
        tvSuccess.setVisibility(View.GONE);

        orderingViews = new View[]{
                (Button) findViewById(R.id.btnPlaceOrder),
                (Button) findViewById(R.id.btnCancelOrder),
                (TextView)findViewById(R.id.txtDrinkOrder)};

        for(View b : orderingViews){
            b.setAlpha(0.0f);
        }
        disableOrderingControls();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drinklist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setAnimationParameters(){
        txtVwFadeOut = new AlphaAnimation(1.0f, 0.0f);
        txtVwFadeOut.setDuration(2000);
        txtVwFadeOut.setFillAfter(true);

        txtVwFadeIn = new AlphaAnimation(0.0f, 1.0f);
        txtVwFadeIn.setDuration(400);
        txtVwFadeIn.setFillAfter(true);

        orderingFadeIn = new AlphaAnimation(0.0f, 1.0f);
        orderingFadeIn.setDuration(400);
        orderingFadeIn.setFillAfter(true);

        orderingFadeOut = new AlphaAnimation(1.0f, 0.0f);
        orderingFadeOut.setDuration(300);
        orderingFadeOut.setFillAfter(true);
    }

    //Sends a get request to the web server which returns the list of drinks in the database
    private class GetListOfDrinks extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... theURL){ //theURL is the parameter connectionString from cts.execute
            String strToReturn= "";
            try {
                URL url = new URL(theURL[0]); //the method is expecting an array, as there is only one parameter passed we can set it to the first entry in the array 0
                HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();

                try{
                    InputStream is = new BufferedInputStream(urlConnect.getInputStream()); //This is when the connection to the web server actually executes
                    StringBuilder sb = new StringBuilder(); //For reconstructing the data returned from the input stream, stringbuilder is used as it is memory efficient
                    InputStreamReader inStrRead = new InputStreamReader(is); //For reading the input from the input stream
                    //Perform the reconstruction of input data into the stringbuilder
                    int data = inStrRead.read();
                    while(data != -1){
                        char current = (char) data;
                        data = inStrRead.read();
                        sb.append(current);
                    }
                    strToReturn = sb.toString(); //Cast the stringbuilder to a string
                    is.close(); //Close the InputStream
                }
                catch (Exception e){
                    Log.e("DAMMIT", "WE has an error", e);
                }
                finally {
                    urlConnect.disconnect(); //Disconnect from web server
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
            if (!result.isEmpty()){
                cleanOutputFromListOfDrinks(result);
            }
        }
    }

    //Removes the JSON encoding from the string
    private void cleanOutputFromListOfDrinks(String listOfDrinks){
        StringBuilder sb = new StringBuilder();
        try {
            JSONArray jArray = new JSONArray(listOfDrinks);
            JSONObject jObject;
            for (int i=0; i < jArray.length(); i++){
                jObject = jArray.getJSONObject(i);
                sb.append(jObject.getString("DrinkName"));
                sb.append(",");
            }
        }catch (Exception e){
            Log.e("JSON ERRORZ", "Cannot array", e);
        }

        final String[] drinkNames = sb.toString().split(",");
        initialiseViews(drinkNames);
    }

    //Adds text from remote database and action listeners to buttons
    private void initialiseViews(String[] drinkNames){

        final TextView tvDrinkOrder = (TextView) findViewById(R.id.txtDrinkOrder);

        final Button btnCancel = (Button)findViewById(R.id.btnCancelOrder);
        btnCancel.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                disableOrderingControls();
                tvDrinkOrder.startAnimation(orderingFadeOut);
            }
        });

        final Button btnOrder = (Button)findViewById(R.id.btnPlaceOrder);
        btnOrder.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                tvDrinkOrder.startAnimation(txtVwFadeOut);
                disableOrderingControls();
                String txt = tvDrinkOrder.getText().toString() + " Order Successful";
                TextView tvSuccess = (TextView)findViewById(R.id.txtSuccess);
                tvSuccess.setText(txt);
                commitOrder();
            }
        });

        Button[] buttonsList ={
                (Button)findViewById(R.id.btnDrink1),
                (Button)findViewById(R.id.btnDrink2),
                (Button)findViewById(R.id.btnDrink3),
                (Button)findViewById(R.id.btnDrink4),
                (Button)findViewById(R.id.btnDrink5)
        };

        for(int i = 0; i < drinkNames.length; i++){
            buttonsList[i].setText(drinkNames[i]);
        }


        for (final Button b:  buttonsList) {
            b.setOnClickListener(
               new Button.OnClickListener() {
                    public void onClick(View v) {
                        tvDrinkOrder.setText(b.getText().toString());
                        enableOrderingControls();
                        tvDrinkOrder.startAnimation(txtVwFadeIn);

                    }});
        }
        tvDrinkOrder.setText("");
    }

    private void enableOrderingControls(){
        if(orderingViews[0].getAlpha() == 0.0f) {
            for (View b : orderingViews) {
                b.setAlpha(1.0f);
            }
        }

        if(!orderingViews[0].isEnabled()){
            for(View b : orderingViews){
                b.setEnabled(true);
                b.startAnimation(orderingFadeIn);
            }
        }
    }

    private void disableOrderingControls(){
        if(orderingViews[0].isEnabled()) {
            for (View b : orderingViews) {
                b.setEnabled(false);
                b.startAnimation(orderingFadeOut);
            }
        }
    }

    private void commitOrder(){
        TextView tvDrinkOrder = (TextView) findViewById(R.id.txtDrinkOrder);
        final String orderDrinkURL = LANAddress + "/chooseDrink";
        final String drinkToOrder =  tvDrinkOrder.getText().toString();
        final String[] strArr = {orderDrinkURL, ClientID, drinkToOrder};
        OrderDrinkAsyncTask orderDrink;

        if(tvDrinkOrder.getText() != ""){
            try {
                orderDrink = new OrderDrinkAsyncTask();
                orderDrink.execute(strArr);
            }
            catch (Exception e){
                Log.e("Uh oh", "WE has an error", e);
            }
        }

    }

    //Sends and receives a message
    private class OrderDrinkAsyncTask extends AsyncTask<String, Void, String> {

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
                jObj.put("UserID", stringArray[1]);
                jObj.put("Drink", stringArray[2]);

                try{
                    DataOutputStream dOutStream = new DataOutputStream(httpConnect.getOutputStream());
                    dOutStream.writeBytes(jObj.toString());
                    dOutStream.flush();
                    dOutStream.close();

                    InputStream is = httpConnect.getInputStream();
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
                //TextView tvDrinkOrder = (TextView)findViewById(R.id.txtDrinkOrder);
                //String txt = tvDrinkOrder.getText().toString() + " Order Successful";
                TextView tvSuccess = (TextView)findViewById(R.id.txtSuccess);
                //tvSuccess.setText(txt);
                tvSuccess.setVisibility(View.VISIBLE);
                tvSuccess.startAnimation(txtVwFadeOut);


                //tvDrinkOrder.setText("");
            }
        }
    }
}
