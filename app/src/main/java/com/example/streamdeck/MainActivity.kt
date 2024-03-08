package com.example.streamdeck

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.streamdeck.ble.PermissionRequestCode
import com.example.streamdeck.ble.initBluetooth
import com.example.streamdeck.ble.requestPermissions
import com.example.streamdeck.ble.startBLEScan
import com.example.streamdeck.ui.theme.StreamdeckTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StreamdeckTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainUi()
                }
            }
        }
        requestPermissions(this)
        initBluetooth(this)
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)
            == PackageManager.PERMISSION_GRANTED) {
            startBLEScan()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStart() {
        super.onStart()
        requestPermissions(this)
        initBluetooth(this)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onResume() {
        super.onResume()
        requestPermissions(this)
        initBluetooth(this)
    }
    @Deprecated("Deprecated in Java")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[grantResults.size-1] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("permissions", "granted")
                    startBLEScan()
                } else {
                    Log.e("permissions", "denied")
                    //Toast.makeText(this, this.getString(R.string.permission_needed), Toast.LENGTH_LONG).show()
                    //requestPermissions(this)
                }
            }
        }
    }
}
