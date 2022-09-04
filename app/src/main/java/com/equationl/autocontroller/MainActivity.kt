package com.equationl.autocontroller

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equationl.autocontroller.ui.theme.AutoControllerTheme
import com.equationl.autocontroller.view.HomeView
import com.equationl.autocontroller.viewModel.HomeAction
import com.equationl.autocontroller.viewModel.HomeViewModel

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "Main"
    }


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AutoControllerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: HomeViewModel = viewModel()
                    val activity = LocalContext.current as? Activity

                    MaterialTheme {
                        Scaffold(
                            topBar = {
                                SmallTopAppBar(
                                    title = {
                                        Text(text = viewModel.viewStates.title)
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            viewModel.dispatch(HomeAction.ClickBack(activity))
                                        }) {
                                            Icon(
                                                imageVector = Icons.Outlined.ArrowBack,
                                                contentDescription = "BACK"
                                            )
                                        }
                                    }
                                )
                            }
                        ) {
                            Column(Modifier.padding(it)) {
                                HomeView(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }

}