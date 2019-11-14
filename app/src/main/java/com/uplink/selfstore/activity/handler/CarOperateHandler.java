package com.uplink.selfstore.activity.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by chingment on 2018/6/14.
 */

public class CarOperateHandler {


    protected static final int SUCCESS_MESSAGE = 0;


    private Handler handler;

    /**
     * Creates a new AsyncHttpResponseHandler
     */
    public CarOperateHandler() {
        // Set up a handler to post events back to the correct thread if possible
        if (Looper.myLooper() != null) {
            handler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    CarOperateHandler.this.handleMessage(msg);

                    return false;
                }
            });
        }
    }

    //
    // Callbacks to be overridden, typically anonymously
    //

    /**
     * Fired when a request returns successfully, override to handle in your own code
     *
     * @param response the body of the HTTP RESTApi response from the server
     */
    public void onSuccess(String response) {
    }

    public void callAnimation()
    {

    }


    //
    // 后台线程调用方法，通过Handler sendMessage把结果转到UI主线程
    //
    protected void sendSuccessMessage(String response) {
        try {
            sendMessage(obtainMessage(SUCCESS_MESSAGE, response));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // Pre-processing of messages (in original calling thread, typically the UI thread)
    //

    protected void handleSuccessMessage(String response) {
        onSuccess(response);
    }


    // Methods which emulate android's Handler and Message methods
    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case SUCCESS_MESSAGE:
                handleSuccessMessage((String) msg.obj);
                break;
        }
    }

    protected void sendMessage(Message msg) {
        if (handler != null) {
            handler.sendMessage(msg);
        } else {
            handleMessage(msg);
        }
    }

    protected Message obtainMessage(int responseMessage, Object response) {
        Message msg = null;
        if (handler != null) {
            msg = this.handler.obtainMessage(responseMessage, response);
        } else {
            msg = Message.obtain();
            msg.what = responseMessage;
            msg.obj = response;
        }
        return msg;
    }
}
