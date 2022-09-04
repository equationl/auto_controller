package com.equationl.autocontroller.view

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.MotionEvent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.equationl.autocontroller.utils.BtHelper
import com.equationl.autocontroller.viewModel.*

@Composable
fun HomeView(viewModel: HomeViewModel) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        viewModel.dispatch(HomeAction.InitBt(context))

        onDispose {  }
    }

    val viewStates = viewModel.viewStates

    if (viewStates.connectState == ConnectState.AlreadyConnect) {
        HomeScreen(viewModel)
    }
    else {
        if (viewStates.pairedDevices.isEmpty()) {
            HomeInit(viewModel)
        }
        else {
            HomeConnect(
                viewStates.pairedDevices,
                viewStates.connectState,
                viewStates.connectDevice
                ) {
                viewModel.dispatch(HomeAction.ConnectDevice(it))
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column {
            Button(
                onClick = { },
                modifier = Modifier.presBtn {
                    // fixme test
                    viewModel.dispatch(HomeAction.OnClickButton(ButtonIndex.Lock, it))
                }
            ) {
                Text(text = "上锁")
            }

            Button(onClick = {/*TODO*/ }) {
                Text(text = "解锁")
            }

            Button(onClick = { /*TODO*/ }) {
                Text(text = "多功能️")
            }

            Button(onClick = { /*TODO*/ }) {
                Text(text = "设置")
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun HomeConnect(
    pairDevices: Set<BluetoothDevice>,
    connectState: ConnectState,
    connectDevice: BluetoothDevice?,
    onClickItem: (device: BluetoothDevice) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Column {
                    Text(text = "请选择一个已配对设备进行连接")
                    Text(text = "如果设备尚未配对请从系统设置中配对")
                }
            }

            LazyColumn {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text(text = "已配对设备：")
                    }
                }

                pairDevices.forEach { item ->
                    item(key = item) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .clickable { onClickItem(item) },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = item.name)
                            Text(text = item.address)
                        }
                        Divider()
                    }
                }
            }
        }
        if (connectState == ConnectState.Connecting) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(modifier = Modifier.size(200.dp)) {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "正在连接至 ${connectDevice?.name}")
                    }
                }
            }
        }
    }

}

@Composable
fun HomeInit(viewModel: HomeViewModel) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        Text(text = viewModel.viewStates.initTip)
        Text(text = "点击刷新",
            Modifier
                .padding(16.dp)
                .clickable {
                    viewModel.dispatch(HomeAction.InitBt(context))
                })
    }
}

@OptIn(ExperimentalComposeUiApi::class)
inline fun Modifier.presBtn(crossinline onPress: (btnAction: ButtonAction)->Unit): Modifier = composed {

    pointerInteropFilter {
        when (it.action) {
            MotionEvent.ACTION_DOWN -> {
                onPress(ButtonAction.Down)
            }
            MotionEvent.ACTION_UP -> {
                onPress(ButtonAction.Up)
            }
        }
        true
    }

}

@Preview(showSystemUi = true)
@Composable
fun PreviewHomeScreen() {
    HomeView(viewModel = HomeViewModel())
}