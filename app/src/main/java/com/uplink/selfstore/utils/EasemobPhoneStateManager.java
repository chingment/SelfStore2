package com.uplink.selfstore.utils;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EasemobPhoneStateManager {

    public interface PhoneStateCallback {
        void onCallStateChanged(int state, String incomingNumber);
    }

    private static final String TAG = "PhoneStateManager";

    private static EasemobPhoneStateManager INSTANCE = null;

    private TelephonyManager telephonyManager;
    private List<PhoneStateCallback> stateCallbacks = null;

    public static EasemobPhoneStateManager get(Context context) {
        if (INSTANCE == null) {
            synchronized (EasemobPhoneStateManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new EasemobPhoneStateManager(context);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    protected void finalize() throws Throwable {
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        super.finalize();
    }

    public void addStateCallback(PhoneStateCallback callback) {
        if (!stateCallbacks.contains(callback)) {
            stateCallbacks.add(callback);
        }
    }

    public void removeStateCallback(PhoneStateCallback callback) {
        if (stateCallbacks.contains(callback)) {
            stateCallbacks.remove(callback);
        }
    }

    private EasemobPhoneStateManager(Context context) {
        Context appContext = context.getApplicationContext();

        telephonyManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        stateCallbacks = new CopyOnWriteArrayList<>();
    }

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            for (PhoneStateCallback callback : stateCallbacks) {
                callback.onCallStateChanged(state, incomingNumber);
            }
        }
    };
}
