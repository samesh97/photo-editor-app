package com.sba.sinhalaphotoeditor.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.sba.sinhalaphotoeditor.Config.Constants;
import com.sba.sinhalaphotoeditor.R;

import static com.sba.sinhalaphotoeditor.Config.Constants.FIREBASE_PAYLOAD_MESSAGE_TEXT;
import static com.sba.sinhalaphotoeditor.Config.Constants.FIREBASE_PAYLOAD_TITLE_TEXT;

public class NotificationView extends AppCompatActivity
{

    private TextView titleTextView,descriptionTextView;
    private Intent intent;

//    @Override
//    public void onBackPressed()
//    {
//        Intent intent = new Intent(NotificationView.this,MainActivity.class);
//        startActivity(intent);
//        finish();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_view);

        initViews();
        intent = getIntent();
        if(intent != null)
        {
            setData();
        }


    }

    private void setData()
    {
        String title = intent.getStringExtra(FIREBASE_PAYLOAD_TITLE_TEXT);
        String message = intent.getStringExtra(FIREBASE_PAYLOAD_MESSAGE_TEXT);


        if(title == null || message == null || (title.equals("") || title.trim().equals("") || message.equals("") || message.trim().equals("")))
        {
            Intent intent = new Intent(NotificationView.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            titleTextView.setText("" + title);
            descriptionTextView.setText("" + message);
        }
    }

    private void initViews()
    {
        titleTextView = findViewById(R.id.title);
        descriptionTextView = findViewById(R.id.description);
    }
}