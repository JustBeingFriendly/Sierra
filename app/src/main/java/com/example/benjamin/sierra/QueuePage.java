package com.example.benjamin.sierra;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class QueuePage extends AppCompatActivity {

    final String drinkQueueURL = "http://192.168.0.107:8081/queue";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_page);
        getRemoteQueue();
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

    private void getRemoteQueue(){
        GetQueuePut getTheQueue = new GetQueuePut();
        getTheQueue.execute(drinkQueueURL);
    }


    //Sends and receives a message
    private class GetQueuePut extends AsyncTask<String, Void, String> {

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
                cleanOutputFromListOfDrinks(result);

            }
        }
    }

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
        createListView(drinks);
        //placeOrderText.setText("");
    }

    private void createListView(String[] drinks){

        ListView lv = (ListView) findViewById(R.id.lvQueue);
        lv.setBackgroundColor(Color.YELLOW);

        ArrayAdapter<String> adaptor = new ArrayAdapter<String>(
                this,
                R.layout.listviewlayout,
                drinks);
        lv.setAdapter(adaptor);
        createClickCallBacks();
    }

    //Allows items in the list view to be clickable
    private void createClickCallBacks(){
        ListView lv = (ListView) findViewById(R.id.lvQueue);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                TextView tv = (TextView) viewClicked;
                String str = tv.getText().toString();
                TextView tvOutput = (TextView) findViewById(R.id.txtLvSelect);
                tvOutput.setText(str);
            }
        });
    }
}
