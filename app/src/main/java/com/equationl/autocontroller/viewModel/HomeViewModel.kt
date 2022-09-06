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
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.autocontroller.utils.BtHelper
import com.equationl.autocontroller.utils.FormatUtils.toBytes
import com.equationl.autocontroller.utils.FormatUtils.toHex
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
            is HomeAction.ClickPowerOn -> changePowerState(true)
            is HomeAction.ClickPowerOff -> changePowerState(false)
            is HomeAction.ClickReadState -> readState(action.readSettingOnly)
            is HomeAction.ToggleSettingView -> toggleSettingView(action.isShow)
            is HomeAction.OnAvailableInductionChange -> onAvailableInductionChange(action.value)
            is HomeAction.OnRssiThresholdChange -> onRssiThresholdChange(action.value)
            is HomeAction.OnScanningTimeChange -> onScanningTimeChange(action.value)
            is HomeAction.OnShutdownThresholdChange -> onShutdownThresholdChange(action.value)
            is HomeAction.OnTriggerUnlockChange -> onTriggerUnlockChange(action.value)
            is HomeAction.ClickSaveSetting -> clickSaveSetting()
        }
    }


    private fun clickSaveSetting() {
        viewModelScope.launch {
            val cmdList = listOf(
                "FF01${viewStates.scanningTime.toInt().toHex(2)}FF".toBytes(),
                "FF02${viewStates.rssiThreshold.toInt().toHex(2)}FF".toBytes(),
                "FF03${if (viewStates.triggerUnlock) "01" else "00"}FF".toBytes(),
                "FF04${if (viewStates.availableInduction) "01" else "00"}FF".toBytes(),
                "FF05${viewStates.shutdownThreshold.toInt().toHex(2)}FF".toBytes()
            )

            for (cmd in cmdList) {
                BtHelper.instance.sendByteToDevice(socket!!, cmd) {
                    it.fold(
                        {
                            Log.i(TAG, "clickSaveSetting: ${it.toHexStr()}")
                        },
                        {
                            Log.e(TAG, "clickSaveSetting: ", it)
                        }
                    )
                }
            }
        }

    }

    private fun onAvailableInductionChange(value: Boolean) {
        viewStates = viewStates.copy(availableInduction = value)
    }

    private fun onRssiThresholdChange(value: String) {
        if (value.isDigitsOnly()) {
            viewStates = viewStates.copy(rssiThreshold = value)
        }
    }

    private fun onScanningTimeChange(value: String) {
        if (value.isDigitsOnly()) {
            viewStates = viewStates.copy(scanningTime = value)
        }
    }

    private fun onShutdownThresholdChange(value: String) {
        if (value.isDigitsOnly()) {
            viewStates = viewStates.copy(shutdownThreshold = value)
        }
    }

    private fun onTriggerUnlockChange(value: Boolean) {
        viewStates = viewStates.copy(triggerUnlock = value)
    }

    private fun toggleSettingView(isShow: Boolean) {
        viewStates = viewStates.copy(isShowSettingView = isShow)
        if (isShow) readState(true)
    }

    private fun readState(isOnlySetting: Boolean = false) {
        if (isOnlySetting) viewStates = viewStates.copy(isReadSettingState = true)
        viewModelScope.launch {
            BtHelper.instance.sendByteToDevice(socket!!, byteArrayOf(if (isOnlySetting) 9 else 8)) {
                it.fold(
                    {
                        Log.i(TAG, "changePowerState: ${it.toHexStr()}")
                    },
                    {
                        Log.e(TAG, "changePowerState: ", it)
                    }
                )
            }
        }
    }

    private fun changePowerState(isOn: Boolean) {
        viewModelScope.launch {
            val value: Byte = if (isOn) 1 else 2
            BtHelper.instance.sendByteToDevice(socket!!, byteArrayOf(value)) {
                it.fold(
                    {
                        Log.i(TAG, "changePowerState: ${it.toHexStr()}")
                    },
                    {
                        Log.e(TAG, "changePowerState: ", it)
                    }
                )
            }
        }
    }

    private fun onClickButton(index: ButtonIndex, action: ButtonAction) {
        val sendValue: Byte = when (index) {
            ButtonIndex.Lock -> {
                if (action == ButtonAction.Down) 101
                else 102
            }
            ButtonIndex.Unlock -> {
                if (action == ButtonAction.Down) 103
                else 104
            }
            ButtonIndex.Loop -> {
                if (action == ButtonAction.Down) 105
                else 106
            }
        }

        viewModelScope.launch {
            BtHelper.instance.sendByteToDevice(socket!!, byteArrayOf(sendValue)) {
                it.fold(
                    {
                        Log.i(TAG, "seed successful: byte= ${it.toHexStr()}")
                    },
                    {
                        Log.e(TAG, "seed fail", it)
                    }
                )
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
                        onReceivedMsg(it, device)
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

    @SuppressLint("MissingPermission")
    private fun onReceivedMsg(bluetoothSocket: BluetoothSocket, device: BluetoothDevice) {
        socket = bluetoothSocket

        viewModelScope.launch {
            if (socket != null) {
                BtHelper.instance.startBtReceiveServer(socket!!, onReceive = { numBytes, byteBufferArray ->
                    if (numBytes > 0) {
                        val contentArray = byteBufferArray.sliceArray(0..numBytes)
                        val contentText = contentArray.toText()

                        Log.i(TAG, "connectDevice: rev：numBytes=$numBytes, " +
                                "\nbyteBuffer(hex)=${contentArray.toHexStr()}, " +
                                "\nbyteBuffer(ascii)=$contentText"
                        )

                        viewStates = viewStates.copy(logText = "${viewStates.logText}\n$contentText")

                        if (contentText.length > 6 && contentText.slice(0..2) == "Set") {
                            Log.i(TAG, "connectDevice: READ from setting")
                            val setList = contentText.split(",")
                            viewStates = viewStates.copy(
                                availableInduction = setList[1] != "0",
                                triggerUnlock = setList[2] != "0",
                                scanningTime = setList[3],
                                rssiThreshold = setList[4],
                                shutdownThreshold = setList[5],
                                isReadSettingState = false
                            )
                        }
                    }
                })
            }
        }

        viewStates = viewStates.copy(
            connectState = ConnectState.AlreadyConnect,
            pairedDevices = setOf(),
            title = device.name
        )
    }
}

data class HomeStates(
    val initTip: String = "正在初始化蓝牙中",
    val connectState: ConnectState = ConnectState.NotConnect,
    val pairedDevices: Set<BluetoothDevice> = setOf(),
    val connectDevice: BluetoothDevice? = null,
    val title: String = "Auto controller",
    val logText: String = "",
    val isShowSettingView: Boolean = false,
    val isReadSettingState: Boolean = true,
    val availableInduction: Boolean = true,
    val triggerUnlock: Boolean = true,
    val scanningTime: String = "5",
    val rssiThreshold: String = "100",
    val shutdownThreshold: String = "1"
)

sealed class HomeAction {
    object ClickPowerOn: HomeAction()
    object ClickPowerOff: HomeAction()
    object ClickSaveSetting: HomeAction()
    data class ClickReadState(val readSettingOnly: Boolean = false): HomeAction()
    data class ClickBack(val activity: Activity?): HomeAction()
    data class InitBt(val context: Context): HomeAction()
    data class ConnectDevice(val device: BluetoothDevice) : HomeAction()
    data class OnClickButton(val index: ButtonIndex, val action: ButtonAction) : HomeAction()
    data class ToggleSettingView(val isShow: Boolean): HomeAction()
    data class OnAvailableInductionChange(val value: Boolean): HomeAction()
    data class OnTriggerUnlockChange(val value: Boolean): HomeAction()
    data class OnScanningTimeChange(val value: String): HomeAction()
    data class OnRssiThresholdChange(val value: String): HomeAction()
    data class OnShutdownThresholdChange(val value: String): HomeAction()
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