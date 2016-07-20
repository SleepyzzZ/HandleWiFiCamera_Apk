package com.sleepyzzz.handlewificamera.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClientOption;
import com.orhanobut.logger.Logger;
import com.sleepyzzz.handlewificamera.R;
import com.sleepyzzz.handlewificamera.base.DTApplication;
import com.sleepyzzz.handlewificamera.constant.Const;
import com.sleepyzzz.handlewificamera.custom.PlayerView;
import com.sleepyzzz.handlewificamera.entity.GpsInfo;
import com.sleepyzzz.handlewificamera.entity.MessageEvent;
import com.sleepyzzz.handlewificamera.location.LocationService;
import com.sleepyzzz.handlewificamera.socket.CmdEventHelper;
import com.sleepyzzz.handlewificamera.socket.SocketThreadManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-07-16
 * Time: 12:36
 * FIXME
 */
public class PlayerActivity extends AppCompatActivity implements Handler.Callback,
        SeekBar.OnSeekBarChangeListener, PlayerView.OnChangeListener {

    private static final String TAG = "PlayerActivity";

    public static final String EXTRA_RTSP_URL = "rtsp_url";

    @Bind(R.id.pv_video)
    PlayerView mPvVideo;
    @Bind(R.id.tv_title)
    TextView mTvTitle;
    @Bind(R.id.rl_title)
    RelativeLayout mRlTitle;
    @Bind(R.id.tv_time)
    TextView mTvTime;
    @Bind(R.id.sb_video)
    SeekBar mSbVideo;
    @Bind(R.id.tv_length)
    TextView mTvLength;
    @Bind(R.id.ib_lock)
    ImageButton mIbLock;
    @Bind(R.id.ib_play)
    ImageButton mIbPlay;
    @Bind(R.id.ib_menu)
    ImageButton mIbMenu;
    @Bind(R.id.ib_capture)
    ImageButton mIbCapture;
    @Bind(R.id.ll_overlay_btns)
    LinearLayout mLlOverlayBtns;
    @Bind(R.id.ll_overlay)
    LinearLayout mLlOverlay;
    @Bind(R.id.tv_buffer)
    TextView mTvBuffer;
    @Bind(R.id.rl_loading)
    LinearLayout mRlLoading;

    private static final int SHOW_PROGRESS = 0;
    private static final int ON_LOADED = 1;
    private static final int HIDE_OVERLAY = 2;

    private String mRtspUrl;

    private Handler mHandler;

    private boolean isLocked = false;

    //private ProgressDialog mProgressDialog;

    /**
     * GPS
     */
    private LocationService mLocationService;
    private LocationClientOption mOption;
    private static final int LOCATION_FREQUENCE = 60 * 100;
    private static final int LOCATION_DISTANCE = 100;

    public static void actionStart(Context context, String data) {

        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(EXTRA_RTSP_URL, data);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);

        mRtspUrl = getIntent().getStringExtra(EXTRA_RTSP_URL);

        /**
         * 启动gps服务
         * 配置模式
         */
        mLocationService = ((DTApplication) getApplication()).mLocationService;
        mLocationService.stop();
        mOption = new LocationClientOption();
        mOption = mLocationService.getDefaultLocationClientOption();
        mOption.setOpenAutoNotifyMode(LOCATION_FREQUENCE,
                LOCATION_DISTANCE, LocationClientOption.LOC_SENSITIVITY_HIGHT);
        mLocationService.setLocationOption(mOption);

        mHandler = new Handler(this);
        SocketThreadManager.getInstance();

        /*mProgressDialog = new ProgressDialog(PlayerActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle("Tips");
        mProgressDialog.setMessage("receive picture...");*/

        mSbVideo.setOnSeekBarChangeListener(this);
        // 设置播放器参数(ms)
        mPvVideo.setNetWorkCache(2000);
        // 初始化播放器
        mPvVideo.initPlayer(mRtspUrl);
        // 设置事件监听，监听缓冲进度等
        mPvVideo.setOnChangeListener(this);
        // 开始播放
        mPvVideo.start();
        // 初始化界面
        mTvTitle.setText(mRtspUrl);

        EventBus.getDefault().register(this);

        showLoadingView();

        hideOverlay();
    }

    @Override
    protected void onPause() {

        //暂停定位服务
        mLocationService.unregisterListener(mLocationListener);
        mLocationService.stop();

        if (mPvVideo.isPlaying()) {
            mPvVideo.pause();
            mIbPlay.setBackgroundResource(R.drawable.ic_play);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {

        //启动定位服务
        mLocationService.registerListener(mLocationListener);
        mLocationService.start();

        mPvVideo.play();
        mIbPlay.setBackgroundResource(R.drawable.ic_pause);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 显示播放控制工具栏
     */
    private void showOverlay() {

        mRlTitle.setVisibility(View.VISIBLE);
        mLlOverlay.setVisibility(View.VISIBLE);
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
        mHandler.removeMessages(HIDE_OVERLAY);
        mHandler.sendEmptyMessageDelayed(HIDE_OVERLAY, 5 * 1000);
    }

    /**
     * 隐藏播放控制工具栏
     */
    private void hideOverlay() {
        mRlTitle.setVisibility(View.GONE);
        mLlOverlay.setVisibility(View.GONE);
        mHandler.removeMessages(SHOW_PROGRESS);
    }

    /**
     * 显示缓冲界面
     */
    private void showLoadingView() {

        mRlLoading.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏缓冲界面
     */
    private void hideLoadingView() {

        mRlLoading.setVisibility(View.GONE);
    }

    /**
     * 设置播放进度
     * @return
     */
    private int setOverlayProgress() {

        if (mPvVideo == null) {

            return 0;
        }

        int time = (int) mPvVideo.getTime();
        int length = (int) mPvVideo.getLength();
        // boolean isSeekable = mPlayerView.canSeekable() && length > 0;
        // ibFarward.setVisibility(isSeekable ? View.VISIBLE : View.GONE);
        // ibBackward.setVisibility(isSeekable ? View.VISIBLE : View.GONE);
        mSbVideo.setMax(length);
        mSbVideo.setProgress(time);

        if (time >= 0) {

            mTvTime.setText(millisToString(time, false));
        }

        if (length >= 0) {

            mTvLength.setText(millisToString(length, false));
        }

        return time;
    }

    private BDLocationListener mLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation != null) {
                GpsInfo.getInstance().setSucess(true);
                GpsInfo.getInstance().setLongitude(bdLocation.getLongitude());
                GpsInfo.getInstance().setLatitude(bdLocation.getLatitude());
                Logger.t(TAG).d("Longitude: %f, Latitude: %f, Altitude: %f",
                        bdLocation.getLongitude(), bdLocation.getLatitude(), bdLocation.getAltitude());

                if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) { //GPS定位结果
                    Logger.t(TAG).d("Altitude: %f", bdLocation.getAltitude());
                } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) { //网络定位结果
                    Logger.t(TAG).d("采用wifi直连模式,此场景不适用");
                } else if (bdLocation.getLocType() == BDLocation.TypeOffLineLocation) { //离线定位结果
                    Logger.t(TAG).d("在无法收到卫星的情况下进入离线定位场景");
                } else if (bdLocation.getLocType() == BDLocation.TypeServerError) {
                    Logger.t(TAG).d("服务端网路定位失败");
                } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkException) {
                    Logger.t(TAG).d("网络不同导致定位失败，请检查网络是否通畅");
                } else if (bdLocation.getLocType() == BDLocation.TypeCriteriaException) {
                    Logger.t(TAG).d("无法获取有效定位依据导致定位失败，一般是由于手机的原因，" +
                            "处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
            }
        }
    };

    @OnClick({R.id.ib_lock, R.id.ib_play, R.id.ib_menu, R.id.ib_capture})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_lock:
                if (isLocked) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    mIbLock.setBackgroundResource(R.drawable.ic_unlock);
                    isLocked = false;
                } else {

                    if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        mIbLock.setBackgroundResource(R.drawable.ic_locked);
                        isLocked = true;
                    } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        mIbLock.setBackgroundResource(R.drawable.ic_locked);
                        isLocked = true;
                    }
                }
                break;
            case R.id.ib_play:
                if (mPvVideo.isPlaying()) {

                    mPvVideo.pause();
                    mIbPlay.setBackgroundResource(R.drawable.ic_play);
                } else {

                    mPvVideo.play();
                    mIbPlay.setBackgroundResource(R.drawable.ic_pause);
                }
                break;
            case R.id.ib_menu:
                break;
            case R.id.ib_capture:
                SocketThreadManager.getInstance().sendMsg(
                        CmdEventHelper.getInstance().generateCmdMsg(Const.CMD_TAKE_PHOTO));
                break;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case SHOW_PROGRESS:
                setOverlayProgress();
                mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 20);
                break;

            case ON_LOADED:
                showOverlay();
                hideLoadingView();
                break;

            case HIDE_OVERLAY:
                hideOverlay();
                break;

            default:
                break;

        }

        return false;
    }

    @Override
    public void onBufferChanged(float buffer) {

        if (buffer >= 100) {

            hideLoadingView();
        } else {

            showLoadingView();
        }

        mTvBuffer.setText("正在缓冲中..." + (int) buffer + "%");
    }

    @Override
    public void onLoadComplet() {

        mHandler.sendEmptyMessage(ON_LOADED);
    }

    @Override
    public void onError() {

        Toast.makeText(DTApplication.getContext(), "Player Error Occur！",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onEnd() {

        finish();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (fromUser && mPvVideo.canSeekable()) {

            mPvVideo.setTime(progress);
            setOverlayProgress();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_UP) {

            if (mLlOverlay.getVisibility() != View.VISIBLE) {

                showOverlay();
            } else {

                hideOverlay();
            }
        }

        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mPvVideo.changeSurfaceSize();
        super.onConfigurationChanged(newConfig);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showEchoMsgFromServer(MessageEvent.EchoMsgEvent event) {
        int type = event.getEchoCode();
        switch (type) {
            case Const.CMD_ECHO_TAKE_PHOTO:
                Bundle args = event.getEchoMsg();
                String photoName = new String(args.getByteArray("CMD_ECHO_TAKE_PHOTO_NAME"));
                Toast.makeText(DTApplication.getContext(),
                        args.getString("CMD_ECHO_TAKE_PHOTO_ISSUCCEED")
                        +"\n"
                        +"photoName: "
                        +photoName
                        +"\n"
                        +"photo size: "
                        +args.getInt("CMD_ECHO_TAKE_PHOTO_SIZE")
                        +"bytes",Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void notifyMedia(MessageEvent.NotifyMediaEvent event) {
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://"+event.getPhotoPath())));
    }

    private String millisToString(long millis, boolean text) {

        boolean negative = millis < 0;
        millis = Math.abs(millis);
        int mini_sec = (int) millis % 1000;
        millis /= 1000;
        int sec = (int) (millis % 60);
        millis /= 60;
        int min = (int) (millis % 60);
        millis /= 60;
        int hours = (int) millis;

        String time;
        DecimalFormat format = (DecimalFormat) NumberFormat
                .getInstance(Locale.US);
        format.applyPattern("00");

        DecimalFormat format2 = (DecimalFormat) NumberFormat
                .getInstance(Locale.US);
        format2.applyPattern("000");
        if (text) {

            if (millis > 0)
                time = (negative ? "-" : "") + hours + "h" + format.format(min)
                        + "min";
            else if (min > 0)
                time = (negative ? "-" : "") + min + "min";
            else
                time = (negative ? "-" : "") + sec + "s";
        } else {

            if (millis > 0)
                time = (negative ? "-" : "") + hours + ":" + format.format(min)
                        + ":" + format.format(sec) + ":"
                        + format2.format(mini_sec);
            else
                time = (negative ? "-" : "") + min + ":" + format.format(sec)
                        + ":" + format2.format(mini_sec);
        }

        return time;
    }
}
