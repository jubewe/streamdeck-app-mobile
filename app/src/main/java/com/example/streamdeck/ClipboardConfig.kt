package com.example.streamdeck

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.widget.R

var clipboardString by mutableStateOf("")
@Composable
fun ClipboardConfig() {
    Row(Modifier.fillMaxSize().padding(top = 16.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.Absolute.SpaceEvenly) {
        OutlinedTextField(
            textStyle = TextStyle(fontWeight = FontWeight.Normal, textAlign = TextAlign.Center, fontSize = 20.sp),
            value = clipboardString,
            onValueChange = {
                if(it.length < 490) { //MTU limit
                    clipboardString = it
                }
            },
        )
    }
}