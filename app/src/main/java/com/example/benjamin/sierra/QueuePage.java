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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class QueuePage extends AppCompatActivity {

    final private String LANAddress = "http://192.168.0.107:8081";
    private View[] cancelingViews;
    private Animation txtVwfadeIn, txtVwfadeOut, cancelingFadeIn, cancelingFadeOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_page);

        getQueue();

        cancelingViews = new View[]{
                (Button) findViewById(R.id.btnCancelQueueYes),
                (Button) findViewById(R.id.btnCancelQueueNo),
                (TextView)findViewById(R.id.txtLvSelect)};

        addButtonActionListeners();

        setAnimationParameters();

        for(View v : cancelingViews){
            v.setAlpha(0.0f);
        }
        disableCancelControls();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_queue_page, menu);
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
        txtVwfadeOut = new AlphaAnimation(1.0f, 0.0f);
        txtVwfadeOut.setDuration(2000);
        txtVwfadeOut.setFillAfter(true);

        txtVwfadeIn = new AlphaAnimation(0.0f, 1.0f);
        txtVwfadeIn.setDuration(400);
        txtVwfadeIn.setFillAfter(true);

        cancelingFadeIn = new AlphaAnimation(0.0f, 1.0f);
        cancelingFadeIn.setDuration(400);
        cancelingFadeIn.setFillAfter(true);

        cancelingFadeOut = new AlphaAnimation(1.0f, 0.0f);
        cancelingFadeOut.setDuration(300);
        cancelingFadeOut.setFillAfter(true);
    }

    private void getQueue(){
        final String drinkQueueURL = LANAddress + "/getQueue";
        GetQueueAsyncTask getTheQueue = new GetQueueAsyncTask();
        getTheQueue.execute(drinkQueueURL);
    }

    //Sends and receives a message
    private class GetQueueAsyncTask extends AsyncTask<String, Void, String> {

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
                parseJSONFromGetQueueAsyncTask(result);

            }
        }
    }

    private void parseJSONFromGetQueueAsyncTask(String listOfDrinks){

        StringBuilder sb = new StringBuilder();
        try {
            JSONArray jArray = new JSONArray(listOfDrinks);
            JSONObject jObject;
            for (int i=0; i < jArray.length(); i++){
                jObject = jArray.getJSONObject(i);
                sb.append("\n");
                sb.append(jObject.getString("Drink"));
                sb.append("\n\n OrderID: ");
                sb.append(jObject.getString("OrderID"));
                sb.append("\n,");
            }
        }catch (Exception e){
            Log.e("JSON ERRORZ", "Cannot array", e);
        }

        //listOfDrinks = listOfDrinks.replaceAll("[\\[|\\]|\"]", "");

        String[] queueList = sb.toString().split(",");

        createListView(queueList);
    }

    private void createListView(String[] drinks){

        ListView lv = (ListView) findViewById(R.id.lvQueue);
        ArrayAdapter<String> adaptor = new ArrayAdapter<>(
                this,
                R.layout.listviewlayout,
                drinks);
        lv.setAdapter(adaptor);
        createClickCallBacks();
    }

    //Allows items in the list view to be clickable
    private void createClickCallBacks(){
        final ListView listViewQueue = (ListView) findViewById(R.id.lvQueue);
        listViewQueue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                TextView tv = (TextView) viewClicked;
                String orderInfo = tv.getText().toString().replaceAll("[\n]", "");
                if(orderInfo.equals(""))
                    enableCancelControls();
                TextView txtSelect = (TextView) findViewById(R.id.txtLvSelect);
                txtSelect.setText("Cancel " + orderInfo + "?");
                txtSelect.setTag(orderInfo);
            }
        });
    }

    private void addButtonActionListeners() {

        final Button btnConfirmCancellation = (Button)findViewById(R.id.btnCancelQueueYes);
        final Button btnDenyCancellation = (Button)findViewById(R.id.btnCancelQueueNo);

        btnConfirmCancellation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDrink();
                disableCancelControls();
            }
        });
        btnDenyCancellation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableCancelControls();
            }
        });
    }

    private void cancelDrink(){
        final String cancelDrinkURL = LANAddress + "/cancelOrder";
        TextView txtSelect = (TextView) findViewById(R.id.txtLvSelect);
        String txtSelectTagInfo = (String) txtSelect.getTag();
        int subStrPosition = txtSelectTagInfo.lastIndexOf(":") + 1;
        int length = txtSelectTagInfo.length();
        String orderID = txtSelectTagInfo.substring(subStrPosition, length).trim();
        CancelDrinkAsyncTask cancelAsyncTask = new CancelDrinkAsyncTask();
        final String[] stringArr = {cancelDrinkURL, orderID};
        cancelAsyncTask.execute(stringArr);
    }

    private class CancelDrinkAsyncTask extends AsyncTask<String, Void, String> {

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
                jObj.put("OrderID", stringArray[1]);

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
            //if (!result.isEmpty()) {
                getQueue();
               // ListView lv = (ListView) findViewById(R.id.lvQueue);
               // ArrayAdapter<String> adaptor = (ArrayAdapter) lv.getAdapter();
                //adaptor.notifyDataSetChanged();
            //}
        }
    }

    private void enableCancelControls(){

        if(cancelingViews[0].getAlpha() == 0.0f) {
            for (View b : cancelingViews) {
                b.setAlpha(1.0f);
            }
        }

        if(!cancelingViews[0].isEnabled()){
            for(View b : cancelingViews){
                b.setEnabled(true);
                b.startAnimation(cancelingFadeIn);
            }
        }
    }

    private void disableCancelControls(){
        if(cancelingViews[0].isEnabled()) {
            for (View b : cancelingViews) {
                b.setEnabled(false);
                b.startAnimation(cancelingFadeOut);
            }
        }
    }
}
