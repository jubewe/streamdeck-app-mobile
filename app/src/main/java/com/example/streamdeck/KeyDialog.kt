package com.example.streamdeck

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.streamdeck.ble.configCharacteristic
import com.example.streamdeck.ble.writeCharacteristic
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyDialog() {
    infoString = ""
    selectedKeysString = ""
    writeCharacteristic(configCharacteristic, "g,${showKeyDialogId!! + selectedPage*15}")
    var showLengthWarning by remember { mutableStateOf(false) }
    val maxStringLength = 490
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
                if(infoStringId != (showKeyDialogId!! + selectedPage*15).toString()){
                    LinearProgressIndicator(
                        Modifier
                            .clip(CircleShape)
                            .fillMaxWidth())
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

                LaunchedEffect(clipboardSelected){
                    pagerState.scrollToPage(if(clipboardSelected)1 else 0)
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Info, null)
                    Text(stringResource(id = R.string.info_combinations_clipboard))
                }


                TabRow(selectedTabIndex = pagerState.currentPage, tabs = {
                    Tab(pagerState.currentPage == 0, modifier = Modifier.clip(
                        RoundedCornerShape(12.dp)
                    ), onClick = {
                        clipboardSelected = false
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }}){
                        val selected = pagerState.currentPage == 0
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center){
                            if(selected){
                                Icon(Icons.Rounded.Check, null)
                            }
                            Text(
                                stringResource(id = R.string.tab_custom),
                                Modifier.padding(vertical = 12.dp), color = if(selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary)
                        }
                    }
                    Tab(pagerState.currentPage == 1, modifier = Modifier.clip(
                        RoundedCornerShape(12.dp)
                    )
                        ,onClick = {
                            clipboardSelected = true
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
                val value =
                    if (clipboardSelected){
                        clipboardString
                    }else{
                        selectedKeysString.removeSuffix("+")+
                        if(selectedCharKey != null){
                            "+$selectedCharKey"
                        } else {
                            ""
                        }
                    }
                val id = if(showKeyDialogId != null) showKeyDialogId!! + selectedPage*15 else if (showEncoderDialogId != null) "$showEncoderDialogId$encoderSelectedOption" else ""
                val string = "c,${id},${infoString},{{${value}}},${if(clipboardSelected){"1"}else{"0"}}"
                Log.d("writeCharacteristic", string)
                writeCharacteristic(configCharacteristic, string)
                showKeyDialogId = null }) {
                Text(stringResource(id = R.string.ok))
            }

        })
}

