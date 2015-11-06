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

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DrinkList extends AppCompatActivity {

    final String drinksListURL = "http://192.168.0.107:8081/drinksList";
    final String orderDrinkURL = "http://192.168.0.107:8081/chooseDrink";

    protected Animation fadeIn, fadeOut, fadeOutPreventOrder, orderFadeIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drinklist);

        GetListOfDrinks getListOfDrinks = new GetListOfDrinks();
        getListOfDrinks.execute(drinksListURL);
        setAnimationParameters();

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

        fadeOutPreventOrder = new AlphaAnimation(1.0f, 0.0f);
        fadeOutPreventOrder.setDuration(500);
        fadeOutPreventOrder.setFillAfter(true);

        orderFadeIn = new AlphaAnimation(0.0f, 1.0f);
        orderFadeIn.setDuration(400);
        orderFadeIn.setFillAfter(true);
    }


    //Sends a get request to the web server which returns the list of drinks in the database
    protected class GetListOfDrinks extends AsyncTask<String, Void, String> {

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
            else{
                //listOfDrinks = "Error Loading web page"; //Sets the button text to an error message
                //parentActivityWkRf.placeOrderText.setText("Please start web server");
            }

        }
    }

    //Removes the JSON encoding from the string
    protected void cleanOutputFromListOfDrinks(String listOfDrinks){
        StringBuilder sb = new StringBuilder(listOfDrinks);

        int i = sb.indexOf("[");
        sb.deleteCharAt(i);
        i = sb.indexOf("]");
        sb.deleteCharAt(i);
        i = sb.indexOf("\"");

        while (i != -1){
            sb.deleteCharAt(i);
            i = sb.indexOf("\"");
        }
        listOfDrinks = sb.toString();

        String[] drinks = listOfDrinks.split(",");
        initialiseViews(drinks);
    }

    //Adds text from remote database and action listeners to buttons
    private void initialiseViews(final String[] drinkNames){

        final TextView tv = (TextView) findViewById(R.id.txtDrinkOrder);

        Button btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                fadeOutPreventOrder.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        tv.setText("");
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                if(fadeOutPreventOrder.hasEnded() || !fadeOutPreventOrder.hasStarted())
                    tv.startAnimation(fadeOutPreventOrder);

            }
        });

        final Button btnPlaceOrder = (Button)findViewById(R.id.btnOrder);
        btnPlaceOrder.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                boolean outEnd = fadeOutPreventOrder.hasEnded();
                if(!fadeOutPreventOrder.hasStarted()) {
                    commitOrder();
                    tv.startAnimation(fadeOutPreventOrder);
                    btnPlaceOrder.startAnimation(fadeOutPreventOrder);
                }
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
                        tv.setText(b.getText().toString());
                        tv.startAnimation(fadeIn);
                        if(!orderFadeIn.hasEnded() || fadeOutPreventOrder.hasEnded())
                            btnPlaceOrder.startAnimation(orderFadeIn);
                    }});
        }


        tv.setText("");



    }

    private void commitOrder(){
        TextView tv = (TextView) findViewById(R.id.txtDrinkOrder);
        final String drinkToOrder =  tv.getText().toString();
        final String id = "Mr_Android";
        final String[] strArr = {orderDrinkURL, id, drinkToOrder};
        OrderDrinkPut orderDrink;

        if(tv.getText() != ""){
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
                TextView tv = (TextView)findViewById(R.id.txtDrinkOrder);
                tv.setText("Success");
                tv.startAnimation(fadeOut);
            }
        }
    }

}
