package com.sharedream.geek.app;

import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Created by young on 2016/4/29.
 */
public class CallBackForJs {
    private Handler handler;

    public CallBackForJs(Handler handler) {
        this.handler = handler;
    }

    @JavascriptInterface
    public void init() {
        if (BuildConfig.DEBUG) {
            Log.i("CallBackForJs", "CallBackForJs.init()  #  init");
        }

        if (handler != null) {
            handler.sendEmptyMessage(Constant.JS_EVENT_INIT);
        }
    }

//    @JavascriptInterface
//    public void back() {
//        if (BuildConfig.DEBUG) {
//            Log.i("CallBackForJs", "CallBackForJs.back()  #  back");
//        }
//
//        if (handler != null) {
//            handler.sendEmptyMessage(Constant.JS_EVENT_BACK);
//        }
//    }
//
//    @JavascriptInterface
//    public void close() {
//        if (BuildConfig.DEBUG) {
//            Log.i("CallBackForJs", "CallBackForJs.close()  #  close");
//        }
//
//        if (handler != null) {
//            handler.sendEmptyMessage(Constant.JS_EVENT_CLOSE);
//        }
//    }
}
