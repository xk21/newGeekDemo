package com.sharedream.geek.app;

import android.text.TextUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by young on 2017/9/7.
 */

public class SceneManager {
    private static volatile SceneManager instance = null;
    private Map<String, JSONObject> sceneMap;
    private String currentScenePagePoiId;
    private String nearSceneUrl;

    public SceneManager() {
        sceneMap = new HashMap<>();
    }

    public String getNearSceneUrl() {
        return nearSceneUrl;
    }

    public void setNearSceneUrl(String nearSceneUrl) {
        this.nearSceneUrl = nearSceneUrl;
    }

    public static SceneManager getInstance() {

        if (instance == null) {
            synchronized (SceneManager.class) {
                if (instance == null) {
                    instance = new SceneManager();
                }
            }
        }
        return instance;
    }

    public void addSceneData(JSONObject sceneData) {
        if (sceneMap != null) {
            String poiId = sceneData.optString("poiId");
            if (!TextUtils.isEmpty(poiId)) {
                sceneMap.put(poiId, sceneData);
            }
        }
    }

    public JSONObject getSceneDataByPoiId(String poiId) {
        JSONObject sceneData = null;
        if (sceneMap != null) {
            if (!TextUtils.isEmpty(poiId)) {
                sceneData = sceneMap.get(poiId);
            }
        }
        return sceneData;
    }

    public void removeSceneDataByPoiId(String poiId) {
        if (sceneMap != null && !TextUtils.isEmpty(poiId)) {
            sceneMap.remove(poiId);
        }
    }

    public void removeAllSceneData() {
        if (sceneMap != null) {
            sceneMap.clear();
        }
    }

    public String getCurrentScenePagePoiId() {
        return currentScenePagePoiId;
    }

    public void setCurrentScenePagePoiId(String currentScenePagePoiId) {
        this.currentScenePagePoiId = currentScenePagePoiId;
    }

    public JSONObject getCurrentScenePagePoiId(String currentScenePagePoiId) {
        Set<Map.Entry<String, JSONObject>> entrySet = sceneMap.entrySet();
        Iterator<Map.Entry<String, JSONObject>> iterator = entrySet.iterator();
        JSONObject value = null;
        while (iterator.hasNext()) {
            Map.Entry<String, JSONObject> next = iterator.next();
            String key = next.getKey();
            if (currentScenePagePoiId != null && !currentScenePagePoiId.equals(key)) {
                value = next.getValue();
            }
        }
        return value;
    }

    public void release() {
        if (sceneMap != null) {
            sceneMap.clear();
            sceneMap = null;
        }
        instance = null;
        currentScenePagePoiId = null;
    }
}
