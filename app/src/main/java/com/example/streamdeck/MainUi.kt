package com.example.streamdeck

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.streamdeck.ble.configCharacteristic
import com.example.streamdeck.ble.disconnectDevice
import com.example.streamdeck.ble.startBLEScan
import com.example.streamdeck.ble.writeCharacteristic
import com.example.streamdeck.ui.theme.StreamdeckTheme
import kotlinx.coroutines.launch

var showKeyDialogId: Int? by mutableStateOf(null)
var selectedPage by mutableIntStateOf(0)
const val supportedPages = 3
var connected by mutableStateOf(false)
var scanning by mutableStateOf(false)
var connecting by mutableStateOf(false)
var showNotInConfigModeDialog by mutableStateOf(false)
var infoString by mutableStateOf("---")
var infoStringId by mutableIntStateOf(-1)
var holdKey by mutableStateOf(false)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainUi() {
    Column {
        TabRow(selectedTabIndex = selectedPage, tabs = {
            for(page in 0..<supportedPages){
                Tab(selectedPage == page, onClick = {selectedPage = page}){
                    Text(stringResource(id = R.string.tab_title, page+1), Modifier.padding(vertical = 12.dp))
                }
            }
        })
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
                                            if (connected) {
                                                showKeyDialogId = keyId
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (connected) {
                                            MaterialTheme.colorScheme.tertiary
                                        } else {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        }
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

        Divider(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        AnimatedContent(targetState = (scanning || connecting), label = "") { progressIndicator ->
            if (progressIndicator) {
                LinearProgressIndicator(
                    Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .padding(8.dp)
                )
            } else {
                AnimatedContent(targetState = connected, label = "") { connected ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (connected) {
                            Button(onClick = { disconnectDevice() }) {
                                Text(stringResource(id = R.string.connected))
                            }
                        } else {
                            Text(stringResource(id = R.string.not_connected))
                            Button(onClick = { startBLEScan() }) {
                                Text(stringResource(id = R.string.search))
                            }
                        }
                    }
                }
            }
        }
        if (showKeyDialogId != null) {
            infoString = ""
            writeCharacteristic(configCharacteristic, "g,${showKeyDialogId!! + selectedPage*15}")
            var showLengthWarning by remember {mutableStateOf(false)}
            val maxStringLength = 500
            //var selectedTab by remember{mutableStateOf(0)}
            AlertDialog(
                modifier = Modifier.fillMaxHeight(0.8f),
                onDismissRequest = { showKeyDialogId = null },
                title = {
                    Text(stringResource(id = R.string.modify_key, showKeyDialogId!! + 1, selectedPage + 1))
                },
                text = {
                    Column {
                        OutlinedTextField(value = infoString,
                            label = { Text(stringResource(id = R.string.title)) },
                            singleLine = true,
                            onValueChange = { s ->
                                if(s.length <= maxStringLength){
                                    infoString = s.filter { it != ',' }
                                } else {
                                    showLengthWarning = true
                                }
                            }
                        )
                        if(infoStringId != showKeyDialogId!! + selectedPage*15){
                            LinearProgressIndicator(Modifier.clip(CircleShape).fillMaxWidth())
                        }
                        /*
                        Switch(
                            checked = holdKey, onCheckedChange = { holdKey = !holdKey },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = MaterialTheme.colorScheme.secondary,
                                checkedThumbColor = MaterialTheme.colorScheme.onSecondary
                            )
                        )*/
                        val pagerState = rememberPagerState(pageCount = {
                            2
                        })
                        val coroutineScope = rememberCoroutineScope()
                        Row(Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Info, null)
                            Text(stringResource(id = R.string.info_combinations_clipboard))
                        }

                        TabRow(selectedTabIndex = pagerState.currentPage, tabs = {
                            Tab(pagerState.currentPage == 0, modifier = Modifier.clip(
                                RoundedCornerShape(12.dp)
                            ), onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }}){
                                val selected = pagerState.currentPage == 0
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center){
                                    if(selected){
                                        Icon(Icons.Rounded.Check, null)
                                    }
                                    Text(stringResource(id = R.string.tab_custom),
                                        Modifier.padding(vertical = 12.dp), color = if(selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary)
                                }
                                 }
                            Tab(pagerState.currentPage == 1, modifier = Modifier.clip(
                                RoundedCornerShape(12.dp)
                            )
                                ,onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }}) {
                                val selected = pagerState.currentPage == 1
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {

                                    if (selected) {
                                        Icon(Icons.Rounded.Check, null)
                                    }
                                    Text(
                                        stringResource(id = R.string.tab_clipboard),
                                        Modifier.padding(vertical = 12.dp),
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            }
                        })

                        HorizontalPager(state = pagerState, userScrollEnabled = true, beyondBoundsPageCount = 1, modifier = Modifier
                            .padding(bottom = 0.dp, top = 8.dp)
                        ) { page ->
                                when(page){
                                    0 -> {
                                        KeyCombinationConfig()
                                    }
                                    1 -> {
                                        ClipboardConfig()
                                    }
                                }
                        }
                        Divider(Modifier.fillMaxWidth(0.8f))



                    }

                },
                confirmButton = {
                    TextButton(onClick = {
                        Log.d("writeCharacteristic", "c,${showKeyDialogId},${infoString},${if(holdKey){"1"}else{"0"}}")
                        writeCharacteristic(configCharacteristic, "c,${showKeyDialogId!! + selectedPage*15},${infoString},${if(holdKey){"1"}else{"0"}}");
                        showKeyDialogId = null }) {
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