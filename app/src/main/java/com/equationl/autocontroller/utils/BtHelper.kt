package com.equationl.autocontroller.utils

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BtHelper {
    var socket: BluetoothSocket? = null

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var keepReceive: Boolean = true

    companion object {
        private const val TAG = "BtHelper"
        const val MESSAGE_READ: Int = 0
        const val MESSAGE_WRITE: Int = 1
        const val MESSAGE_TOAST: Int = 2

        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            BtHelper()
        }
    }

    fun init(bluetoothAdapter: BluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter
    }

    fun init(context: Context): Boolean {
        val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
        this.bluetoothAdapter = bluetoothManager.adapter
        return if (bluetoothAdapter == null) {
            Log.e(TAG, "init: bluetoothAdapter is null, may this device not support bluetooth!")
            false
        } else {
            true
        }
    }

    fun checkBluetooth(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                && bluetoothAdapter?.isEnabled == true
    }

    @SuppressLint("MissingPermission")
    fun queryPairDevices(): Set<BluetoothDevice>? {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "queryPairDevices: bluetoothAdapter is null!")
            return null
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter!!.bondedDevices

        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address

            Log.i(TAG, "queryPairDevices: deveice name=$deviceName, mac=$deviceHardwareAddress")
        }

        return pairedDevices
    }

    @SuppressLint("MissingPermission")
    suspend fun connectDevice(device: BluetoothDevice, onConnected : (socket: Result<BluetoothSocket>) -> Unit) {
        val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"))
        }

        withContext(Dispatchers.IO) {

            kotlin.runCatching {
                // 开始连接前应该关闭扫描，否则会减慢连接速度
                bluetoothAdapter?.cancelDiscovery()

                mmSocket?.connect()
            }.fold({
                withContext(Dispatchers.Main) {
                    socket = mmSocket
                    onConnected(Result.success(mmSocket!!))
                }
            }, {
                withContext(Dispatchers.Main) {
                    onConnected(Result.failure(it))
                }
                Log.e(TAG, "connectDevice: connect fail!", it)
            })
        }
    }

    fun cancelConnect(mmSocket: BluetoothSocket?) {
        try {
            mmSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the client socket", e)
        }
    }

    suspend fun startBtReceiveServer(mmSocket: BluetoothSocket, onReceive: (numBytes: Int, byteBufferArray: ByteArray) -> Unit) {
        keepReceive = true
        val mmInStream: InputStream = mmSocket.inputStream
        val mmBuffer = ByteArray(1024) // mmBuffer store for the stream

        withContext(Dispatchers.IO) {
            var numBytes = 0 // bytes returned from read()
            while (true) {

                kotlin.runCatching {
                    mmInStream.read(mmBuffer)
                }.fold(
                    {
                        numBytes = it
                    },
                    {
                        Log.e(TAG, "Input stream was disconnected", it)
                        return@withContext
                    }
                )

                withContext(Dispatchers.Main) {
                    onReceive(numBytes, mmBuffer)
                }
            }
        }
    }

    fun stopBtReceiveServer() {
        keepReceive = false
    }

    suspend fun sendByteToDevice(mmSocket: BluetoothSocket, bytes: ByteArray, onSend: (result: Result<ByteArray>) -> Unit) {
        val mmOutStream: OutputStream = mmSocket.outputStream

        withContext(Dispatchers.IO) {
            val result = kotlin.runCatching {
                mmOutStream.write(bytes)
            }

            if (result.isFailure) {
                Log.e(TAG, "Error occurred when sending data", result.exceptionOrNull())
                onSend(Result.failure(result.exceptionOrNull() ?: Exception("not found exception")))
            }
            else {
                onSend(Result.success(bytes))
            }
        }
    }

    class MyBluetoothService(
        // handler that gets info from Bluetooth service
        private val handler: Handler
    ) {

        private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

            private val mmInStream: InputStream = mmSocket.inputStream
            private val mmOutStream: OutputStream = mmSocket.outputStream
            private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

            override fun run() {
                var numBytes: Int // bytes returned from read()

                // Keep listening to the InputStream until an exception occurs.
                while (true) {
                    // Read from the InputStream.
                    numBytes = try {
                        mmInStream.read(mmBuffer)
                    } catch (e: IOException) {
                        Log.d(TAG, "Input stream was disconnected", e)
                        break
                    }

                    // Send the obtained bytes to the UI activity.
                    val readMsg = handler.obtainMessage(
                        MESSAGE_READ, numBytes, -1,
                        mmBuffer)
                    readMsg.sendToTarget()
                }
            }

            // Call this from the main activity to send data to the remote device.
            fun write(bytes: ByteArray) {
                try {
                    mmOutStream.write(bytes)
                } catch (e: IOException) {
                    Log.e(TAG, "Error occurred when sending data", e)

                    // Send a failure message back to the activity.
                    val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                    val bundle = Bundle().apply {
                        putString("toast", "Couldn't send data to the other device")
                    }
                    writeErrorMsg.data = bundle
                    handler.sendMessage(writeErrorMsg)
                    return
                }

                // Share the sent message with the UI activity.
                val writtenMsg = handler.obtainMessage(
                    MESSAGE_WRITE, -1, -1, mmBuffer)
                writtenMsg.sendToTarget()
            }

            // Call this method from the main activity to shut down the connection.
            fun cancel() {
                try {
                    mmSocket.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Could not close the connect socket", e)
                }
            }
        }
    }


}