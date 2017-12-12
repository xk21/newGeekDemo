package com.sharedream.geek.app;

import org.json.JSONArray;
import org.json.JSONObject;

public interface SceneResultObserver {

    void onSceneResultFound(JSONObject sceneCard);

    void onSceneResultNotFound(JSONArray sceneCard);

    void onRelease();
}
