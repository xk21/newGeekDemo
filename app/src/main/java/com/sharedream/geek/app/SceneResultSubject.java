package com.sharedream.geek.app;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SceneResultSubject {
    private static volatile SceneResultSubject instance;
    private List<SceneResultObserver> observerList;

    private SceneResultSubject() {
        observerList = new ArrayList<SceneResultObserver>();
    }

    public static SceneResultSubject getInstance() {
        if (instance == null) {
            synchronized (SceneResultSubject.class) {
                if (instance == null) {
                    instance = new SceneResultSubject();
                }
            }
        }
        return instance;
    }

    public void registerObserver(SceneResultObserver observer) {
        unregisterObserver(observer);
        observerList.add(observer);
    }

    public void unregisterObserver(SceneResultObserver observer) {
        for (int k = 0; k < observerList.size(); k++) {
            SceneResultObserver item = observerList.get(k);
            if (item.getClass().getName().equals(observer.getClass().getName())) {
                observerList.remove(item);
                break;
            }
        }
    }

    public void notifySceneResultFound(JSONObject sceneCard) {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            SceneResultObserver observer = observerList.get(k);
            if (observer != null) {
                observer.onSceneResultFound(sceneCard);
            }
        }
    }
    
    public void notifySceneResultNotFound(JSONArray sceneCard) {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            SceneResultObserver observer = observerList.get(k);
            if (observer != null) {
                observer.onSceneResultNotFound(sceneCard);
            }
        }
    }

    public void release() {
        int size = observerList.size();
        for (int k = 0; k < size; k++) {
            SceneResultObserver observer = observerList.get(k);
            if (observer != null) {
                observer.onRelease();
            }
        }
    }
}
