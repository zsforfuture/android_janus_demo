package com.demo.janus;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.janus.constant.Constant;
import com.demo.janus.meet.MeetingClient;
import com.demo.janus.meet.MeetingOptions;
import com.demo.janus.meet.Resolution;
import com.demo.janus.permission.ISimplePermissionListener;
import com.demo.janus.permission.PermissionManager;

public class MainActivity extends AppCompatActivity {

    private EditText urlEt;
    private EditText meetNumET;
    private EditText nameEt;

    private RadioGroup statsRadioGroup;
    private RadioButton yesRadio;
    private RadioButton noRadio;


    private RadioGroup resolutionRadioGroup;
    private RadioButton resolution720P;
    private RadioButton resolution480P;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MeetingClient.init(getApplicationContext());
        setContentView(R.layout.activity_main);
        initView();
        findViewById(R.id.janus_test1).setOnClickListener(v -> {
            PermissionManager.getInstance().requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    (ISimplePermissionListener) allGranted -> {
                        if (allGranted) {
                            startActivity(new Intent(this, DemoActivity.class));
                        } else {
                            Toast.makeText(this, "请打开摄像头和录音功能权限", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        findViewById(R.id.janus_video_room).setOnClickListener(v -> {
            PermissionManager.getInstance().requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    (ISimplePermissionListener) allGranted -> {
                        if (allGranted) {
                            jumpVideoRoom();
                        } else {
                            Toast.makeText(this, "请打开摄像头和录音功能权限", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void jumpVideoRoom() {
        Constant.SERVER_URL = urlEt.getText().toString();
        MeetingOptions.userName = nameEt.getText().toString();
        MeetingOptions.meetNum = Integer.parseInt(meetNumET.getText().toString());
        MeetingOptions.showStats = yesRadio.isChecked();
        MeetingOptions.resolution = resolution720P.isChecked() ? Resolution.Resolution_720P : Resolution.Resolution_480P;
        startActivity(new Intent(this, VideoRoomActivity.class));
    }

    private void initView() {
        urlEt = findViewById(R.id.server_url);
        meetNumET = findViewById(R.id.room_id);
        nameEt = findViewById(R.id.user_name);

        statsRadioGroup = findViewById(R.id.radioGroup1);
        yesRadio = findViewById(R.id.radio_yes);
        noRadio = findViewById(R.id.radio_no);

        resolutionRadioGroup = findViewById(R.id.radioGroup2);
        resolution720P = findViewById(R.id.radio_720);
        resolution480P = findViewById(R.id.radio_480);

        urlEt.setText(Constant.SERVER_URL);
        meetNumET.setText(String.valueOf(MeetingOptions.meetNum));
        nameEt.setText(MeetingOptions.userName);

        if (MeetingOptions.showStats) {
            yesRadio.setChecked(true);
        } else {
            noRadio.setChecked(true);
        }

        if (MeetingOptions.resolution == Resolution.Resolution_720P) {
            resolution720P.setChecked(true);
        } else {
            resolution480P.setChecked(true);
        }
    }

}