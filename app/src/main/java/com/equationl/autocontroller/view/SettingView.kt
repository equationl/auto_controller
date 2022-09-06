package com.equationl.autocontroller.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.equationl.autocontroller.viewModel.HomeAction
import com.equationl.autocontroller.viewModel.HomeViewModel

@Composable
fun SettingView(viewModel: HomeViewModel) {
    val viewState = viewModel.viewStates

    if (viewState.isReadSettingState) {
        Text(text = "正在读取数据，请稍候")
    }
    else {
        SettingCard(viewModel = viewModel)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingCard(viewModel: HomeViewModel) {
    val viewState = viewModel.viewStates

    Card(Modifier.padding(8.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = viewState.availableInduction,
                    onCheckedChange = { viewModel.dispatch(HomeAction.OnAvailableInductionChange(it)) }
                )
                Text(text = "启用感应解锁")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = viewState.triggerUnlock,
                    onCheckedChange = { viewModel.dispatch(HomeAction.OnTriggerUnlockChange(it)) },
                    enabled = viewState.availableInduction
                )
                Text(text = "扫描到设备时是否触发解锁按键（避免钥匙感应解锁失效）")
            }
            OutlinedTextField(
                value = viewState.scanningTime,
                onValueChange = { viewModel.dispatch(HomeAction.OnScanningTimeChange(it)) },
                label = {
                    Text(text = "扫描时间（s）")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = viewState.availableInduction
            )
            OutlinedTextField(
                value = viewState.rssiThreshold,
                onValueChange = { viewModel.dispatch(HomeAction.OnRssiThresholdChange(it)) },
                label = {
                    Text(text = "解锁阈值（数字越高越敏感，感应距离越远）")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = viewState.availableInduction
            )
            OutlinedTextField(
                value = viewState.shutdownThreshold,
                onValueChange = { viewModel.dispatch(HomeAction.OnShutdownThresholdChange(it)) },
                label = {
                    Text(text = "扫描X次后无结果断开钥匙电源")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = viewState.availableInduction
            )

            Row {
                Button(
                    onClick = {
                        viewModel.dispatch(HomeAction.ClickSaveSetting)
                    },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(text = "保存")
                }
                Button(
                    onClick = {
                        viewModel.dispatch(HomeAction.ClickReadState(true))
                    }
                ) {
                    Text(text = "读取")
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewSetting() {
    SettingView(HomeViewModel())
}