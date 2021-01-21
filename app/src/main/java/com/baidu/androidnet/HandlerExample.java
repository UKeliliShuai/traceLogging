package com.baidu.androidnet;


import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * @project: BaiduTrace_AndroidSDK_v3_1_7_Sample
 * @author: UKelili
 * @date: 2021/1/15 21
 */
public class HandlerExample extends Handler {
    private View viewIdx = null;
    HandlerExample(){
        this(null,null);
    }
    HandlerExample(Message msg, View viewIdx){

        this.viewIdx = viewIdx;
    }
    @Override
    public void handleMessage(@NonNull Message msg) {
//        super.handleMessage(msg);
        switch (msg.what) {
            case 0x001:
//                hideAllWidget();
//                imgPic.setVisibility(View.VISIBLE);
//                imgPic.setImageBitmap(bitmap);
//                Toast.makeText(MainActivity.this, "图片加载完毕", Toast.LENGTH_SHORT).show();
//                break;
                break;
            case 0x002:
//                mTextView.setText("执行了线程2的UI操作");
                break;
        }
    }
    // 用于刷新界面
//    private Handler handler = new Handler() {
//        public void handleMessage(android.os.Message msg) {
//            switch (msg.what) {
//                case 0x001:
//                    hideAllWidget();
//                    imgPic.setVisibility(View.VISIBLE);
//                    imgPic.setImageBitmap(bitmap);
//                    Toast.makeText(MainActivity.this, "图片加载完毕", Toast.LENGTH_SHORT).show();
//                    break;
//                case 0x002:
//                    hideAllWidget();
//                    scroll.setVisibility(View.VISIBLE);
//                    txtshow.setText(detail);
//                    Toast.makeText(MainActivity.this, "HTML代码加载完毕", Toast.LENGTH_SHORT).show();
//                    break;
//                case 0x003:
//                    hideAllWidget();
//                    webView.setVisibility(View.VISIBLE);
//                    webView.loadDataWithBaseURL("", detail, "text/html", "UTF-8", "");
//                    Toast.makeText(MainActivity.this, "网页加载完毕", Toast.LENGTH_SHORT).show();
//                    break;
//                default:
//                    break;
//            }
//        }
//    };
}
