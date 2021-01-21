package com.baidu.track.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.baidu.androidnet.GetExample;
import com.baidu.androidnet.PostExample;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.sener.SenserDetect;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.fence.FenceAlarmPushInfo;
import com.baidu.trace.api.fence.MonitoredAction;
import com.baidu.trace.api.track.LatestPoint;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.LocationMode;
import com.baidu.trace.model.OnCustomAttributeListener;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TraceLocation;
import com.baidu.track.R;
import com.baidu.track.TrackApplication;
import com.baidu.track.model.CurrentLocation;
import com.baidu.track.receiver.TrackReceiver;
import com.baidu.track.utils.CommonUtil;
import com.baidu.track.utils.Constants;
import com.baidu.track.utils.MapUtil;
import com.baidu.track.utils.ViewUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 轨迹追踪
 */
public class TracingActivity extends BaseActivity implements View.OnClickListener, SensorEventListener {

    private TrackApplication trackApp = null;

    private ViewUtil viewUtil = null;

    private Button traceBtn = null;

    private Button gatherBtn = null;

    private Button getHttp = null;

    private Button postHttp = null;

    private NotificationManager notificationManager = null;

    private PowerManager powerManager = null;

    private PowerManager.WakeLock wakeLock = null;

    private TrackReceiver trackReceiver = null;

    /**
     * 地图工具
     */
    private MapUtil mapUtil = null;

    /**
     * 轨迹服务监听器
     */
    private OnTraceListener traceListener = null;

    /**
     * 轨迹监听器(用于接收纠偏后实时位置回调)
     */
    private OnTrackListener trackListener = null;

    /**
     * Entity监听器(用于接收实时定位回调)
     */
    private OnEntityListener entityListener = null;
    /**
     * 设置用户自定义属性
     */
    private OnCustomAttributeListener mCustomAttributeListener = null;
    /**
     * 实时定位任务
     */
    private RealTimeHandler realTimeHandler = new RealTimeHandler();

    private RealTimeLocRunnable realTimeLocRunnable = null;

    private boolean isRealTimeRunning = true;

    private int notifyId = 0;
    //传感器部分
    private SensorManager mSensorManager = null;
    private Sensor Sensor_Acc = null;
    private float[] Acc_data = null;
    //    private float[] gravity = null;
    //GPS全限权限部分————onResume
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
    //定位管理器
    private LocationManager locManager = null;
    private GpsStatus.Listener gpsStatusListener = null;
    int SatellitesCount= 0;
    //wifi管理
    private WifiManager wifimanager;
    private int WifiTag = 0;//未连接0;已连接1
    private String SSID = "None";
    private BroadcastReceiver wifibroadcastreceiver = null;
    /**
     * 打包周期
     */
    public int packInterval = Constants.DEFAULT_PACK_INTERVAL;
    private float[] linear_acceleration = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.tracing_title);
        setOnClickListener(this);
        /**
         * （1）实现SensorManageListener接口，设置全局常量值
         * （2）onCreate下获取传感器实例 Sensor_Acc,定义监听器，将监听器注册到Manage种
         * （3）onCreate下注册监听器mSensorManager.registerListener(this, Sensor_Acc, delay)
         * （4）使用全局变量值:OnCustomAttributeListener
         */

        init();
    }

    private void init() {
        initListener();
        /**
         * （1）声明全局变量
         * （2）初始化定位管理器
         * （3）onresum下动态开启GPS模块
         */
        initLocation();
        initWifi();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor_Acc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        /**
         *  .....注册加速度监听器 register sensors............
         *  设置延迟
         */
        int delay;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(TracingActivity.this);
        switch (Integer.parseInt(pref.getString("opcion1", "2"))) {
            case 1:
                delay = SensorManager.SENSOR_DELAY_FASTEST;
                Log.i("OnResume", "Opcion 1: SENSOR_DELAY_FASTEST");
                break;
            case 2:
                delay = SensorManager.SENSOR_DELAY_GAME;
                Log.i("OnResume", "Opcion 1: SENSOR_DELAY_GAME");
                break;
            case 3:
                delay = SensorManager.SENSOR_DELAY_NORMAL;
                Log.i("OnResume", "Opcion 1: SENSOR_DELAY_NORMAL");
                break;
            case 4:
                delay = SensorManager.SENSOR_DELAY_UI;
                Log.i("OnResume", "Opcion 1: SENSOR_DELAY_UI");
                break;
            default:
                delay = SensorManager.SENSOR_DELAY_GAME;
        }
        if (Sensor_Acc != null) {
            mSensorManager.registerListener(this, Sensor_Acc, delay);
        }

        trackApp = (TrackApplication) getApplicationContext();
        viewUtil = new ViewUtil();
        mapUtil = MapUtil.getInstance();
        mapUtil.init((MapView) findViewById(R.id.tracing_mapView));
        mapUtil.setCenter(trackApp);
        startRealTimeLoc(Constants.LOC_INTERVAL);
        powerManager = (PowerManager) trackApp.getSystemService(Context.POWER_SERVICE);

        traceBtn = (Button) findViewById(R.id.btn_trace);
        gatherBtn = (Button) findViewById(R.id.btn_gather);
        getHttp = (Button) findViewById(R.id.getHttp);
        postHttp = (Button) findViewById(R.id.postHttp);

        traceBtn.setOnClickListener(this);
        gatherBtn.setOnClickListener(this);
        getHttp.setOnClickListener(this);
        postHttp.setOnClickListener(this);
        setTraceBtnStyle();
        setGatherBtnStyle();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        /**
         * 设置百度鹰眼用户自定义属性监听器
         */
        trackApp.mClient.setOnCustomAttributeListener(mCustomAttributeListener);


    }


    @Override
    public void onClick(View view) {
        Log.e("res1", "有按钮点击");
        Log.i("res2", String.valueOf(view.getId()));
        switch (view.getId()) {
            // 追踪选项设置
            case R.id.btn_activity_options:
                ViewUtil.startActivityForResult(this, TracingOptionsActivity.class, Constants
                        .REQUEST_CODE);
                break;

            case R.id.btn_trace:
                if (trackApp.isTraceStarted) {
                    trackApp.mClient.stopTrace(trackApp.mTrace, traceListener);
                    stopRealTimeLoc();
                } else {
                    trackApp.mClient.startTrace(trackApp.mTrace, traceListener);
                    if (Constants.DEFAULT_PACK_INTERVAL != packInterval) {
                        stopRealTimeLoc();
                        startRealTimeLoc(packInterval);
                    }
                }
                break;

            case R.id.btn_gather:
                Log.i("res", "btn_gather被点击");
                if (trackApp.isGatherStarted) {
                    trackApp.mClient.stopGather(traceListener);
                } else {
                    trackApp.mClient.startGather(traceListener);
                }
                break;
            /**
             * 执行get请求 Android OKHttp使用详解https://www.jianshu.com/p/2663ce3da0db
             */
            case R.id.getHttp:
                Log.i("res", "get按钮被点击");
                String url = "http://www.baidu.com/";
                GetExample getExample = new GetExample();
                getExample.getDatasync(url);//同步
                getExample.getDatasync(url);//异步
                break;

            case R.id.postHttp:
                /**
                 * 执行post请求
                 */
                Log.i("res", "post按钮被点击");
                PostExample postExample = new PostExample();
                postExample.postDataWithParame();
                Log.i("res", "post按钮结束");
                break;
            default:
                break;
        }

    }


    /**
     * 设置服务按钮样式
     */
    private void setTraceBtnStyle() {
        boolean isTraceStarted = trackApp.trackConf.getBoolean("is_trace_started", false);
        if (isTraceStarted) {
            traceBtn.setText(R.string.stop_trace);
            traceBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color
                    .white, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                traceBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_sure, null));
            } else {
                traceBtn.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_sure, null));
            }
        } else {
            traceBtn.setText(R.string.start_trace);
            traceBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.layout_title, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                traceBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_cancel, null));
            } else {
                traceBtn.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_cancel, null));
            }
        }
    }

    /**
     * 设置采集按钮样式
     */
    private void setGatherBtnStyle() {
        boolean isGatherStarted = trackApp.trackConf.getBoolean("is_gather_started", false);
        if (isGatherStarted) {
            gatherBtn.setText(R.string.stop_gather);
            gatherBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                gatherBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_sure, null));
            } else {
                gatherBtn.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_sure, null));
            }
        } else {
            gatherBtn.setText(R.string.start_gather);
            gatherBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.layout_title, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                gatherBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_cancel, null));
            } else {
                gatherBtn.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_cancel, null));
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//
//        final float alpha = (float) 0.8;
//        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
//            gravity = event.values.clone();
//        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Acc_data = event.values.clone();
//            // Isolate the force of gravity with the low-pass filter.
//            gravity[0] = alpha * gravity[0] + (1 - alpha) * Acc_data[0];
//            gravity[1] = alpha * gravity[1] + (1 - alpha) * Acc_data[1];
//            gravity[2] = alpha * gravity[2] + (1 - alpha) * Acc_data[2];

            // Remove the gravity contribution with the high-pass filter.
//            linear_acceleration[0] = Acc_data[0] ;
//            linear_acceleration[1] = Acc_data[1] ;
//            linear_acceleration[2] = Acc_data[2] ;
//            linear_acceleration[0] = Acc_data[0] - gravity[0];
//            linear_acceleration[1] = Acc_data[1] - gravity[1];
//            linear_acceleration[2] = Acc_data[2] - gravity[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * 实时定位任务
     *
     * @author baidu
     */
    class RealTimeLocRunnable implements Runnable {

        private int interval = 0;

        public RealTimeLocRunnable(int interval) {
            this.interval = interval;
        }

        @Override
        public void run() {
            if (isRealTimeRunning) {
                trackApp.getCurrentLocation(entityListener, trackListener);
                realTimeHandler.postDelayed(this, interval * 1000);

            }
        }
    }

    public void startRealTimeLoc(int interval) {
        isRealTimeRunning = true;
        realTimeLocRunnable = new RealTimeLocRunnable(interval);
        realTimeHandler.post(realTimeLocRunnable);
    }

    public void stopRealTimeLoc() {
        isRealTimeRunning = false;
        if (null != realTimeHandler && null != realTimeLocRunnable) {
            realTimeHandler.removeCallbacks(realTimeLocRunnable);
        }
        trackApp.mClient.stopRealTimeLoc();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null == data) {
            return;
        }

        if (data.hasExtra("locationMode")) {
            LocationMode locationMode = LocationMode.valueOf(data.getStringExtra("locationMode"));
            trackApp.mClient.setLocationMode(locationMode);
        }

        if (data.hasExtra("isNeedObjectStorage")) {
            boolean isNeedObjectStorage = data.getBooleanExtra("isNeedObjectStorage", false);
            trackApp.mTrace.setNeedObjectStorage(isNeedObjectStorage);
        }

        if (data.hasExtra("gatherInterval") || data.hasExtra("packInterval")) {
            int gatherInterval = data.getIntExtra("gatherInterval", Constants.DEFAULT_GATHER_INTERVAL);
            int packInterval = data.getIntExtra("packInterval", Constants.DEFAULT_PACK_INTERVAL);
            TracingActivity.this.packInterval = packInterval;
            trackApp.mClient.setInterval(gatherInterval, packInterval);
        }

        //        if (data.hasExtra("supplementMode")) {
        //            mSupplementMode = SupplementMode.valueOf(data.getStringExtra("supplementMode"));
        //        }
    }

    /**
     * 初始化WIFI管理
     */
    private void initWifi() {
        wifimanager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //wifimanager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifimanager != null) {
            if (wifimanager.isWifiEnabled()==false)  // 检测到WiFi关闭
            {
                Log.i("Wifi","检测到WiFi关闭");
                if (wifimanager.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {  // si no est� ya en proceso de encendido => mandar encender
                    wifimanager.setWifiEnabled(true);  // mandar encender WiFi
                }
            } else {
                Log.i("Wifi","检测到WiFi开启");
            }
        }
        wifibroadcastreceiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiInfo wifiinfo = wifimanager.getConnectionInfo();
                if (wifiinfo.getBSSID() != null) {
//                    String cadena_display=String.format(Locale.US,"\tConnected to: %s\n\tBSSID: %s\n\tRSSI: %d dBm \n\tLinkSpeed: %d Mbps\n\t\t\t\t\t\t\t\tFreq: %5.1f Hz",wifiinfo.getSSID(),wifiinfo.getBSSID(),wifiinfo.getRssi(),wifiinfo.getLinkSpeed());
//                    Log.i("wifi", cadena_display);
                    SSID = wifiinfo.getSSID();
                    WifiTag = 1;
                } else {
                    WifiTag = 0;
                    SSID = "None";
                    Log.i("wifi", "No connection detected");
                }
            }
        };
        if (wifimanager!=null)
        {
            registerReceiver(wifibroadcastreceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)  );
        }
    }
    /**
     * 初始化定位管理
     */
    @SuppressLint("MissingPermission") // 压制GPS未开启的警告
    private void initLocation() {
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //todo 未开启GPS定位模块则跳转到GPS设置界面；目前的问题是，未开启GOS无法进入GPS状态改变算法，而locManager.getGpsStatus(null);又需要监测状态
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(TracingActivity.this, "请开启GPS导航", Toast.LENGTH_SHORT).show();
            //返回开启GPS导航设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }
        //添加卫星状态改变监听
        gpsStatusListener = new GpsStatus.Listener() {
            //卫星状态改变
            @Override
            public void onGpsStatusChanged(int event) {
                switch (event) {
                    //判断GPS是否正常启动
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        //获取当前状态
                        GpsStatus gpsStatus = locManager.getGpsStatus(null);
                        //获取卫星颗数的默认最大值
                        int maxSatellites = gpsStatus.getMaxSatellites();
                        //获取所有的卫星
                        Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                        //卫星颗数统计
                        SatellitesCount= 0;
                        StringBuilder sb = new StringBuilder();
                        while (iters.hasNext() && SatellitesCount <= maxSatellites) {
                            SatellitesCount++;
                            GpsSatellite s = iters.next();
                            //卫星的信噪比
                            float snr = s.getSnr();
                            sb.append("第").append(SatellitesCount).append("颗").append("：").append(snr).append("\n");
                        }
                        Log.e("TAG", sb.toString());
                        break;
                    default:
                        break;
                }
            }
        };
        if (gpsStatusListener != null){
            locManager.addGpsStatusListener(gpsStatusListener);
        }

    }

    private void initListener() {

        trackListener = new OnTrackListener() {

            @Override
            public void onLatestPointCallback(LatestPointResponse response) {
                if (StatusCodes.SUCCESS != response.getStatus()) {
                    return;
                }

                LatestPoint point = response.getLatestPoint();
                if (null == point || CommonUtil.isZeroPoint(point.getLocation().getLatitude(), point.getLocation()
                        .getLongitude())) {
                    return;
                }

                LatLng currentLatLng = mapUtil.convertTrace2Map(point.getLocation());
                if (null == currentLatLng) {
                    return;
                }
                CurrentLocation.locTime = point.getLocTime();
                CurrentLocation.latitude = currentLatLng.latitude;
                CurrentLocation.longitude = currentLatLng.longitude;

                if (null != mapUtil) {
                    mapUtil.updateStatus(currentLatLng, true);
                }
            }
        };

        entityListener = new OnEntityListener() {

            @Override
            public void onReceiveLocation(TraceLocation location) {

                if (StatusCodes.SUCCESS != location.getStatus() || CommonUtil.isZeroPoint(location.getLatitude(),
                        location.getLongitude())) {
                    return;
                }
                LatLng currentLatLng = mapUtil.convertTraceLocation2Map(location);
                if (null == currentLatLng) {
                    return;
                }
                CurrentLocation.locTime = CommonUtil.toTimeStamp(location.getTime());
                CurrentLocation.latitude = currentLatLng.latitude;
                CurrentLocation.longitude = currentLatLng.longitude;

                if (null != mapUtil) {
                    mapUtil.updateStatus(currentLatLng, true);
                }
            }

        };

        traceListener = new OnTraceListener() {

            /**
             * 绑定服务回调接口
             * @param errorNo  状态码
             * @param message 消息
             *                <p>
             *                <pre>0：成功 </pre>
             *                <pre>1：失败</pre>
             */
            @Override
            public void onBindServiceCallback(int errorNo, String message) {
                viewUtil.showToast(TracingActivity.this,
                        String.format("onBindServiceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * 开启服务回调接口
             * @param errorNo 状态码
             * @param message 消息
             *                <p>
             *                <pre>0：成功 </pre>
             *                <pre>10000：请求发送失败</pre>
             *                <pre>10001：服务开启失败</pre>
             *                <pre>10002：参数错误</pre>
             *                <pre>10003：网络连接失败</pre>
             *                <pre>10004：网络未开启</pre>
             *                <pre>10005：服务正在开启</pre>
             *                <pre>10006：服务已开启</pre>
             */
            @Override
            public void onStartTraceCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.START_TRACE_NETWORK_CONNECT_FAILED <= errorNo) {
                    trackApp.isTraceStarted = true;
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.putBoolean("is_trace_started", true);
                    editor.apply();
                    setTraceBtnStyle();
                    registerReceiver();
                }
                viewUtil.showToast(TracingActivity.this,
                        String.format("onStartTraceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * 停止服务回调接口
             * @param errorNo 状态码
             * @param message 消息
             *                <p>
             *                <pre>0：成功</pre>
             *                <pre>11000：请求发送失败</pre>
             *                <pre>11001：服务停止失败</pre>
             *                <pre>11002：服务未开启</pre>
             *                <pre>11003：服务正在停止</pre>
             */
            @Override
            public void onStopTraceCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.CACHE_TRACK_NOT_UPLOAD == errorNo) {
                    trackApp.isTraceStarted = false;
                    trackApp.isGatherStarted = false;
                    // 停止成功后，直接移除is_trace_started记录（便于区分用户没有停止服务，直接杀死进程的情况）
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.remove("is_trace_started");
                    editor.remove("is_gather_started");
                    editor.apply();
                    setTraceBtnStyle();
                    setGatherBtnStyle();
                    unregisterPowerReceiver();
                }
                viewUtil.showToast(TracingActivity.this,
                        String.format("onStopTraceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * 开启采集回调接口
             * @param errorNo 状态码
             * @param message 消息
             *                <p>
             *                <pre>0：成功</pre>
             *                <pre>12000：请求发送失败</pre>
             *                <pre>12001：采集开启失败</pre>
             *                <pre>12002：服务未开启</pre>
             */
            @Override
            public void onStartGatherCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.GATHER_STARTED == errorNo) {
                    trackApp.isGatherStarted = true;
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.putBoolean("is_gather_started", true);
                    editor.apply();
                    setGatherBtnStyle();
                }
                viewUtil.showToast(TracingActivity.this,
                        String.format("onStartGatherCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * 停止采集回调接口
             * @param errorNo 状态码
             * @param message 消息
             *                <p>
             *                <pre>0：成功</pre>
             *                <pre>13000：请求发送失败</pre>
             *                <pre>13001：采集停止失败</pre>
             *                <pre>13002：服务未开启</pre>
             */
            @Override
            public void onStopGatherCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.GATHER_STOPPED == errorNo) {
                    trackApp.isGatherStarted = false;
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.remove("is_gather_started");
                    editor.apply();
                    setGatherBtnStyle();
                }
                viewUtil.showToast(TracingActivity.this,
                        String.format("onStopGatherCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * 推送消息回调接口
             *
             * @param messageType 状态码
             * @param pushMessage 消息
             *                  <p>
             *                  <pre>0x01：配置下发</pre>
             *                  <pre>0x02：语音消息</pre>
             *                  <pre>0x03：服务端围栏报警消息</pre>
             *                  <pre>0x04：本地围栏报警消息</pre>
             *                  <pre>0x05~0x40：系统预留</pre>
             *                  <pre>0x41~0xFF：开发者自定义</pre>
             */
            @Override
            public void onPushCallback(byte messageType, PushMessage pushMessage) {
                if (messageType < 0x03 || messageType > 0x04) {
                    viewUtil.showToast(TracingActivity.this, pushMessage.getMessage());
                    return;
                }
                FenceAlarmPushInfo alarmPushInfo = pushMessage.getFenceAlarmPushInfo();
                if (null == alarmPushInfo) {
                    viewUtil.showToast(TracingActivity.this,
                            String.format("onPushCallback, messageType:%d, messageContent:%s ", messageType,
                                    pushMessage));
                    return;
                }
                StringBuffer alarmInfo = new StringBuffer();
                alarmInfo.append("您于")
                        .append(CommonUtil.getHMS(alarmPushInfo.getCurrentPoint().getLocTime() * 1000))
                        .append(alarmPushInfo.getMonitoredAction() == MonitoredAction.enter ? "进入" : "离开")
                        .append(messageType == 0x03 ? "云端" : "本地")
                        .append("围栏：").append(alarmPushInfo.getFenceName());

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    Notification notification = new Notification.Builder(trackApp)
                            .setContentTitle(getResources().getString(R.string.alarm_push_title))
                            .setContentText(alarmInfo.toString())
                            .setSmallIcon(R.mipmap.icon_app)
                            .setWhen(System.currentTimeMillis()).build();
                    notificationManager.notify(notifyId++, notification);
                }
            }

            @Override
            public void onInitBOSCallback(int errorNo, String message) {
                viewUtil.showToast(TracingActivity.this,
                        String.format("onInitBOSCallback, errorNo:%d, message:%s ", errorNo, message));
            }
        };

        mCustomAttributeListener = new OnCustomAttributeListener() {
            //        onResume方法需要注册传感器
            @Override
            public Map<String, String> onTrackAttributeCallback() {
                /**
                 * 需要上传的数据
                 * （1）加速度计
                 * （2）卫星数量
                 * （3）wifi连接
                 */
                Map<String, String> trackAttrs = new HashMap<String, String>();
//                trackAttrs.put("strAcceX", "1");
//                trackAttrs.put("strAcceY", "2");
//                trackAttrs.put("strAcceZ", "3");
//                trackAttrs.put("satCount", "4");
//                trackAttrs.put("wifiTag", "5");
//                trackAttrs.put("SSID", "6");
                trackAttrs.put("strAcceX", String.valueOf(Acc_data[0]));
                trackAttrs.put("strAcceY", String.valueOf(Acc_data[1]));
                trackAttrs.put("strAcceZ", String.valueOf(Acc_data[2]));
                trackAttrs.put("satCount", String.valueOf(SatellitesCount));
                trackAttrs.put("wifiTag", String.valueOf(WifiTag));
                trackAttrs.put("SSID", SSID);
                return trackAttrs;
            }

            @Override
            public Map<String, String> onTrackAttributeCallback(long l) {
                return null;
            }
        };
    }

    static class RealTimeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    /**
     * 注册广播（电源锁、GPS状态）
     */
    @SuppressLint("InvalidWakeLockTag")
    private void registerReceiver() {
        if (trackApp.isRegisterReceiver) {
            return;
        }

        if (null == wakeLock) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "track upload");
        }
        if (null == trackReceiver) {
            trackReceiver = new TrackReceiver(wakeLock);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(StatusCodes.GPS_STATUS_ACTION);
        trackApp.registerReceiver(trackReceiver, filter);
        trackApp.isRegisterReceiver = true;

    }

    private void unregisterPowerReceiver() {
        if (!trackApp.isRegisterReceiver) {
            return;
        }
        if (null != trackReceiver) {
            trackApp.unregisterReceiver(trackReceiver);
        }
        trackApp.isRegisterReceiver = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        startRealTimeLoc(packInterval);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapUtil.onResume();

        // 在Android 6.0及以上系统，若定制手机使用到doze模式，请求将应用添加到白名单。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = trackApp.getPackageName();
            boolean isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName);
            if (!isIgnoring) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                try {
                    startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapUtil.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRealTimeLoc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapUtil.clear();
        stopRealTimeLoc();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_tracing;
    }

}
