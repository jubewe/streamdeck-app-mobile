package com.example.streamdeck

import android.util.Log
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.streamdeck.ble.configCharacteristic
import com.example.streamdeck.ble.writeCharacteristic
import kotlinx.coroutines.launch

@Composable
fun EncoderDialog() {
    writeCharacteristic(configCharacteristic, "g,${showEncoderDialogId!! + selectedPage*15}")
    AlertDialog(
        modifier = Modifier.fillMaxHeight(0.8f),
        onDismissRequest = { showEncoderDialogId = null },
        title = {
            Text(stringResource(id = R.string.modify_encoder, showEncoderDialogId!! + 1, selectedPage + 1))
        },
        text = {
            Column {
                RadioButton(selected = true, onClick = { /*TODO*/ })
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