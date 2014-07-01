/** LEScanner.java --- 
 *
 * Copyright (C) 2014 Dmitry Mozgin
 *
 * Author: Dmitry Mozgin <m0391n@gmail.com>
 *
 * 
 */

package com.m039.ibeacon.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;

import com.m039.ibeacon.U;
import com.m039.ibeacon.content.IBeacon;
import com.m039.ibeacon.content.IBeaconFactory;

/**
 * 
 *
 * Created: 
 *
 * @author 
 * @version 
 * @since 
 */
public class LEScanner {
    final private long mPeriod;
    final private Handler mHandler = new Handler();

    public LEScanner() {
        this(1000L);
    }

    public LEScanner(long period) {
        mPeriod = period;
    }

    private Runnable mOnContinueScanRunnable = null;

    public static abstract class LeScanCallback 
        implements BluetoothAdapter.LeScanCallback {
        @Override
        public void onLeScan (BluetoothDevice device, int rssi, byte[] scanRecord) {
            IBeacon ibeacon = IBeaconFactory.decodeScanRecord(scanRecord);
            if (ibeacon != null) {
                onLeScan(device, rssi, ibeacon);
            }
        }

        public abstract void onLeScan(BluetoothDevice device, int rssi, IBeacon ibeacon);
    }

    public boolean startScan(final Context ctx, final LeScanCallback callback) {
        BluetoothAdapter ba = U.getBluetoothAdapter(ctx);
        if (ba != null && mOnContinueScanRunnable == null) {
            if (ba.startLeScan(callback)) {
                onStartScan();

                mOnContinueScanRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (!onContinueScan()) {
                                stopScan(ctx, callback);
                            } else {
                                // rescan
                                mHandler.postDelayed(this, mPeriod);
                            }
                        }
                    };

                mHandler.postDelayed(mOnContinueScanRunnable, mPeriod);

                return true;

            } else {

                onStopScan();

            }
        }

        return false;
    }

    public void stopScan(Context ctx, LeScanCallback callback) {
        BluetoothAdapter ba = U.getBluetoothAdapter(ctx);
        if (ba != null && mOnContinueScanRunnable != null) {
            mHandler.removeCallbacks(mOnContinueScanRunnable);
            mOnContinueScanRunnable = null;
            ba.stopLeScan(callback);
        }

        onStopScan();
    }

    protected void onStartScan() {
    }

    protected boolean onContinueScan() {
        return true;
    }

    protected void onStopScan() {
    }
} // LEScanner