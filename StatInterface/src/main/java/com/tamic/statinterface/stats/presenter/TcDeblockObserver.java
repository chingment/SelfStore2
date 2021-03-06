
package com.tamic.statinterface.stats.presenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.tamic.statinterface.stats.constants.StaticsConfig;
import com.tamic.statinterface.stats.core.TcIntentManager;
import com.tamic.statinterface.stats.util.LogUtil;

/**
 * DeblockObserver
 * Created by Tamic on 2016-04-15.
 */
public class TcDeblockObserver extends BroadcastReceiver {

	/** DEBUG mode */
	private static final boolean DEBUG = StaticsConfig.DEBUG;
	/** LogUtil TAG */
	private static final String LogUtil_TAG = TcDeblockObserver.class.getSimpleName();
	/** Context */
	private Context mContext;
	/** IKeyguardListener */
	private IKeyguardListener mListener;

	/**
	 * Constructor
	 *
	 * @param aContext
	 *            Context
	 * @param aListener
	 *            IScreenListener
	 */
	public TcDeblockObserver(Context aContext, IKeyguardListener aListener) {
		mContext = aContext;
		mListener = aListener;
	}

	/**
	 * start Listener
	 */
	public void start() {
		try {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_USER_PRESENT);
            mContext.registerReceiver(this, filter);

			if (!isScreenLocked(mContext)) {
				if (mListener != null) {
					mListener.onKeyguardGone(mContext);
				}
			}
		} catch (Exception e) {
			if (DEBUG) {
				LogUtil.d(LogUtil_TAG, "start Exception");
			}
		}
	}

	/**
	 * stop Listener
	 */
	public void stop() {
		try {
			mContext.unregisterReceiver(this);
		} catch (Exception e) {
			if (DEBUG) {
				LogUtil.d(LogUtil_TAG, "stop Exception");
			}
		}
	}

    /**
     * is Screen Locked
     *
     * @param aContext Context
     * @return true if screen locked, false otherwise
     */
	public boolean isScreenLocked(Context aContext) {
		android.app.KeyguardManager km = (android.app.KeyguardManager) aContext
				.getSystemService(Context.KEYGUARD_SERVICE);
		return km.inKeyguardRestrictedInputMode();
	}

	@Override
	public void onReceive(Context aContext, Intent aIntent) {
		if (TcIntentManager.getInstance().isUserPresentIntent(aIntent)) {
			if (mListener != null) {
				mListener.onKeyguardGone(aContext);
			}
		}
	}

	/**
	 * IKeyguardListener
	 */
	public interface IKeyguardListener {

		/**
		 * unlock
		 * 
		 * @param aContext
		 *            Context
		 */
		void onKeyguardGone(Context aContext);
	}
}
