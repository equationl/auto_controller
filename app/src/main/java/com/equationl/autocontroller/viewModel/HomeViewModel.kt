package com.equationl.autocontroller.viewModel

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.equationl.autocontroller.utils.BtHelper

class HomeViewModel: ViewModel() {
    var viewStates by mutableStateOf(HomeStates())
        private set

    fun dispatch(action: HomeAction) {
        when (action) {
            is HomeAction.InitBt -> initBt()
        }
    }

    private fun initBt() {
        // TODO
        viewStates = viewStates.copy(
            isConnected = false,
            pairedDevices = BtHelper.instance.queryPairDevices() ?: setOf()
        )
    }
}

data class HomeStates(
    val isConnected: Boolean = false,
    val pairedDevices: Set<BluetoothDevice> = setOf()
)

sealed class HomeAction {
    object InitBt: HomeAction()
}