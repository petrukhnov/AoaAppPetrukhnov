package com.petrukhnov.prototypes.aoa.androidaoaapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

class UsbAccessoryManager(context: Context, val callback: (String, String?) -> Unit) {
    private val TAG = UsbAccessoryManager::class.java.simpleName
    private val ACCESSORY_MODEL = "deviceName"
    private val ACCESSORY_MANUFACTURER = "petrukhnovVendorName"
    private val applicationContext: Context
    private val usbAttachedEventReceiver: BroadcastReceiver = UsbAttachedBroadcastReceiver()
    private val usbManager: UsbManager
    private var streamInterrupted = false

    init {
        applicationContext = context.applicationContext
        usbManager = applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
        checkOnStart()
    }

    companion object {
        val TAG = UsbAccessoryManager::class.java.simpleName
        val ACTION_USB_ACCESSORY_ATTACHED = TAG + "action_accessory_attached"

        val STATE_CONNECTED = "connected"
        val STATE_FAILED_CONNECT = "failed-connect"
        val STATE_DISCONNECTED = "disconnected"
    }

    fun registerUsbAttachReceiver() {
        Log.d(TAG, "registering usb attach receiver")
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_ACCESSORY_ATTACHED)
        applicationContext.registerReceiver(usbAttachedEventReceiver, filter)
    }

    fun unregisterUsbAttachReceiver() {
        Log.d(TAG, "unregistering usb attach receiver")
        try {
            applicationContext.unregisterReceiver(usbAttachedEventReceiver)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Unregistering usbAttachedEventReceiver receiver, that wasnt registered")
        }
    }

    private inner class UsbAttachedBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "inside UsbAttachedBroadcastReceiver")
            val usbAccessory = intent.getParcelableExtra<UsbAccessory>(UsbManager.EXTRA_ACCESSORY)
            Log.d(TAG, "received accessory: $usbAccessory")
            openAccessory(usbAccessory!!)
        }
    }

    private fun openAccessory(usbAccessory: UsbAccessory) {
        Log.d(TAG, "openAccessory(): $usbAccessory")

        GlobalScope.launch(Dispatchers.Main) {
            val usbAccessorySerialPort = UsbAccessoryIO(usbAccessory)
            val result = usbAccessorySerialPort.open(usbManager)
            Log.d(TAG, "openAccessory() result: $result")
            if (result){
                callback.invoke(STATE_CONNECTED, null)
                startListeningToAccessory(usbAccessorySerialPort)
            } else {
                callback.invoke(STATE_FAILED_CONNECT, null)
            }
        }
    }

    private fun checkOnStart() {
        val accessoryList = usbManager.accessoryList
        if (accessoryList.isNullOrEmpty()) return
        Log.d(TAG, "usb accessories found on start: ${accessoryList.size}")
        for (accessory in accessoryList){
            if (accessory.manufacturer == ACCESSORY_MANUFACTURER && accessory.model == ACCESSORY_MODEL){
                openAccessory(accessory)
                return
            }
        }
    }

    private fun startListeningToAccessory(usbAccessoryIO: UsbAccessoryIO) {
        GlobalScope.launch(Dispatchers.IO) { listeningLoop(usbAccessoryIO) }
    }

    private suspend fun listeningLoop(socket: UsbAccessoryIO) {
        val inStream: ConnectionInputStream
        val outStream: ConnectionOutputStream
        val readBuffer = ByteArray(16*1024)
        try {
            inStream = socket.getInputStream()
            outStream = socket.getOutputStream()
        } catch (e: IOException) {
            Log.e(TAG, "streams not created: " + e.message)
            return
        }
        while (!streamInterrupted){
            try {
                val readBytes = inStream.read(readBuffer)
                val readString = String(readBuffer, 0, readBytes)
                Log.e(TAG, "readString: " + readString)
                //reply pongAndroid to pingSrv
                if (readString.equals("pingSrv")) {
                    outStream.write("pongAndroid".toByteArray())
                }

            } catch (e: IOException) {
                Log.e(TAG, "Stream IOException " + e.message)
                GlobalScope.launch(Dispatchers.IO){
                    callback.invoke(STATE_DISCONNECTED, e.message)
                }
                break
            }
            delay(100L)
        }
        // releasing resources
        try {
            inStream.close()
        } catch (e: IOException) {
            Log.e(TAG, "inStream.close() failed " + e.message)
        }
        try {
            outStream.close()
        } catch (e: IOException) {
            Log.e(TAG, "outStream.close() failed " + e.message)
        }
        try {
            socket.close()
        } catch (e: IOException) {
            Log.e(TAG, "socket.close() failed " + e.message)
        }
    }
}