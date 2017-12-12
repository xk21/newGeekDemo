package com.sharedream.geek.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sharedream.geek.sdk.GeekCallback;
import com.sharedream.geek.sdk.GeekSdk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final int PERMISSION_REQUEST_CODE = 0X01;
    private static final String PACK_NAME = "IVVI 注册结果 : ";
    private LinearLayout llOneScreen;
    private TextView tvPackName;
    private GeekView geekView;
    private WifiManager wifiManager;
    private long exitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        MyUtils.saveField(getApplicationContext(), "sd34405156835", 289);
        if (checkAndRequestPermissions()) {
            initView();
            initData();
        }
    }

    /**
     * 检查并获取权限
     *
     * @return true 已获取全部权限； false 部分权限未获取
     */
    private boolean checkAndRequestPermissions() {
        List<String> permissionList = checkPermissions();
        if (permissionList.size() == 0) {
            return true;
        }

        showPermissionDialog(permissionList);

        return false;
    }

    /**
     * 检查权限获取情况
     *
     * @return
     */
    private ArrayList<String> checkPermissions() {
        ArrayList<String> requestPermissionList = new ArrayList<>();
        boolean phoneStateCheckResult = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        if (!phoneStateCheckResult) {
            requestPermissionList.add(Manifest.permission.READ_PHONE_STATE);
        }

        boolean storageCheckResult = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!storageCheckResult) {
            requestPermissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        boolean locationCheckResult = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!locationCheckResult) {
            requestPermissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        return requestPermissionList;
    }

    /**
     * 请求权限
     *
     * @param permissionList
     */
    private void showPermissionDialog(List<String> permissionList) {
        String[] requestPermissions = new String[permissionList.size()];
        permissionList.toArray(requestPermissions);
        ActivityCompat.requestPermissions(this, requestPermissions, PERMISSION_REQUEST_CODE);
    }

    private void initData() {
        LocationModule.getInstance().startLocation(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GeekSdk.release();
        LocationModule.getInstance().destroy();
        SceneManager.getInstance().release();
        SceneResultSubject.getInstance().release();
        geekView = null;
    }

    private void initView() {
        if (geekView == null) {
            geekView = new GeekView(getApplicationContext());
        }
        llOneScreen = (LinearLayout) findViewById(R.id.ll_one_screen);
        tvPackName = (TextView) findViewById(R.id.tv_packName);
        tvPackName.setText(PACK_NAME);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        findViewById(R.id.btn_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "register", Toast.LENGTH_SHORT).show();
                initGeekSdk();
            }
        });

        findViewById(R.id.btn_release).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "release", Toast.LENGTH_SHORT).show();
                llOneScreen.removeView(geekView);
                GeekSdk.release();
                tvPackName.setText(PACK_NAME);
            }
        });

        findViewById(R.id.btn_scan_wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    boolean scanAlwaysAvailable = wifiManager.isScanAlwaysAvailable();
                    wifiManager.startScan();
                    Toast.makeText(getApplicationContext(), "scanWifi , scanAlwaysAvailable : " + scanAlwaysAvailable, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btn_upload_log).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeekSdk.uploadLog();
            }
        });
    }

    private void initGeekSdk() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Context", getApplicationContext());
            jsonObject.put("Uid", "123456789");
            GeekSdk.init(jsonObject, new GeekCallback() {
                @Override
                public void onCallback(int code, JSONObject jsonObject) {
                    switch (code) {
                        case EVENT_SDK_INIT:
                            int result = jsonObject.optInt("result");
                            if (result == INIT_RESULT_SUCCESS) {
                                GeekSdk.startScanScene();
                                Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();
                                tvPackName.setText(PACK_NAME + "成功");
                            } else {
                                Toast.makeText(getApplicationContext(), "注册失败 : " + result, Toast.LENGTH_SHORT).show();
                                tvPackName.setText(PACK_NAME + "失败");
                            }
                            break;

                        case EVENT_SDK_RESIDENT_SHOP:
                            Log.i("EVENT_SDK_RESIDENT_SHOP", "驻店回调 ");
                            int enterResult = jsonObject.optInt("result");
                            JSONArray data = jsonObject.optJSONArray("data");
                            if (enterResult == GET_SCENE_RESULT_SUCCESS) {
                                JSONObject jsonSceneData = data.optJSONObject(0);
                                Log.i("EVENT_SDK_RESIDENT_SHOP", "返回场景 : " + jsonSceneData);
                                SceneManager.getInstance().addSceneData(jsonSceneData);
//                                Toast.makeText(getApplicationContext(), "成功获取场景数据 : " + jsonSceneData, Toast.LENGTH_SHORT).show();
                                SceneResultSubject.getInstance().notifySceneResultFound(jsonSceneData);
                                int childCount = llOneScreen.getChildCount();
                                if (childCount == 3) {
                                    llOneScreen.addView(geekView, childCount - 1);
                                }
                            }
                            break;

                        case EVENT_SDK_LEAVE_SHOP:
                            Log.i("EVENT_SDK_LEAVE_SHOP", "离店回调 ");
                            if (jsonObject != null) {
                                int leaveResult = jsonObject.optInt("result");
                                if (leaveResult == 1) {
                                    JSONArray leaveShopJsonArray = jsonObject.optJSONArray("data");
                                    SceneResultSubject.getInstance().notifySceneResultNotFound(leaveShopJsonArray);
                                }
                            }
                            break;

                        case EVENT_SDK_QUERY_NEARBY_POI_URL:
                            if (jsonObject != null) {
                                int initResult = jsonObject.optInt("result");
                                if (initResult == 1) {
                                    JSONObject uriJSON = jsonObject.optJSONObject("data");
                                    if (uriJSON != null) {
                                        String url = uriJSON.optString("url");
                                        if (!TextUtils.isEmpty(url)) {
                                            SceneManager.getInstance().setNearSceneUrl(url);
                                            Intent intent = new Intent(MainActivity.this, SceneServiceActivity.class);
                                            intent.putExtra("actionUrl", url);
                                            intent.putExtra("name", "附近场景");
//                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    }
                                }
                            }
                            break;

                        case EVENT_SDK_UPLOAD_LOG:
                            if (jsonObject != null) {
                                int initResult = jsonObject.optInt("result");
                                if (initResult == 1) {
                                    Toast.makeText(getApplicationContext(), "反馈成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "反馈失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    checkAndRequestPermissions();
                    return;
                }
            }

            // 在全部权限获取后，初始化SDK
            initView();
            initData();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
        return false;
    }
}
