package com.equationl.autocontroller.viewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.autocontroller.utils.BtHelper
import com.equationl.autocontroller.utils.FormatUtils.toHexStr
import com.equationl.autocontroller.utils.FormatUtils.toText
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

class HomeViewModel: ViewModel() {
    private var socket: BluetoothSocket? = null

    var viewStates by mutableStateOf(HomeStates())
        private set

    fun dispatch(action: HomeAction) {
        when (action) {
            is HomeAction.ClickBack -> clickBack(action.activity)
            is HomeAction.InitBt -> initBt(action.context)
            is HomeAction.ConnectDevice -> connectDevice(action.device)
            is HomeAction.OnClickButton -> onClickButton(action.index, action.action)
        }
    }

    private fun onClickButton(index: ButtonIndex, action: ButtonAction) {
        // TODO

        // fixme test
        viewModelScope.launch {
            if (action == ButtonAction.Down) {
                Log.i(TAG, "onClickButton: button down")
                BtHelper.instance.sendByteToDevice(socket!!, byteArrayOf(10)) {
                    it.fold(
                        {
                            Log.i(TAG, "onClickButton: ${it.toHexStr()}")
                        },
                        {
                            Log.e(TAG, "onClickButton: ", it)
                        }
                    )
                }
            }
            else {
                Log.i(TAG, "onClickButton: button up")
            }
        }

    }

    private fun clickBack(activity: Activity?) {
        when (viewStates.connectState) {
            ConnectState.NotConnect -> {
                activity?.finish()
            }
            ConnectState.Connecting -> {
                // 正在连接时不做响应
            }
            ConnectState.AlreadyConnect -> {
                BtHelper.instance.cancelConnect(socket)
                BtHelper.instance.stopBtReceiveServer()
                viewStates = HomeStates()
                if (activity != null) {
                    initBt(activity.applicationContext)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectDevice(device: BluetoothDevice) {

        viewModelScope.launch {
            viewStates = viewStates.copy(
                connectState = ConnectState.Connecting,
                connectDevice = device,
                title = "连接中"
            )

            BtHelper.instance.connectDevice(device) {
                it.fold(
                    {
                        socket = it

                        viewModelScope.launch {
                            if (socket != null) {
                                BtHelper.instance.startBtReceiveServer(socket!!, onReceive = { byte, byteBufferArray ->
                                    /* TODO */
                                    Log.i(TAG, "connectDevice: rev：byte=$byte, \nbyteBuffer(hex)=${byteBufferArray.toHexStr()}, \nbyteBuffer(ascii)=${byteBufferArray.toText()}")
                                })
                            }
                        }

                        viewStates = viewStates.copy(
                            connectState = ConnectState.AlreadyConnect,
                            pairedDevices = setOf(),
                            title = device.name
                        )
                    },
                    { tr ->
                        viewStates = viewStates.copy(
                            connectState = ConnectState.NotConnect,
                            pairedDevices = setOf(),
                            initTip = "连接失败，请重试！\n${tr.message}",
                            connectDevice = null,
                            title = "连接失败"
                        )
                    }
                )
            }
        }

    }

    private fun initBt(context: Context) {
        BtHelper.instance.init(context)

        viewStates = if (BtHelper.instance.checkBluetooth(context)) {
            viewStates.copy(
                connectState = ConnectState.NotConnect,
                pairedDevices = BtHelper.instance.queryPairDevices() ?: setOf(),
                title = "等待连接"
            )
        } else {
            viewStates.copy(
                connectState = ConnectState.NotConnect,
                pairedDevices = setOf(),
                initTip = "请授予蓝牙权限并打开蓝牙后重试！",
                title = "需要权限"
            )
        }
    }
}

data class HomeStates(
    val initTip: String = "正在初始化蓝牙中",
    val connectState: ConnectState = ConnectState.NotConnect,
    val pairedDevices: Set<BluetoothDevice> = setOf(),
    val connectDevice: BluetoothDevice? = null,
    val title: String = "Auto controller"
)

sealed class HomeAction {
    data class ClickBack(val activity: Activity?): HomeAction()
    data class InitBt(val context: Context): HomeAction()
    data class ConnectDevice(val device: BluetoothDevice) : HomeAction()
    data class OnClickButton(val index: ButtonIndex, val action: ButtonAction) : HomeAction()
}

enum class ConnectState {
    NotConnect,
    Connecting,
    AlreadyConnect
}

enum class ButtonIndex {
    Lock,
    Unlock,
    Loop
}

enum class ButtonAction {
    Down,
    Up
}