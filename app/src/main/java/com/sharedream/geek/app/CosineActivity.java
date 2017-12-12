package com.sharedream.geek.app;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CosineActivity extends AppCompatActivity {
    List<ScanResult> scanResultListOne;
    List<ScanResult> scanResultListTwo;
    TextView tvSimilarity;
    TextView tvMatrixes1;
    TextView tvMatrixes2;
    Button btnOne;
    Button btmTwo;
    Button btnJiSuan;
    private DecimalFormat df = new DecimalFormat("0.00000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cosine);
        final WifiManager wifiManager = (WifiManager) getApplicationContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        tvSimilarity = (TextView) findViewById(R.id.tv_xiangsidu);
        tvMatrixes1 = (TextView) findViewById(R.id.tv_matrixes1);
        tvMatrixes2 = (TextView) findViewById(R.id.tv_matrixes2);
        btnOne = (Button) findViewById(R.id.btn_one);
        btmTwo = (Button) findViewById(R.id.btn_two);
        btnJiSuan = (Button) findViewById(R.id.btn_jisuan);
        btnOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOne.setText("获取成功");
                scanResultListOne = wifiManager.getScanResults();
                Toast.makeText(getApplicationContext(), "wifi个数 : " + scanResultListOne.size(), Toast.LENGTH_SHORT).show();
            }
        });

        btmTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btmTwo.setText("获取成功");
                scanResultListTwo = wifiManager.getScanResults();
                Toast.makeText(getApplicationContext(), "wifi个数 : " + scanResultListTwo.size(), Toast.LENGTH_SHORT).show();
            }
        });

        btnJiSuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOne.setText("获取第一个样本");
                btmTwo.setText("获取第二个样本");
                Map<String, Integer> oneMap = new LinkedHashMap<>();
                Map<String, Integer> twoMap = new LinkedHashMap<>();
                List<ScanResult> newScanResultList = new ArrayList<>();

                if (scanResultListOne != null && scanResultListOne.size() > 0) {
                    for (ScanResult scanResult : scanResultListOne) {
                        if (!newScanResultList.contains(scanResult)) {
                            newScanResultList.add(scanResult);
                        }
                    }
                }

                if (scanResultListTwo != null && scanResultListTwo.size() > 0) {
                    for (ScanResult scanResult : scanResultListTwo) {
                        String ssid = scanResult.SSID;
                        String bssid = scanResult.BSSID;

                        boolean isIntersect = false;
                        for (ScanResult result : newScanResultList) {
                            String ssid1 = result.SSID;
                            String bssid1 = result.BSSID;
                            if (ssid.equals(ssid1) && bssid.equals(bssid1)) {
                                isIntersect = true;
                                break;
                            }
                        }

                        if (!isIntersect) {
                            newScanResultList.add(scanResult);
                        }
                    }
                }

                for (ScanResult scanResult : newScanResultList) {
                    String bssid = scanResult.BSSID;
//                    int level = scanResult.level;
                    oneMap.put(bssid, -65);
                    twoMap.put(bssid, -65);
                }

                if (scanResultListOne != null && scanResultListOne.size() > 0) {
                    for (ScanResult scanResult : scanResultListOne) {
                        String bssid = scanResult.BSSID;
                        int level = scanResult.level;
                        oneMap.put(bssid, level);
                    }
                }

                if (scanResultListTwo != null && scanResultListTwo.size() > 0) {
                    for (ScanResult scanResult : scanResultListTwo) {
                        String bssid = scanResult.BSSID;
                        int level = scanResult.level;
                        twoMap.put(bssid, level);
                    }
                }

                List<Map.Entry<String, Integer>> one = new ArrayList<>(oneMap.entrySet());
                List<Map.Entry<String, Integer>> two = new ArrayList<>(twoMap.entrySet());

                showMatrixes(one, two, newScanResultList.size());

                //计算第一个样本的模
                int sumOne = 0;
                for (Map.Entry<String, Integer> stringIntegerEntry : one) {
                    int value = stringIntegerEntry.getValue();
                    sumOne += value * value;
                }
                double sqrtOne = Math.sqrt(sumOne);

                //计算第二个样本的模
                int sumTwo = 0;
                for (Map.Entry<String, Integer> stringIntegerEntry : two) {
                    int value = stringIntegerEntry.getValue();
                    sumTwo += value * value;
                }
                double sqrtTwo = Math.sqrt(sumTwo);

                //数量积的计算
                int quantity = 0;
                int size = newScanResultList.size();
                for (int i = 0; i < size; i++) {
                    int valueOne = one.get(i).getValue();
                    int valueTwo = two.get(i).getValue();
                    quantity += (valueOne * valueTwo);
                }

                //相似度计算
                if (sqrtOne * sqrtTwo == 0D || quantity == 0) {
                    tvSimilarity.setText(String.valueOf(0));
                } else {
                    double v1 = quantity / (sqrtOne * sqrtTwo);
                    tvSimilarity.setText(df.format(v1));
                }
            }
        });
    }

    private void showMatrixes(List<Map.Entry<String, Integer>> one, List<Map.Entry<String, Integer>> two, int size) {
        StringBuilder stringBuilderOne = new StringBuilder();
        StringBuilder stringBuilderTwo = new StringBuilder();
        for (int i = 0; i < size; i++) {
            Map.Entry<String, Integer> stringIntegerEntryOne = one.get(i);
            String key = stringIntegerEntryOne.getKey();
            int value = stringIntegerEntryOne.getValue();
            stringBuilderOne.append(key).append(" , ").append(value).append("\n");
        }

        for (int i = 0; i < size; i++) {
            Map.Entry<String, Integer> stringIntegerEntryTwo = two.get(i);
            String key = stringIntegerEntryTwo.getKey();
            int value = stringIntegerEntryTwo.getValue();
            stringBuilderTwo.append(key).append(" , ").append(value).append("\n");
        }

        tvMatrixes1.setText(stringBuilderOne);
        tvMatrixes2.setText(stringBuilderTwo);
    }
}
