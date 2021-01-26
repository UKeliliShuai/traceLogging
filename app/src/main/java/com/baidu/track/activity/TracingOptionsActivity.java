package com.baidu.track.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.baidu.trace.model.LocationMode;
import com.baidu.track.R;
import com.baidu.track.TrackApplication;
import com.baidu.track.utils.Constants;

import static com.baidu.trace.model.LocationMode.High_Accuracy;

public class TracingOptionsActivity extends BaseActivity {
    private TrackApplication trackApp = null;
    // 返回结果
    private Intent result = null;
    private RadioGroup vehicleModeGroup = null;
    private RadioButton vehicleModeRadio = null;
    private RadioGroup commuteModeGroup = null;
    private RadioButton commuteModeRadio = null;
    private int vehicleModeID = -1;
    private int commuteModeID = -1;



//    private EditText gatherIntervalText = null;
//    private EditText packIntervalText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.tracing_options_title);
        setOptionsButtonInVisible();
        init();
    }

    private void init() {
        trackApp = (TrackApplication) getApplicationContext();

        vehicleModeID = trackApp.trackConf.getInt("vehicleModeID", R.id.vehicle6);
        commuteModeID = trackApp.trackConf.getInt("commuteModeID", R.id.commute3);
        vehicleModeGroup = (RadioGroup) findViewById(R.id.vehicle_mode);
        commuteModeGroup = (RadioGroup) findViewById(R.id.commute_mode);

        vehicleModeGroup.check(vehicleModeID);
        commuteModeGroup.check(commuteModeID);
    }

    public void onCancel(View v) {
        super.onBackPressed();
    }
    //onClick属性添加点击事件的方式
    public void onFinish(View v) {
        result = new Intent();

        vehicleModeRadio = (RadioButton) findViewById(vehicleModeGroup.getCheckedRadioButtonId());
        int vehicleMode = -1;
        switch (vehicleModeRadio.getId()) {
            case R.id.vehicle0:
                vehicleMode = 0;
                vehicleModeID = R.id.vehicle0;
                break;
            case R.id.vehicle1:
                vehicleMode = 1;
                vehicleModeID = R.id.vehicle1;
                break;
            case R.id.vehicle2:
                vehicleMode = 2;
                vehicleModeID = R.id.vehicle2;
                break;
            case R.id.vehicle3:
                vehicleMode = 3;
                vehicleModeID = R.id.vehicle3;
                break;
            case R.id.vehicle4:
                vehicleMode = 4;
                vehicleModeID = R.id.vehicle4;
                break;
            case R.id.vehicle5:
                vehicleMode = 5;
                vehicleModeID = R.id.vehicle5;
                break;
            case R.id.vehicle6:
                vehicleMode = 6;
                vehicleModeID = R.id.vehicle6;
                break;
            default:
                break;
        }
        result.putExtra("vehicleMode", vehicleMode);


        commuteModeRadio = (RadioButton) findViewById(commuteModeGroup.getCheckedRadioButtonId());
        int commuteMode = -1;
        switch (commuteModeRadio.getId()) {
            case R.id.commute0:
                commuteMode = 0;
                commuteModeID = R.id.commute0;
                break;
            case R.id.commute1:
                commuteMode = 1;
                commuteModeID = R.id.commute1;
                break;
            case R.id.commute2:
                commuteMode = 2;
                commuteModeID = R.id.commute2;
                break;
            case R.id.commute3:
                commuteMode = 3;
                commuteModeID = R.id.commute3;
                break;
            case R.id.commute4:
                commuteMode = 4;
                commuteModeID = R.id.commute4;
                break;
            case R.id.commute5:
                commuteMode = 5;
                commuteModeID = R.id.commute5;
                break;
            case R.id.commute6:
                commuteMode = 6;
                commuteModeID = R.id.commute6;
                break;
            default:
                break;
        }
        result.putExtra("commuteMode", commuteMode);

        SharedPreferences.Editor editor = trackApp.trackConf.edit();
        editor.putInt("vehicleModeID", vehicleModeID);
        editor.putInt("commuteModeID", commuteModeID);
        editor.apply();
        setResult(Constants.RESULT_CODE, result);
        super.onBackPressed();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_tracing_options;
    }

}
