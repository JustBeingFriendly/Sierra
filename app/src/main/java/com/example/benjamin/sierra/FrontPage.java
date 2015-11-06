package com.example.benjamin.sierra;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FrontPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);

        final Button makedrink = (Button) findViewById(R.id.make_drink);
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
}