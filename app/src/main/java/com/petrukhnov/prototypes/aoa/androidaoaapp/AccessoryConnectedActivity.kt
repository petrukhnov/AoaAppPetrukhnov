package com.petrukhnov.prototypes.aoa.androidaoaapp

import android.app.Activity
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Parcelable
import android.util.Log

class AccessoryConnectedActivity : Activity() {
    private val TAG = AccessoryConnectedActivity::class.java.simpleName

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        Log.d(TAG, "intent Action: ${intent.action}")
        val intent = intent
        if (intent != null && intent.action == UsbManager.ACTION_USB_ACCESSORY_ATTACHED) {
            val usbAccessory = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_ACCESSORY)
            Log.d(TAG, "accessory: $usbAccessory")
            val redirectIntent = Intent(UsbAccessoryManager.ACTION_USB_ACCESSORY_ATTACHED)
            redirectIntent.putExtra(UsbManager.EXTRA_ACCESSORY, usbAccessory)
            redirectIntent.putExtra(UsbManager.EXTRA_PERMISSION_GRANTED, true)
            Log.d(TAG, "sending broadcast intent for accessory")
            sendBroadcast(redirectIntent)
        }
        // Close the activity
        Log.d(TAG, "сlosing activity")
        finish()
    }
}