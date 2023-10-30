package com.petrukhnov.prototypes.aoa.androidaoaapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.petrukhnov.android.openaccessorysample.R
import com.petrukhnov.android.openaccessorysample.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var usbAccessoryManager: UsbAccessoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        usbAccessoryManager = UsbAccessoryManager(this){ state, errorMessage ->
            updateState(state)
            errorMessage?.let{showError(it)}
        }
    }

    override fun onStart() {
        super.onStart()
        usbAccessoryManager.registerUsbAttachReceiver()
    }

    override fun onStop() {
        super.onStop()
        usbAccessoryManager.unregisterUsbAttachReceiver()
    }

    private fun updateState(state: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.textview1.setText(state)
        }
    }

    private fun showError(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        }
    }
}