package com.example.streamdeck.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.streamdeck.scanning
import com.example.streamdeck.showNotInConfigModeDialog
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

val deviceNames = mutableStateListOf<String>()
//val devices = mutableListOf<BluetoothDevice>()
var bluetoothGatt: BluetoothGatt? = null
var scanTimeout = 10000L
lateinit var bluetoothLeScanner: BluetoothLeScanner


var scanCallback = object : ScanCallback() {
    @SuppressLint("MissingPermission")
    override fun onScanResult(callbackType: Int, result: ScanResult) {
        val device = result.device
        if(device != null){
            if (device.name == "Streamdeck"){
                showNotInConfigModeDialog = true
                stopBLEScan()
            }
            if (device.name == "Streamdeck Configuration"){
                connectToDevice(device)
            }
        }

    }

    @SuppressLint("MissingPermission")
    override fun onBatchScanResults(results: MutableList<ScanResult>) {
        for (result in results) {
            val device = result.device
            //addResult(device)
        }

    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        Log.e("scanning error", errorCode.toString())
    }
}

@SuppressLint("MissingPermission")
fun startBLEScan() {

    Handler().postDelayed({ stopBLEScan() }, scanTimeout)
    MainScope().launch {
        val scanFilter = ScanFilter.Builder().build()
        val scanSettings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        if(::bluetoothLeScanner.isInitialized){
            scanning = true
            bluetoothLeScanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
        }
    }


}

@SuppressLint("MissingPermission")
fun stopBLEScan() {
    scanning = false
    MainScope().launch {
        if(::bluetoothLeScanner.isInitialized){
            bluetoothLeScanner.stopScan(scanCallback)
        }
    }
}