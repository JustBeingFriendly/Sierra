package com.example.benjamin.sierra;


import android.os.AsyncTask;
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

    final private String LANAddress = "http://192.168.0.107:8081";

    private Animation fadeIn, fadeOut;//fadeInOut, fadeOutPreventOrder, orderFadeIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drinklist);

        GetListOfDrinks getListOfDrinks = new GetListOfDrinks();
        final String drinksListURL = LANAddress + "/drinksList";
        getListOfDrinks.execute(drinksListURL);
        setAnimationParameters();

        TextView tvSuccess = (TextView)findViewById(R.id.txtSuccess);
        tvSuccess.startAnimation(fadeOut);
        Button btnOrder = (Button)findViewById(R.id.btnOrder);
        Button btnCancel = (Button)findViewById(R.id.btnCancel);
        btnOrder.startAnimation(fadeOut);
        btnCancel.startAnimation(fadeOut);
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
        fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(2000);
        fadeOut.setFillAfter(true);

        fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(400);
        fadeIn.setFillAfter(true);
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
                    InputStream in = new BufferedInputStream(urlConnect.getInputStream()); //This is when the connection to the web server actually executes
                    StringBuilder sb = new StringBuilder(); //For reconstructing the data returned from the input stream, stringbuilder is used as it is memory efficient
                    InputStreamReader inStrRead = new InputStreamReader(in); //For reading the input from the input stream
                    //Perform the reconstruction of input data into the stringbuilder
                    int data = inStrRead.read();
                    while(data != -1){
                        char current = (char) data;
                        data = inStrRead.read();
                        sb.append(current);
                    }
                    strToReturn = sb.toString(); //Cast the stringbuilder to a string
                    in.close(); //Close the InputStream
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

        String[] drinks = sb.toString().split(",");
        initialiseViews(drinks);
    }

    //Adds text from remote database and action listeners to buttons
    private void initialiseViews(final String[] drinkNames){

        final TextView tvDrinkOrder = (TextView) findViewById(R.id.txtDrinkOrder);

        final Button btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Button btnOrder = (Button)findViewById(R.id.btnOrder);
                btnOrder.startAnimation(fadeOut);
                btnCancel.startAnimation(fadeOut);
                tvDrinkOrder.setText("");
            }
        });


       // TextView tvSuccess = (TextView)findViewById(R.id.txtSuccess);



        final Button btnOrder = (Button)findViewById(R.id.btnOrder);
        btnOrder.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                tvDrinkOrder.startAnimation(fadeOut);
                btnOrder.startAnimation(fadeOut);
                btnCancel.startAnimation(fadeOut);
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
                        tvDrinkOrder.startAnimation(fadeIn);
                        Button btnOrder = (Button)findViewById(R.id.btnOrder);
                        Button btnCancel = (Button)findViewById(R.id.btnCancel);
                        float alp =  btnOrder.getAlpha();
                        btnOrder.startAnimation(fadeIn);
                        btnCancel.startAnimation(fadeIn);
                        if(alp == 0.0f){
                            btnOrder.startAnimation(fadeIn);
                            btnCancel.startAnimation(fadeIn);
                        }
                    }});
        }



        tvDrinkOrder.setText("");


    }

    private void commitOrder(){
        TextView tvDrinkOrder = (TextView) findViewById(R.id.txtDrinkOrder);
        final String orderDrinkURL = LANAddress + "/chooseDrink";
        final String drinkToOrder =  tvDrinkOrder.getText().toString();
        final String id = "Mr_Android";
        final String[] strArr = {orderDrinkURL, id, drinkToOrder};
        OrderDrinkPut orderDrink;

        if(tvDrinkOrder.getText() != ""){
            try {
                orderDrink = new OrderDrinkPut();
                orderDrink.execute(strArr);
            }
            catch (Exception e){
                Log.e("Uh oh", "WE has an error", e);
            }
        }

    }

    //Sends and receives a message
    private class OrderDrinkPut extends AsyncTask<String, Void, String> {

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
                TextView tvDrinkOrder = (TextView)findViewById(R.id.txtDrinkOrder);
                TextView tvSuccess = (TextView)findViewById(R.id.txtSuccess);
                tvSuccess.setText(tvDrinkOrder.getText().toString() + " Order Successful");
                tvSuccess.startAnimation(fadeOut);
                tvSuccess.setVisibility(View.VISIBLE);

                tvDrinkOrder.setText("");
            }
        }
    }
}
