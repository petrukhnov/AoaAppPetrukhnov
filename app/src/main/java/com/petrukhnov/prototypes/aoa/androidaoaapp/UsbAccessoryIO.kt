package com.petrukhnov.prototypes.aoa.androidaoaapp

import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbManager
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream

class UsbAccessoryIO(val usbAccessory: UsbAccessory) {
    private val TAG = UsbAccessoryIO::class.java.simpleName
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var fileDescriptor: FileDescriptor? = null
    private var iStream: FileInputStream? = null
    private var oStream: FileOutputStream? = null

    val serial: String?
    get(){
        return usbAccessory.serial
    }

    val model: String
    get(){
        return usbAccessory.model
    }

    fun open(usbManager: UsbManager): Boolean {
        Log.d(TAG, "trying to open accessory: $usbAccessory")
        parcelFileDescriptor = usbManager.openAccessory(usbAccessory)
        fileDescriptor = parcelFileDescriptor?.fileDescriptor
        if (fileDescriptor == null) {
            Log.e(TAG, "failed to open accessory: $usbAccessory")
            return false
        }
        iStream = FileInputStream(fileDescriptor)
        oStream = FileOutputStream(fileDescriptor)
        return true
    }

    fun close() {
        Log.d(TAG, "closing accessory")
        parcelFileDescriptor?.close()
    }

    fun getInputStream(): ConnectionInputStream {
        return object : ConnectionInputStream {
            override fun read(rxBuffer: ByteArray?): Int {
                Log.d(TAG, "read: $rxBuffer")
                return iStream?.read(rxBuffer) ?: -1
            }

            override fun close() {
                Log.d(TAG, "closing input stream")
                iStream?.close()
            }

        }
    }

    fun getOutputStream(): ConnectionOutputStream {
        return object : ConnectionOutputStream {
            override fun write(txBuffer: ByteArray?) {
                Log.d(TAG, "write: $txBuffer")
                oStream?.write(txBuffer)
            }


            override fun close() {
                Log.d(TAG, "closing output stream")
                oStream?.close()
            }

        }
    }
}