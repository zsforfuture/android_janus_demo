package com.demo.janus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.demo.janus.meet.MeetingClient;
import com.demo.janus.meet.MeetingOptions;

public class TestMsgActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_msg);
        MeetingClient.getInstance();
        findViewById(R.id.connect).setOnClickListener(v -> {
            MeetingOptions meetingOptions = new MeetingOptions();
            meetingOptions.meetNum = 1234;
            meetingOptions.userName = "11";
            MeetingClient.getInstance().joinMeeting(meetingOptions);
        });
    }
}