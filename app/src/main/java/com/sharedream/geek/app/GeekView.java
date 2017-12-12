package com.sharedream.geek.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.sharedream.geek.sdk.GeekSdk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by young on 2017/6/16.
 */

public class GeekView extends LinearLayout implements SceneResultObserver {
    private Context context;
    private String sceneUrl;
    private String shopName;
    private GridView gvSceneCard;
    private TextView tvSceneName;
    private TextView tvFootprints;
    private TextView tvNearScene;
    private ImageView ivSceneLogo;
    private ImageView ivOtherSceneLogo;
    private LinearLayout llOneScreen;
    private RelativeLayout rlShop;
    private SceneCardAdapter sceneCardAdapter;
    private List<SceneNode> newSceneNodeList;
    private boolean isShowOtherScene = false;
    private JSONObject currentScenePage;
    private List<JSONObject> sceneList = new ArrayList<>();

    public GeekView(Context context) {
        super(context);
        this.context = context;
        initView();
        setListener();
    }

    private void setListener() {
        SceneResultSubject.getInstance().registerObserver(this);
        rlShop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SceneServiceActivity.class);
                intent.putExtra("actionUrl", sceneUrl);
                intent.putExtra("name", shopName);
//                intent.putExtra("actionUrl", "http://i.dmall.com");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        gvSceneCard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SceneNode sceneNode = newSceneNodeList.get(position);
                String actionUrl = sceneNode.actionUrl;
                Intent intent = new Intent(context, SceneServiceActivity.class);
                intent.putExtra("actionUrl", actionUrl);
                intent.putExtra("name", sceneNode.serviceName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        tvFootprints.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SceneServiceActivity.class);
                intent.putExtra("actionUrl", "http://poi.igeekee.cn/scene/history");
                intent.putExtra("name", "最近足迹");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        tvNearScene.setOnClickListener(new OnClickListener() {
            private double lastLatitude = -1.0;
            private double lastLongitude = -1.0;

            @Override
            public void onClick(View v) {
                double lastLatitude = LocationModule.getInstance().getLastLatitude();
                double lastLongitude = LocationModule.getInstance().getLastLongitude();

                if (this.lastLatitude == lastLatitude && this.lastLongitude == lastLongitude) {
                    String nearSceneUrl = SceneManager.getInstance().getNearSceneUrl();
                    if (!TextUtils.isEmpty(nearSceneUrl)) {
                        Intent intent = new Intent(context, SceneServiceActivity.class);
                        intent.putExtra("actionUrl", nearSceneUrl);
                        intent.putExtra("name", "附近场景");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        return;
                    }
                }
                this.lastLatitude = lastLatitude;
                this.lastLongitude = lastLongitude;
                GeekSdk.queryNearbyPoiUrl(lastLongitude, lastLatitude);
            }
        });

        ivOtherSceneLogo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sceneList != null) {
                    int size = sceneList.size();
                    JSONObject currentScene = sceneList.get(size - 1);
                    JSONObject rightScene = sceneList.get(size - 2);
                    sceneList.remove(currentScene);
                    sceneList.remove(rightScene);
                    sceneList.add(currentScene);
                    sceneList.add(rightScene);
                    showSceneCard();
                }
            }
        });
    }

    private void showSceneCard(JSONObject sceneData) {
        saveScene2Sp(sceneData);
        shopName = sceneData.optString("shopName");
        String logoUrl = sceneData.optString("logoUrl");
        sceneUrl = sceneData.optString("sceneUrl");
        tvSceneName.setText(shopName);
        if (!TextUtils.isEmpty(logoUrl)) {
            Glide.with(context).load(logoUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    // 商家logo裁剪成圆角
                    Bitmap convert2RoundBitmap = MyUtils.convert2BorderBitmap(resource, 0, context);
                    ivSceneLogo.setImageBitmap(convert2RoundBitmap);
                }
            });
        }

        JSONArray serviceJsonArray = sceneData.optJSONArray("service");
        if (serviceJsonArray != null) {
            List<SceneNode> sceneNodeList = new ArrayList<>();
            int length = serviceJsonArray.length();
            for (int i = 0; i < length; i++) {
                SceneNode sceneNode = new SceneNode();
                JSONObject sceneObject = serviceJsonArray.optJSONObject(i);
                String serviceName = sceneObject.optString("serviceName");
                String actionUrl = sceneObject.optString("actionUrl");
                String iconHttpUrl = sceneObject.optString("iconHttpUrl");
                String iconFileUrl = sceneObject.optString("iconFileUrl");

                sceneNode.serviceName = serviceName;
                sceneNode.actionUrl = actionUrl;
                sceneNode.iconHttpUrl = iconHttpUrl;
                sceneNode.iconFileUrl = iconFileUrl;
                sceneNode.sceneUrl = sceneUrl;
                sceneNodeList.add(sceneNode);
            }

            // 处理服务个数,最多展示6个
            newSceneNodeList = handleSceneNodeList(sceneNodeList);
//            int columnCount = newSceneNodeList.size() >  ? 6 : newSceneNodeList.size();
            gvSceneCard.setNumColumns(3);
            if (sceneCardAdapter == null) {
                sceneCardAdapter = new SceneCardAdapter(context, newSceneNodeList);
                gvSceneCard.setAdapter(sceneCardAdapter);
            } else {
                sceneCardAdapter.setData(newSceneNodeList);
                sceneCardAdapter.notifyDataSetChanged();
            }
        }
    }

    private void saveScene2Sp(JSONObject sceneData) {
        String poiId = sceneData.optString("poiId");
        String footprintInSp = MyUtils.getFootprint(context, "footprint");
        JSONArray jsonArray = new JSONArray();
        JSONArray jsonArrayFinal = new JSONArray();
        try {
            if (!TextUtils.isEmpty(footprintInSp)) {
                JSONArray footprintInSpArray = new JSONArray(footprintInSp);
                if (footprintInSpArray != null && footprintInSpArray.length() > 0) {
                    int length = footprintInSpArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject jsonObject = footprintInSpArray.optJSONObject(i);
                        String poiIdInSp = jsonObject.optString("poiId");
                        if (!poiId.equals("0") && !poiIdInSp.equals("0") && poiId.equals(poiIdInSp)) {
                            continue;
                        }

                        jsonArray.put(jsonObject);
                    }
                }
            }

            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("logoUrl", sceneData.optString("logoUrl"));
            jsonObject1.put("shopName", sceneData.optString("shopName"));
            jsonObject1.put("sceneUrl", sceneData.optString("sceneUrl"));
            jsonObject1.put("poiId", sceneData.optString("poiId"));
            jsonArray.put(jsonObject1);

            // 最多记录过去的5个场景
            int length = jsonArray.length();
            if (length > 5) {
                for (int i = length - 5; i < length; i++) {
                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                    jsonArrayFinal.put(jsonObject);
                }
            } else {
                jsonArrayFinal = jsonArray;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MyUtils.saveField(context, "footprint", jsonArrayFinal.toString());
    }

    private List<SceneNode> handleSceneNodeList(List<SceneNode> nodeList) {
        if (nodeList == null) {
            return nodeList;
        }

        List<SceneNode> sceneNodeList = new ArrayList<>();
        if (nodeList.size() > 6) {
            for (int i = 0; i < 5; i++) {
                SceneNode newSceneNode = new SceneNode();
                SceneNode sceneNode = nodeList.get(i);
                newSceneNode.serviceName = sceneNode.serviceName;
                newSceneNode.sceneUrl = sceneNode.sceneUrl;
                newSceneNode.actionUrl = sceneNode.actionUrl;
                newSceneNode.iconFileUrl = sceneNode.iconFileUrl;
                newSceneNode.iconHttpUrl = sceneNode.iconHttpUrl;
                sceneNodeList.add(newSceneNode);
            }

            // 第6个服务设置成 "更多服务"
            SceneNode sceneNode = new SceneNode();
            sceneNode.sceneUrl = sceneUrl;
            sceneNode.actionUrl = sceneUrl;
            sceneNode.serviceName = "更多服务";
            sceneNode.iconFileUrl = "file:///android_asset/icon_more_service_jinli.png";
            sceneNodeList.add(sceneNode);
        } else {
            sceneNodeList = nodeList;
        }

        return sceneNodeList;
    }

    private void initView() {
        View view = View.inflate(context, R.layout.geek_view, this);
        gvSceneCard = (GridView) view.findViewById(R.id.gv_scene_card);
        tvSceneName = (TextView) view.findViewById(R.id.tv_scene);
        ivSceneLogo = (ImageView) view.findViewById(R.id.iv_scene_logo);
        ivOtherSceneLogo = (ImageView) view.findViewById(R.id.iv_other_scene);
        tvFootprints = (TextView) view.findViewById(R.id.tv_footprints);
        tvNearScene = (TextView) view.findViewById(R.id.tv_near_scene);
        llOneScreen = (LinearLayout) view.findViewById(R.id.ll_one_screen);
        rlShop = (RelativeLayout) view.findViewById(R.id.rl_shop);
    }

    @Override
    public synchronized void onSceneResultFound(JSONObject sceneData) {
        llOneScreen.setVisibility(VISIBLE);
        if (sceneList == null) {
            sceneList = new ArrayList<>();
        }

        String stayEntePoiId = sceneData.optString("poiId");
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        for (JSONObject jsonObject : sceneList) {
            String poiId = jsonObject.optString("poiId");
            if (poiId != null && poiId.equals(stayEntePoiId)) {
                jsonObjects.add(jsonObject);
            }
        }

        if (jsonObjects != null) {
            for (JSONObject jsonObject : jsonObjects) {
                sceneList.remove(jsonObject);
            }
        }

        sceneList.add(sceneData);
        showSceneCard();
    }

    @Override
    public synchronized void onSceneResultNotFound(JSONArray leaveShopJsonArray) {
        if (leaveShopJsonArray != null && leaveShopJsonArray.length() > 0) {
            int length = leaveShopJsonArray.length();
            ArrayList<JSONObject> leaveShopJSONObject = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                JSONObject jsonObject = leaveShopJsonArray.optJSONObject(i);
                String poiId = jsonObject.optString("poiId");
                if (sceneList != null) {
                    for (JSONObject sceneJSONObject : sceneList) {
                        String stayEnterShopPoiId = sceneJSONObject.optString("poiId");
                        if (stayEnterShopPoiId != null && stayEnterShopPoiId.equals(poiId)) {
                            leaveShopJSONObject.add(sceneJSONObject);
                        }
                    }
                }
            }

            if (leaveShopJSONObject != null) {
                for (JSONObject jsonObject : leaveShopJSONObject) {
                    sceneList.remove(jsonObject);
                }
            }
            //
            showSceneCard();
        } else {
            sceneList.clear();
            llOneScreen.setVisibility(INVISIBLE);
            ivOtherSceneLogo.setVisibility(INVISIBLE);
            showSceneCard();
            Toast.makeText(context, "已经全部离店", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSceneCard() {
        if (sceneList != null) {
            for (JSONObject jsonObject : sceneList) {
                String shopName = jsonObject.optString("shopName");
                String ssid = jsonObject.optString("ssid");
                Log.i("showSceneCard", "demo test 当前所有驻店的场景 shopName : " + shopName + "  , ssid : " + ssid);
            }

            int size = sceneList.size();
            if (size == 0) {
                llOneScreen.setVisibility(INVISIBLE);
                ivOtherSceneLogo.setVisibility(INVISIBLE);
            } else if (size == 1) {
                llOneScreen.setVisibility(VISIBLE);
                ivOtherSceneLogo.setVisibility(INVISIBLE);
                JSONObject jsonObject = sceneList.get(0);
                showSceneCard(jsonObject);
            } else if (size >= 2) {
                llOneScreen.setVisibility(VISIBLE);
                ivOtherSceneLogo.setVisibility(VISIBLE);
                JSONObject mainjJsonObject = sceneList.get(size - 1);
                JSONObject rightJsonObject = sceneList.get(size - 2);
                // 显示主场景
                showSceneCard(mainjJsonObject);
                // 显示右上角logo
                showRightScene(rightJsonObject);
            }
        }
    }

    private void showRightScene(JSONObject rightJsonObject) {
        String logoUrl = rightJsonObject.optString("logoUrl");
        if (!TextUtils.isEmpty(logoUrl)) {
            ivOtherSceneLogo.setVisibility(VISIBLE);
            Glide.with(context).load(logoUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    // 商家logo裁剪成圆角
                    Bitmap convert2RoundBitmap = MyUtils.convert2RoundBitmap(resource, 0, context);
                    ivOtherSceneLogo.setImageBitmap(convert2RoundBitmap);
                }
            });
        }
    }

    @Override
    public void onRelease() {
        llOneScreen.setVisibility(INVISIBLE);
        ivOtherSceneLogo.setVisibility(INVISIBLE);
        if (newSceneNodeList != null) {
            newSceneNodeList.clear();
            newSceneNodeList = null;
        }
        isShowOtherScene = false;
        currentScenePage = null;
    }
}
