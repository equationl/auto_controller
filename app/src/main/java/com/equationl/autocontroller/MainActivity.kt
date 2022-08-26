package com.equationl.autocontroller

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.equationl.autocontroller.ui.theme.AutoControllerTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "Main"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoControllerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }


        val adapter = getBtAdapter()
        if (adapter?.isEnabled == false) {
            checkBtEnable(adapter)
        }
        else {
            queryPairDevices(adapter)
        }

    }

    private fun getBtAdapter(): BluetoothAdapter? {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.i(TAG, "getBtAdapter: device not support bt")
        }

        return bluetoothAdapter
    }

    private fun checkBtEnable(bluetoothAdapter: BluetoothAdapter?) {
        // FIXME 需要运行时权限
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }
    }

    private fun queryPairDevices(bluetoothAdapter: BluetoothAdapter?) {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address

            Log.i(TAG, "queryPairDevices: deveice name=$deviceName, mac=$deviceHardwareAddress")
        }
    }

    private fun connectDevice() {
        // TODO


    }

}



@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AutoControllerTheme {
        Greeting("Android")
    }
}