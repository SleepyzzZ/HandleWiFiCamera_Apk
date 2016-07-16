package com.sleepyzzz.handlewificamera.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sleepyzzz.handlewificamera.R;
import com.sleepyzzz.handlewificamera.base.DTApplication;
import com.sleepyzzz.handlewificamera.constant.Const;
import com.sleepyzzz.handlewificamera.entity.ServerInfo;
import com.sleepyzzz.photo_selector.activity.PhotoPickerActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "AppCompatActivity";

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.et_input)
    EditText mEtInput;
    @Bind(R.id.btn_play)
    Button mBtnPlay;
    @Bind(R.id.btn_photo)
    Button mBtnPhoto;

    private String RTSP_IP;

    private String RTSP_URL;

    private static final String HTTP_URL = "http://10.10.77.129:8080/DetectionServer/DetectionServlet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
    }

    @OnClick({R.id.btn_play, R.id.btn_photo})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_play:
                RTSP_IP = mEtInput.getText().toString();
                if (RTSP_IP.length() <= 0) {
                    Toast.makeText(DTApplication.getContext(),
                            "Rtsp URL is null,please input...", Toast.LENGTH_SHORT).show();
                } else {
                    ServerInfo.getInstance().setServerInfo(RTSP_IP, Const.DEFAULT_CMDSERVER_PORT);
                    RTSP_URL = "rtsp://" + RTSP_IP + ":" + Const.DEFAULT_RTSP_PORT;
                    PlayerActivity.actionStart(MainActivity.this, RTSP_URL);
                }
                break;
            case R.id.btn_photo:
                PhotoPickerActivity.actionStart(MainActivity.this, 9,
                        DTApplication.mSDCardPath + "/SmartCamera/", HTTP_URL);
                break;
        }
    }
}
