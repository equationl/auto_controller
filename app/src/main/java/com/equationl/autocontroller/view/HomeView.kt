package com.equationl.autocontroller.view

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.equationl.autocontroller.viewModel.*

@Composable
fun HomeView(viewModel: HomeViewModel) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        viewModel.dispatch(HomeAction.InitBt(context))

        onDispose {  }
    }

    BackHandler {
        viewModel.dispatch(HomeAction.ClickBack(context as? Activity))
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
    val logScrollState = rememberScrollState()

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    viewModel.dispatch(HomeAction.ClickPowerOn)
                },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(text = "??????")
            }

            Button(
                onClick = {
                    viewModel.dispatch(HomeAction.ClickPowerOff)
                },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(text = "??????")
            }

            Button(
                onClick = {
                    viewModel.dispatch(HomeAction.ClickReadState()
                    )
                },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(text = "??????")
            }

            Button(
                onClick = {
                    viewModel.dispatch(HomeAction.ToggleSettingView(!viewModel.viewStates.isShowSettingView))
                }
            ) {
                Text(text = "??????")
            }
        }

        AnimatedVisibility(visible = viewModel.viewStates.isShowSettingView) {
            SettingView(viewModel = viewModel)

        }

        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { },
                modifier = Modifier.presBtn {
                    viewModel.dispatch(HomeAction.OnClickButton(ButtonIndex.Lock, it))
                }
            ) {
                Text(text = "??????")
            }

            Button(onClick = { },
                modifier = Modifier.presBtn {
                    viewModel.dispatch(HomeAction.OnClickButton(ButtonIndex.Unlock, it))
                }) {
                Text(text = "??????")
            }

            Button(onClick = { },
                modifier = Modifier.presBtn {
                    viewModel.dispatch(HomeAction.OnClickButton(ButtonIndex.Loop, it))
                }) {
                Text(text = "?????????")
            }
        }

        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.LightGray),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LaunchedEffect(Unit) {
                    logScrollState.animateScrollTo(0)
                }
                Text(text = "???????????????", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = viewModel.viewStates.logText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .verticalScroll(logScrollState, reverseScrolling = true)
                        .fillMaxWidth()
                        .padding(8.dp)
                )
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
                    Text(text = "??????????????????????????????????????????")
                    Text(text = "???????????????????????????????????????????????????")
                }
            }

            LazyColumn {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text(text = "??????????????????")
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
                        Text(text = "??????????????? ${connectDevice?.name}")
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
        Text(text = "????????????",
            Modifier
                .padding(16.dp)
                .clickable {
                    viewModel.dispatch(HomeAction.InitBt(context))
                })
    }
}

@SuppressLint("UnnecessaryComposedModifier")
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
    //val viewModel: MockViewModel = viewModel()
    //HomeScreen(viewModel)
}