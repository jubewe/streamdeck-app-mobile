package com.example.streamdeck

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.streamdeck.ui.theme.StreamdeckTheme

var showKeyDialogId: Int? by mutableStateOf(null)

var connected by mutableStateOf(false)
var scanning by mutableStateOf(false)
var showNotInConfigModeDialog by mutableStateOf(false)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUi() {
    Column {

        Card(
            Modifier
                .width(400.dp)
                .height(320.dp)
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Row(
                Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {}
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Card(
                        modifier = Modifier
                            .height(45.dp)
                            .width(128.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Streamdeck",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {}
            }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Row(
                    Modifier
                        .height(250.dp)
                        .width(400.dp)
                        .padding(4.dp),
                ) {
                    for (column in 0..4) {
                        Column(
                            Modifier
                                .fillMaxHeight()
                                .weight(1f)
                        ) {
                            for (row in 0..2) {
                                val keyId = (column + (row * 5))
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(2.dp)
                                        .clickable {
                                            showKeyDialogId = keyId
                                        }, colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Box(
                                        Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text((keyId + 1).toString())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (scanning) {
            LinearProgressIndicator(
                Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .padding(8.dp)
            )
        }
        if (showKeyDialogId != null) {
            AlertDialog(
                onDismissRequest = { showKeyDialogId = null },
                title = {
                    Text(stringResource(id = R.string.modify_key, showKeyDialogId!!+1))
                },
                text = {
                    Column {
                        var string by remember { mutableStateOf("-") }
                        var enableHold by remember { mutableStateOf(false) }
                        OutlinedTextField(value = string,
                            label = { Text("test") },
                            onValueChange = { s ->
                                Log.e("test", s)
                                string = s.filter { it != ',' }
                            }
                        )
                        Switch(checked = enableHold, onCheckedChange = {enableHold = !enableHold},
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.secondary, checkedThumbColor = MaterialTheme.colorScheme.onSecondary))
                        Text((showKeyDialogId!!+1).toString())
                    }

                },
                confirmButton = {
                    TextButton(onClick = { showKeyDialogId = null }) {
                        Text(stringResource(id = R.string.ok))
                    }

                })
        }

        if (showNotInConfigModeDialog) {
            AlertDialog(onDismissRequest = { showNotInConfigModeDialog = false },
                title = {
                    Text(
                        stringResource(id = R.string.not_in_config_mode_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(stringResource(id = R.string.not_in_config_mode_desc))
                },
                confirmButton = {
                    TextButton(onClick = { showNotInConfigModeDialog = false }) {
                        Text(stringResource(id = R.string.ok))
                    }
                })
        }
    }

}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    StreamdeckTheme {
        MainUi()
    }
}