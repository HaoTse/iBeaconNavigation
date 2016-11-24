/*
 * Copyright (C) 2013 youten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.lai.project3;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

/** LeScanned Bluetooth Device */
class ScannedDevice {
    private static final String UNKNOWN = "Unknown";
    /** BluetoothDevice */
    private BluetoothDevice mDevice;
    /** RSSI */
    private int mRssi;
    /** Display Name */
    private String mDisplayName;
    /** Advertise Scan Record */
    private byte[] mScanRecord;
    /** parsed iBeacon Data */
    private IBeacon mIBeacon;
    /** last updated (Advertise scanned) */
    private long mLastUpdatedMs;

    ScannedDevice(BluetoothDevice device, int rssi, byte[] scanRecord, long now) {
        if (device == null) {
            throw new IllegalArgumentException("BluetoothDevice is null");
        }
        mLastUpdatedMs = now;
        mDevice = device;
        mDisplayName = device.getName();
        if ((mDisplayName == null) || (mDisplayName.length() == 0)) {
            mDisplayName = UNKNOWN;
        }
        mRssi = rssi;
        mScanRecord = scanRecord;
        checkIBeacon();
    }

    private void checkIBeacon() {
        if (mScanRecord != null) {
            mIBeacon = IBeacon.fromScanData(mScanRecord, mRssi);
        }
    }

    BluetoothDevice getDevice() {
        return mDevice;
    }

    int getRssi() {
        return mRssi;
    }

    void setRssi(int rssi) {
        mRssi = rssi;
    }

    void setLastUpdatedMs(long lastUpdatedMs) {
        mLastUpdatedMs = lastUpdatedMs;
    }

    public byte[] getScanRecord() {
        return mScanRecord;
    }

    String getScanRecordHexString() {
        return ScannedDevice.asHex(mScanRecord);
    }

    void setScanRecord(byte[] scanRecord) {
        mScanRecord = scanRecord;
        checkIBeacon();
    }

    IBeacon getIBeacon() {
        return mIBeacon;
    }

    String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    /**
         * Converts an array of bytes to a hexadecimal string
         *
         * @param bytes an array of bytes
         * @return a hexadecimal string
         */
    @SuppressLint("DefaultLocale")
    static String asHex(byte bytes[]) {
        if ((bytes == null) || (bytes.length == 0)) {
            return "";
        }

        // new a string buffer which length is twice the length of byte array
        StringBuffer sb = new StringBuffer(bytes.length * 2);

        for (int index = 0; index < bytes.length; index++) {
            int bt = bytes[index] & 0xff;

            if (bt < 0x10) {
                sb.append("0");
            }

            sb.append(Integer.toHexString(bt).toUpperCase());
        }

        return sb.toString();
    }

    public long getLastUpdatedMs() {
        return mLastUpdatedMs;
    }
}
