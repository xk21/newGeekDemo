package com.sharedream.geek.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.List;

public class SceneServiceActivity extends Activity {
    private WebView wvSceneService;
    private String actionUrl;
    private String titleName;
    private FrameLayout frameLayout;
    private TextView tvTitleName;
    private WebViewProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_webview);
        initView();
        initData();
    }

    private void initData() {
        actionUrl = getIntent().getStringExtra("actionUrl");
        titleName = getIntent().getStringExtra("name");
        tvTitleName.setText(titleName);
//        actionUrl = "http://lapp.m.mallcoo.cn/shop/map_p0?_mid=238#p=map_p1&mode=shop&fid=0";
        wvSceneService.addJavascriptInterface(new CallBackForJs(new JsHandler(this)), "mx");

        WebSettings webSettings = wvSceneService.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
//        webSettings.setDomStorageEnabled(true);

        // 获取地理位置权限
        webSettings.setGeolocationEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationDatabasePath(getFilesDir().getPath());
        // 设置页面自适应webview
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        // 从Lollipop(21)开始WebView默认不允许混合模式，https当中不能加载http资源，需要设置开启
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        wvSceneService.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null) {
                    int colonIndex = url.indexOf(":");
                    if (colonIndex != -1) {
                        String tempUrl = url.toLowerCase();
                        if (tempUrl.contains("%3d") || tempUrl.contains("%3f") || tempUrl.contains("%2f")) {
                            try {
                                url = URLDecoder.decode(url, "UTF-8");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (url.startsWith("mailto:")
                                || url.startsWith("geo:")
                                || url.startsWith("tel:")
                                || url.startsWith("weixin:")
                                || url.startsWith("alipay:")
                                || url.startsWith("alipays:")
                                || (url.contains("intent:") && url.contains("alipay"))
                                || url.endsWith(".apk")
                                || url.startsWith("http://imtt.dd.qq.com/") && url.contains(".apk")) { // 兼容应用宝下载跳转
                            launchOtherApp(url);
                        } else if (url.startsWith("http:") || url.startsWith("https:")) {
                            return false;
                        }
                    }
                }

                return true; // True if the host application wants to leave the current WebView and handle the url itself, otherwise return false. If return false, it will load url automatically
            }

            @Override
            public void onFormResubmission(WebView view, Message dontResend, Message resend) {
                super.onFormResubmission(view, dontResend, resend);
                resend.sendToTarget();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!TextUtils.isEmpty(url)) {
//                    filterUiElement(view, url);
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.start();
            }



            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                if (!TextUtils.isEmpty(url)) {
//                    filterUiElement(view, url);
                }
            }
        });


        wvSceneService.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsHidePrompt() {
                super.onGeolocationPermissionsHidePrompt();
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
                int startPos = actionUrl.indexOf("//");
                startPos = startPos == -1 ? 0 : startPos + 2;
                if (actionUrl.startsWith("m.dianping.com", startPos)) {
                    callback.invoke(origin, false, false);
                } else {
                    showDialog(origin, callback);
                }
            }
        });

        wvSceneService.loadUrl(actionUrl);
    }

    private void showDialog(final String origin, final GeolocationPermissions.Callback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("允许获取定位信息?");
        DialogInterface.OnClickListener dialogButtonOnClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int clickedButton) {
                if (DialogInterface.BUTTON_POSITIVE == clickedButton) {
                    callback.invoke(origin, true, true);
                } else if (DialogInterface.BUTTON_NEGATIVE == clickedButton) {
                    callback.invoke(origin, false, false);
                }
            }
        };
        builder.setPositiveButton("允许", dialogButtonOnClickListener);
        builder.setNegativeButton("忽略", dialogButtonOnClickListener);
        builder.show();
    }

    private void launchOtherApp(String url) {
        try {
            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            intent.addCategory("android.intent.category.BROWSABLE");
            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfoList != null && resolveInfoList.size() > 0) {
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initView() {
        frameLayout = (FrameLayout) findViewById(R.id.fl_container);
        tvTitleName = (TextView) findViewById(R.id.tv_title);

        wvSceneService = new WebView(getApplicationContext());
        wvSceneService.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        wvSceneService.setBackgroundColor(getResources().getColor(R.color.geek_sdk_background));
        wvSceneService.setHorizontalScrollBarEnabled(false);
        wvSceneService.setVerticalScrollBarEnabled(false);
        frameLayout.addView(wvSceneService);

        progressBar = new WebViewProgressBar(this);
        progressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelOffset(R.dimen.dp_4)));
        frameLayout.addView(progressBar);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            actionUrl = getIntent().getStringExtra("actionUrl");
            wvSceneService.loadUrl(actionUrl);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (wvSceneService.canGoBack()) {
                    wvSceneService.goBack();
                } else {
                    finish();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wvSceneService != null) {
            wvSceneService.removeJavascriptInterface("mx");
            wvSceneService.getSettings().setJavaScriptEnabled(false);
            wvSceneService.destroy();
            wvSceneService = null;
        }
    }

    private static class JsHandler extends Handler {
        private final WeakReference<SceneServiceActivity> activity;

        public JsHandler(SceneServiceActivity sceneServiceActivity) {
            this.activity = new WeakReference<>(sceneServiceActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            SceneServiceActivity sceneServiceActivity = activity.get();
            switch (msg.what) {
                case Constant.JS_EVENT_INIT:
                    String footprint = MyUtils.getFootprint(sceneServiceActivity, "footprint");
                    try {
                        JSONArray jsonArray = new JSONArray(footprint);
                        if (jsonArray != null && jsonArray.length() > 0) {
                            JSONArray jsonArrayAfterSort = handleSortFootprintData(jsonArray);
                            sceneServiceActivity.wvSceneService.loadUrl("javascript:getSceneList(" + jsonArrayAfterSort + ")");
                        } else {
                            sceneServiceActivity.wvSceneService.loadUrl("javascript:getSceneList()");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

        private JSONArray handleSortFootprintData(JSONArray jsonArray) {
            JSONArray jsonArrayAfterSort = new JSONArray();
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                int index = length - i - 1;
                JSONObject jsonObject = jsonArray.optJSONObject(index);
                jsonArrayAfterSort.put(jsonObject);
            }
            return jsonArrayAfterSort;
        }
    }
}
