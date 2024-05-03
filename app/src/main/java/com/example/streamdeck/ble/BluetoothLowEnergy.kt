package com.example.streamdeck.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.streamdeck.MainActivity
import com.example.streamdeck.clipboardSelected
import com.example.streamdeck.clipboardString
import com.example.streamdeck.connected
import com.example.streamdeck.connecting
import com.example.streamdeck.holdKey
import com.example.streamdeck.infoString
import com.example.streamdeck.infoStringId
import com.example.streamdeck.scanning
import com.example.streamdeck.selectedCharKey
import com.example.streamdeck.selectedKeysString
import com.example.streamdeck.selectedKeysStringEncoder
import com.example.streamdeck.showMtuRequestErrorDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID

val characters = arrayOf(
    'q', 'w', 'e', 'r', 't', 'z', 'u', 'i', 'o', 'p', 'ü', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'ö', 'ä', 'y', 'x', 'c', 'v', 'b', 'n', 'm',
    '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
    'ß', '<', '>', '.', ';', ':'
)

var configCharacteristic: BluetoothGattCharacteristic? = null

lateinit var genericService: BluetoothGattService
lateinit var bleApplicationContext: Context
val CharacteristicMap = mapOf(
    UUID.fromString("006f82e3-fafa-44d9-82ac-add23151a870") to ::configCharacteristic
)

val serviceMap = mapOf(
    UUID.fromString("34b4daf9-ff80-4e58-a497-40d349f78692") to ::genericService,
)

lateinit var bluetoothAdapter: BluetoothAdapter

const val PermissionRequestCode = 5

fun initBluetooth(context: Context){
    val bluetoothManager: BluetoothManager? =
        ContextCompat.getSystemService(context, BluetoothManager::class.java)
    bluetoothAdapter = bluetoothManager!!.adapter

    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    enableBluetooth(context)
}


fun enableBluetooth(context: Context){
    val activity = context.findActivity()
    if (!bluetoothAdapter.isEnabled) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        ActivityCompat.startActivityForResult(
            activity,
            enableBtIntent,
            PermissionRequestCode,
            null
        )
    }
    else{
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    }
}

@RequiresApi(Build.VERSION_CODES.S)
val permissions = arrayOf(
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_ADMIN,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.BLUETOOTH_CONNECT
)


@RequiresApi(Build.VERSION_CODES.S)
fun requestPermissions(context: Context){
    if ((ContextCompat.checkSelfPermission(
            context as MainActivity,
            Manifest.permission.BLUETOOTH_CONNECT
        ) != PackageManager.PERMISSION_GRANTED
                )||(ContextCompat.checkSelfPermission(
            context as MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                )) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                context,
                permissions,
                PermissionRequestCode
            )
        }
    }


}

private tailrec fun Context.findActivity(): Activity =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> throw IllegalStateException(
            "findActivity should be called in the context of an Activity"
        )
    }


@SuppressLint("MissingPermission")
fun connectToDevice(device: BluetoothDevice): Boolean {
    connecting = true
    var successful = false
    MainScope().launch {
        try {
            Log.e("connection", device.address)
            if(scanning){
                stopBLEScan()
            }
            if (bluetoothGatt != null){
                Log.d("connection", "already connected")
                for (deviceName in deviceNames) {
                    connected = true
                    connecting = false
                }

            }else{
                bluetoothGatt = device.connectGatt(bleApplicationContext, false, bluetoothGattCallback)
                if(bluetoothGatt != null){
                    successful = true
                }
            }
        } catch (e: NullPointerException) {
            Log.e("Connect", "ERROR Null pointer Exception")
        }
    }
    Log.e("connection ", successful.toString())
    return successful
}

@SuppressLint("MissingPermission")
fun disconnectDevice(){
    bluetoothGatt?.disconnect()
}


val bluetoothGattCallback = object : BluetoothGattCallback() {
    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            //gatt?.requestMtu(100)
            // successfully connected to the GATT Server
            Log.e("connection", "successful")

            if(gatt != null){
                connected = true
                connecting = false
            }
            //val res = bluetoothGatt?.requestMtu(100)
            //Log.e("res", res.toString())

            bluetoothGatt?.discoverServices()

            if(scanning){
                stopBLEScan()
            }


        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            // disconnected from the GATT Server
            gatt?.device?.name?.let { Log.e("disconnected", it) }
            gatt?.device?.bondState?.let { Log.e("disconnected", it.toString()) }

            connected = false

            gatt?.close()
            bluetoothGatt = null;

        }
        if (newState == BluetoothProfile.STATE_DISCONNECTING) {
            // disconnected from the GATT Server
            gatt?.device?.name?.let { Log.e("disconnecting", it) }
            gatt?.device?.bondState?.let { Log.e("disconnected", it.toString()) }

            connected = false

        }
    }

    @SuppressLint("MissingPermission")
    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d("mtu changed", "success: $mtu")
        } else {
            Log.e("mtu changed", "failed")
            showMtuRequestErrorDialog = true
            disconnectDevice()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        Log.e("succesfully wrote", characteristic.toString())

        super.onCharacteristicWrite(gatt, characteristic, status)
    }

    @SuppressLint("MissingPermission")

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        Log.e("descriptor", "wrote")
        val parentCharacteristic = descriptor?.characteristic

        Log.e("desc char", parentCharacteristic?.uuid.toString())

        currentCharacteristicIndex++
        enableNotifications()

        super.onDescriptorWrite(gatt, descriptor, status)

    }

    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {

        super.onServicesDiscovered(gatt, status)
        val services = gatt.services
        Log.e("service count", services.size.toString())
        for (service in services) {
            Log.e("service", service.uuid.toString())
            val serviceVariable = serviceMap[service.uuid]
            if (serviceVariable != null) {
                serviceVariable.set(service)
            } else {
                Log.e("unknown service found ", service.uuid.toString())
            }
            Log.e("service discovery", "Service UUID: " + service.uuid)

            for (characteristic in service.characteristics) {
                val characteristicVariable = CharacteristicMap[characteristic.uuid]
                Log.i("characteristic discovery", "Characteristic UUID: " + characteristic.uuid)

                if (characteristicVariable != null) {
                    characteristicVariable.set(characteristic)
                    Log.i("characteristic discovery", "Characteristic UUID: " + (characteristicVariable.get()?.uuid))
                    Log.i("characteristic discovery", "Characteristic UUID: " + (characteristic.uuid==characteristicVariable.get()?.uuid).toString())
                    if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                        bluetoothGatt?.setCharacteristicNotification(characteristic, true)
                        Log.e("notification", characteristic.toString())
                    }
                    Log.e("onServicesDiscovered:", characteristic.properties.toString())
                } else {
                    Log.e("new characteristic found", characteristic.uuid.toString())
                    //disconnectGatt()
                }
            }
        }
        enableNotifications()

    }

    @Deprecated("Deprecated in Java")
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
    ) {
        super.onCharacteristicChanged(gatt, characteristic)

        var values: String? = ""
        val data = characteristic!!.value
        Log.e("char changed", characteristic.uuid.toString())

        if (data != null && data.isNotEmpty()) {
            val stringBuilder = StringBuilder(data.size)
            for (byteChar in data) stringBuilder.append(String.format("%02X ", byteChar))
            values = String(data)
            Log.e("char changed", values)

            characteristicChanged(characteristic, values)

        }
    }


}


fun characteristicChanged (characteristic: BluetoothGattCharacteristic, value: String){
    when (characteristic) {
        configCharacteristic -> {
            if (value.startsWith("s,")){
                //writeCharacteristic(configCharacteristic, "s," + id + "," + value + ",{{"+ config + "}}," + String(clipboard));
                val divider1 = value.indexOf(',')
                val divider2 = value.indexOf(',', divider1+1)
                val divider3 = value.indexOf(",{{", divider2+1)
                val divider4 = value.indexOf("}},", divider3+1)

                infoStringId = value.substring(divider1+1, divider2).toString()
                Log.e("infoStringId", infoStringId.toString())
                infoString = value.substring(divider2+1, divider3)
                Log.e("infoString", infoString)
                runBlocking {
                    launch(Dispatchers.Main) { // Launches a coroutine on the main thread
                        clipboardSelected = value.substring(divider4+3, value.length)=="1"
                        println("Coroutine running on the main thread")
                    }
                }
                Log.e("clipboard", clipboardSelected.toString())
                Log.e("divider 4", divider4.toString())
                val config = value.substring(divider3+3, divider4)
                Log.e("config", config)
                if(clipboardSelected){
                    clipboardString = value.substring(divider3+3, divider4)
                    selectedKeysString = ""
                    selectedCharKey = null
                }else{
                    if(config.isNotEmpty()) {
                        val lastPart =
                            config.substring(config.indexOfLast { it == '+' } + 1, config.length)
                        if (!lastPart.contains("KEY_")) {
                            selectedKeysString =
                                config.substring(0, config.indexOfLast { it == '+' })
                            selectedCharKey = lastPart[0]
                        } else {
                            selectedKeysString = config
                            selectedCharKey = null
                        }
                    }else{
                        selectedKeysString = ""
                        selectedCharKey = null
                    }

                    selectedKeysStringEncoder = selectedKeysString;
                    clipboardString = ""
                }
                Log.e("selectedKeys", selectedKeysString)
                Log.e("selectedChar", selectedCharKey.toString())
            }
            Log.e("battery state", "$value%")
        }
    }
}

@SuppressLint("MissingPermission")
fun writeCharacteristic(char: BluetoothGattCharacteristic?, value: String) {
    if (char != null && bluetoothGatt != null) {
        char.setValue(value)
        bluetoothGatt!!.writeCharacteristic(char)
        Log.e(char.toString(), "wrote $value")

    } else {
        //navController.navigate()
        Log.e("device", "not connected")
    }
}


var currentCharacteristicIndex = 0
fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)
fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)
fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean =
    properties and property != 0
val CCC_DESCRIPTOR_UUID: UUID =  UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")


@SuppressLint("MissingPermission")
fun enableNotifications() {
    val notificationCharacteristics = listOf(configCharacteristic)

    if (currentCharacteristicIndex < notificationCharacteristics.size) {
        Log.d(
            "currentCharacteristicIndex char",
            notificationCharacteristics[currentCharacteristicIndex]?.uuid.toString()
        )
        val payload = when {
            notificationCharacteristics[currentCharacteristicIndex]?.isIndicatable() == true -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            //characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> {
                Log.e(
                    "ConnectionManager",
                    "${notificationCharacteristics[currentCharacteristicIndex]?.uuid} doesn't support notifications/indications"
                )
                currentCharacteristicIndex++
                enableNotifications()
                return
            }
        }

        notificationCharacteristics[currentCharacteristicIndex]?.getDescriptor(CCC_DESCRIPTOR_UUID)
            ?.let { cccDescriptor ->
                if (bluetoothGatt?.setCharacteristicNotification(
                        notificationCharacteristics[currentCharacteristicIndex],
                        true
                    ) == false
                ) {
                    Log.e(
                        "ConnectionManager",
                        "setCharacteristicNotification failed for ${notificationCharacteristics[currentCharacteristicIndex]?.uuid}"
                    )
                    currentCharacteristicIndex++
                    enableNotifications()
                    return
                }
                writeDescriptor(cccDescriptor, payload)
            } ?: run {
            Log.e(
                "ConnectionManager",
                "${notificationCharacteristics[currentCharacteristicIndex]?.uuid} doesn't contain the CCC descriptor!"
            )
            currentCharacteristicIndex++
            enableNotifications()
        }
    } else {
        currentCharacteristicIndex = 0
        Log.d("notifications", "enabled all")
        bluetoothGatt?.requestMtu(517)
    }
}

@SuppressLint("MissingPermission")
fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
    bluetoothGatt?.let { gatt ->
        descriptor.value = payload
        gatt.writeDescriptor(descriptor)
    } ?: error("Not connected to a BLE device!")
}