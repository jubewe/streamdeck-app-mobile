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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.streamdeck.ble.configCharacteristic
import com.example.streamdeck.ble.writeCharacteristic
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EncoderDialog() {
    writeCharacteristic(configCharacteristic, "g,${-showEncoderDialogId!!}")
    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.8f),
        onDismissRequest = { showEncoderDialogId = null },
        title = {
            Text(stringResource(id = R.string.modify_encoder, showEncoderDialogId!!))
        },
        text = {
            val pagerState = rememberPagerState(pageCount = {
                3
            })
            val coroutineScope = rememberCoroutineScope()

            Column {
                TabRow(selectedTabIndex = pagerState.currentPage, tabs = {
                    Tab(pagerState.currentPage == 0, modifier = Modifier.clip(
                        RoundedCornerShape(12.dp)
                    ), onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }}){
                        val selected = pagerState.currentPage == 0
                        Icon(
                            painter = painterResource(id = R.drawable.round_rotate_left_24),
                            contentDescription = null,
                            modifier = Modifier.padding(vertical = 12.dp),
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary
                        )
                    }
                    Tab(pagerState.currentPage == 1, modifier = Modifier.clip(
                        RoundedCornerShape(12.dp)
                    )
                        ,onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }}) {
                        val selected = pagerState.currentPage == 1

                            Icon(
                                painter = painterResource(id = R.drawable.round_rotate_right_24),
                                contentDescription = null,
                                modifier = Modifier.padding(vertical = 12.dp),
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary
                            )


                    }
                    Tab(pagerState.currentPage == 2, modifier = Modifier.clip(
                        RoundedCornerShape(12.dp)
                    )
                        ,onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(2)
                            }}) {
                        val selected = pagerState.currentPage == 2

                        Icon(
                            painter = painterResource(id = R.drawable.round_keyboard_double_arrow_down_24),
                            contentDescription = null,
                            modifier = Modifier.padding(vertical = 12.dp),
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary
                        )

                    }
                })

                HorizontalPager(state = pagerState, userScrollEnabled = true, beyondBoundsPageCount = 1, modifier = Modifier
                    .padding(bottom = 0.dp, top = 8.dp)
                ) { page ->
                    KeyCombinationConfigEncoder(page)

                }

            }
        },
        confirmButton = {
            TextButton(onClick = {
                val value =
                    selectedKeysStringEncoder.removeSuffix("+")+
                            if(selectedCharKeyEncoder != null){
                                "+$selectedCharKeyEncoder"
                            } else {
                                ""
                            }
                val id = if(showEncoderDialogId != null) (-showEncoderDialogId!!).toString() else ""
                val string = "c,${id},${infoString},{{${value}}},${"0"}"
                Log.d("writeCharacteristic", string)
                writeCharacteristic(configCharacteristic, string)
                showEncoderDialogId = null }) {
                Text(stringResource(id = R.string.ok))
            }

        })
}