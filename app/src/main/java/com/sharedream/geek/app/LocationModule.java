package com.sharedream.geek.app;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.List;

public class LocationModule {
    private static LocationModule instance = null;
    private LocationListener onLocationChange = null;
    private LocationManager locationManager = null;
    private String provider = null;
    private double lastLongitude = 0;
    private double lastLatitude = 0;

    private LocationModule() {

    }

    public static LocationModule getInstance() {
        if (instance == null) {
            synchronized (LocationModule.class) {
                if (instance == null) {
                    instance = new LocationModule();
                }
            }
        }
        return instance;
    }

    public void startLocation(final Context context) {
        try {
            if (context == null) {
                return;
            }

            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            onLocationChange = new LocationListener() {
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        try {
                            setLastLongitude(location.getLongitude());
                            setLastLatitude(location.getLatitude());
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }

                public void onProviderDisabled(String arg0) {
                }

                public void onProviderEnabled(String arg0) {
                }

                public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
                    try {
                        if (provider != null) {
                            locationManager.requestLocationUpdates(provider, Constant.LOCATION_DELAY_TIME_UNIT, (float) 500.0, onLocationChange);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            };
            List<String> providerList = locationManager.getProviders(true);
            if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
                provider = LocationManager.NETWORK_PROVIDER;
            } else {
                provider = null;
            }
            if (provider != null && locationManager != null) {
                locationManager.requestLocationUpdates(provider, Constant.LOCATION_DELAY_TIME_UNIT, 10, onLocationChange);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        try {
            if (locationManager != null) {
                if (onLocationChange != null) {
                    locationManager.removeUpdates(onLocationChange);
                }
                locationManager = null;
            }

            if (onLocationChange != null) {
                onLocationChange = null;
            }

            instance = null;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public double getLastLongitude() {
        return lastLongitude;
    }

    private void setLastLongitude(double lastLastLongitude) {
        this.lastLongitude = lastLastLongitude;
    }

    public double getLastLatitude() {
        return lastLatitude;
    }

    private void setLastLatitude(double lastLatitude) {
        this.lastLatitude = lastLatitude;
    }
}