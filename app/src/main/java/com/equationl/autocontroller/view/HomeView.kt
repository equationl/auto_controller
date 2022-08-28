package com.equationl.autocontroller.view

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.equationl.autocontroller.utils.BtHelper
import com.equationl.autocontroller.viewModel.HomeAction
import com.equationl.autocontroller.viewModel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeView(viewModel: HomeViewModel) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        BtHelper.instance.init(context)
        viewModel.dispatch(HomeAction.InitBt)

        onDispose {  }
    }

    val viewStates = viewModel.viewStates

    if (viewStates.isConnected) {
        HomeScreen()
    }
    else {
        if (viewStates.pairedDevices.isEmpty()) {
            HomeInit()
        }
        else {
            HomeConnect(
                viewStates.pairedDevices
            ) {
                /*TODO*/
                BtHelper.instance.connectDevice(it)
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { /*TODO*/ }) {
            Text(text = "🔒")
        }

        Button(onClick = { /*TODO*/ }) {
            Text(text = "🔓")
        }

        Button(onClick = { /*TODO*/ }) {
            Text(text = "⭕️")
        }
    }
}

@Composable
fun HomeConnect(
    pairDevices: Set<BluetoothDevice>,
    onClickItem: (device: BluetoothDevice) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(text = "请选择一个已配对设备进行连接")
            Text(text = "如果设备尚未配对请从系统设置中配对")
        }

        LazyColumn {
            pairDevices.forEach { item ->
                item(key = item) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClickItem(item) },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = item.name)
                        Text(text = item.address)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeInit() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "正在初始化蓝牙中")
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewHomeScreen() {
    HomeView(viewModel = HomeViewModel())
}