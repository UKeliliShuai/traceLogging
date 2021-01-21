package com.baidu.androidnet;

import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * @project: BaiduTrace_AndroidSDK_v3_1_7_Sample
 * @author: UKelili
 * @date: 2021/1/15 17
 *  首先，参考：Android OKHttp使用详解 https://www.jianshu.com/p/2663ce3da0db
 *  其次，参考：解决android 9.0之后必须用https的网址：https://blog.csdn.net/it_mr_lu/article/details/89022807
 *  再次，参考：Gson的详细使用（android必备，快速提高开发效率 https://blog.csdn.net/oQiHaoGongYuan/article/details/50944755
 */
public class GetExample {
    /**
     * 同步Get请求
     */
    public void getDatasync(final String url){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
                    Request request = new Request.Builder()
                            .url(url)//请求接口。如果需要传参拼接到接口后面。
                            .build();//创建Request 对象
                    Response response = null;
                    response = client.newCall(request).execute();//得到Response 对象
                    if (response.isSuccessful()) {
                        Log.d("kwwl","response.code()=="+response.code());
                        Log.d("kwwl","response.message()=="+response.message());
                        Log.d("kwwl","res=="+response.body().string());
                        //此时的代码执行在子线程，修改UI的操作请使用handler跳转到UI线程。
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /**
     * 异步Get请求
     */
    private void getDataAsync(final String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("kwwl","获取数据失败！！！");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){//回调的方法执行在子线程。
                    Log.d("kwwl","获取数据成功了");
                    Log.d("kwwl","response.code()=="+response.code());
                    Log.d("kwwl","response.body().string()=="+response.body().string());
                }
            }
        });
    }
}
