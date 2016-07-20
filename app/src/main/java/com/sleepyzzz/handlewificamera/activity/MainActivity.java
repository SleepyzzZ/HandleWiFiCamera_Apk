package com.sleepyzzz.handlewificamera.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
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

import java.util.ArrayList;

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

    private static final int SDK_PERMISSION_REQUEST = 127;
    private String permissionInfo;

    private String RTSP_IP;

    private String RTSP_URL;

    private static final String HTTP_URL = "http://10.10.77.129:8080/DetectionServer/DetectionServlet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        getPersimmions();
    }

    private void getPersimmions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            /*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)){
                return true;
            }else{
                permissionsList.add(permission);
                return false;
            }

        }else{
            return true;
        }
    }

    @OnClick({R.id.btn_play, R.id.btn_photo})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_play:
                RTSP_IP = mEtInput.getText().toString();
                if (RTSP_IP.length() <= 0) {
                    Toast.makeText(DTApplication.getContext(),
                            "Rtsp URL is null,please input...", Toast.LENGTH_SHORT).show();
                    PlayerActivity.actionStart(MainActivity.this, "rtsp://218.204.223.237:554/live/1/66251FC11353191F/e7ooqwcfbqjoo80j.sdp");
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
