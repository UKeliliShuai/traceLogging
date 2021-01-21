package com.baidu.androidnet;

import android.util.Log;

import com.baidu.POJO.ChildExample;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @project: BaiduTrace_AndroidSDK_v3_1_7_Sample
 * @author: UKelili
 * @date: 2021/1/15 21
 */
public class PostExample {
    public void postDataWithParame() {
        final String url = "http://33r548p594.qicp.vip:24010/addfun/";
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("first_name","zhangsan");//传递键值对参数
        formBody.add("last_name","lisi");
        formBody.add("salary","20000");
        Request request = new Request.Builder()//创建Request 对象。
                .url(url)
                .post(formBody.build())//传递请求体
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("post", "onFailure: 失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("post", "onResponse: 成功");
            }
        });//此处省略回调方法。
    }
    public void psotDataWithJson(){
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        Gson gson = new Gson();
        ChildExample childExample = new ChildExample();
        String toJson = gson.toJson(childExample);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，
//        String jsonStr = "{\"username\":\"lisi\",\"nickname\":\"李四\"}";//json数据.
        RequestBody body = RequestBody.create(JSON, toJson);
        Request request = new Request.Builder()
                .url("http://www.baidu.com")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }
}
