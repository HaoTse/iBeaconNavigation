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

package com.uscc.ibeacon_navigation.ibeacon_detect;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

/** LeScanned Bluetooth Device */
public class ScannedDevice {
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

    ScannedDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (device == null) {
            throw new IllegalArgumentException("BluetoothDevice is null");
        }
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

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public int getRssi() {
        return mRssi;
    }

    void setRssi(int rssi) {
        mRssi = rssi;
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

    /**
         * Converts an array of bytes to a hexadecimal string
         *
         * @param bytes an array of bytes
         * @return a hexadecimal string
         */
    @SuppressLint("DefaultLocale")
    private static String asHex(byte bytes[]) {
        if ((bytes == null) || (bytes.length == 0)) {
            return "";
        }

        // new a string buffer which length is twice the length of byte array
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        for (byte aByte : bytes) {
            int bt = aByte & 0xff;

            if (bt < 0x10) {
                sb.append("0");
            }

            sb.append(Integer.toHexString(bt).toUpperCase());
        }

        return sb.toString();
    }
}
