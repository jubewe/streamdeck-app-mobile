package com.example.streamdeck

import android.os.Handler
import android.util.Log
import android.widget.EditText
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streamdeck.ble.scanTimeout
import com.example.streamdeck.ble.stopBLEScan

val modifierKeys = listOf(
    Pair("KEY_LEFT_CTRL", R.string.key_ctrl_left),
    Pair("KEY_RIGHT_CTRL", R.string.key_right_ctrl),
    Pair("KEY_LEFT_SHIFT", R.string.key_shift_left),
    Pair("KEY_RIGHT_SHIFT", R.string.key_right_shift),
    Pair("KEY_LEFT_ALT", R.string.key_left_alt),
    Pair("KEY_RIGHT_ALT", R.string.key_right_alt),
    Pair("KEY_LEFT_GUI", R.string.key_left_gui),
    Pair("KEY_RIGHT_GUI", R.string.key_right_gui))
val mediaKeys = listOf(
    Pair("KEY_MEDIA_NEXT_TRACK", R.string.key_media_next_track),
    Pair("KEY_MEDIA_PREVIOUS_TRACK", R.string.key_media_previous_track),
//    Pair("KEY_MEDIA_STOP", R.string.key_media_stop),
    Pair("KEY_MEDIA_PLAY_PAUSE", R.string.key_media_play_pause),
    Pair("KEY_MEDIA_MUTE", R.string.key_media_mute),
    Pair("KEY_MEDIA_VOLUME_UP", R.string.key_media_volume_up),
    Pair("KEY_MEDIA_VOLUME_DOWN", R.string.key_media_volume_down),
    Pair("KEY_MEDIA_LOCAL_MACHINE_BROWSER", R.string.key_media_local_machine_browser),
    Pair("KEY_MEDIA_CALCULATOR", R.string.key_media_calculator),
    Pair("KEY_MEDIA_CONSUMER_CONTROL_CONFIGURATION", R.string.key_media_consumer_control_configuration),
    Pair("KEY_MEDIA_EMAIL_READER", R.string.key_media_email_reader))
val otherKeys = listOf(
    Pair("KEY_UP_ARROW", R.string.key_up_arrow),
    Pair("KEY_DOWN_ARROW", R.string.key_down_arrow),
    Pair("KEY_LEFT_ARROW", R.string.key_left_arrow),
    Pair("KEY_RIGHT_ARROW", R.string.key_right_arrow),
    Pair("KEY_BACKSPACE", R.string.key_backspace),
    Pair("KEY_TAB", R.string.key_tab),
    Pair("KEY_RETURN", R.string.key_return),
    Pair("KEY_ESC", R.string.key_esc),
    Pair("KEY_INSERT", R.string.key_insert),
    Pair("KEY_PRTSC", R.string.key_prtsc),
    Pair("KEY_DELETE", R.string.key_delete),
    Pair("KEY_PAGE_UP", R.string.key_page_up),
    Pair("KEY_PAGE_DOWN", R.string.key_page_down),
    Pair("KEY_HOME", R.string.key_home),
    Pair("KEY_END", R.string.key_end),
    Pair("KEY_CAPS_LOCK", R.string.key_caps_lock))
val functionKeys = listOf(
    Pair("KEY_F1", R.string.key_f1),
    Pair("KEY_F2", R.string.key_f2),
    Pair("KEY_F3", R.string.key_f3),
    Pair("KEY_F4", R.string.key_f4),
    Pair("KEY_F5", R.string.key_f5),
    Pair("KEY_F6", R.string.key_f6),
    Pair("KEY_F7", R.string.key_f7),
    Pair("KEY_F8", R.string.key_f8),
    Pair("KEY_F9", R.string.key_f9),
    Pair("KEY_F10", R.string.key_f10),
    Pair("KEY_F11", R.string.key_f11),
    Pair("KEY_F12", R.string.key_f12),
    Pair("KEY_F13", R.string.key_f13),
    Pair("KEY_F14", R.string.key_f14),
    Pair("KEY_F15", R.string.key_f15),
    Pair("KEY_F16", R.string.key_f16),
    Pair("KEY_F17", R.string.key_f17),
    Pair("KEY_F18", R.string.key_f18),
    Pair("KEY_F19", R.string.key_f19),
    Pair("KEY_F20", R.string.key_f20),
    Pair("KEY_F21", R.string.key_f21),
    Pair("KEY_F22", R.string.key_f22),
    Pair("KEY_F23", R.string.key_f23),
    Pair("KEY_F24", R.string.key_f24))
val numPadKeys = listOf(
    Pair("KEY_NUM_1", R.string.key_num_1),
    Pair("KEY_NUM_2", R.string.key_num_2),
    Pair("KEY_NUM_3", R.string.key_num_3),
    Pair("KEY_NUM_SLASH", R.string.key_num_slash),
    Pair("KEY_NUM_4", R.string.key_num_4),
    Pair("KEY_NUM_5", R.string.key_num_5),
    Pair("KEY_NUM_6", R.string.key_num_6),
    Pair("KEY_NUM_ASTERISK", R.string.key_num_asterisk),
    Pair("KEY_NUM_7", R.string.key_num_7),
    Pair("KEY_NUM_8", R.string.key_num_8),
    Pair("KEY_NUM_9", R.string.key_num_9),
    Pair("KEY_NUM_MINUS", R.string.key_num_minus),
    Pair("KEY_NUM_0", R.string.key_num_0),
    Pair("KEY_NUM_ENTER", R.string.key_num_enter),
    Pair("KEY_NUM_PERIOD", R.string.key_num_period),
    Pair("KEY_NUM_PLUS", R.string.key_num_plus)
)

var selectedKeysString by mutableStateOf("")
var selectedCharKey by mutableStateOf<Char?>(null)
var clipboardSelected by mutableStateOf(false)
var selectedKeysStringEncoder by mutableStateOf("")
var selectedCharKeyEncoder by mutableStateOf<Char?>(null)

data class KeyType (val list: List<Pair<String, Int>>, val titleId: Int, val span: Int)

var keyTypes = listOf(
    KeyType(modifierKeys, R.string.modifier_keys, 2),
    KeyType(mediaKeys, R.string.media_keys, 2),
    KeyType(otherKeys, R.string.other_keys, 2),
    KeyType(functionKeys, R.string.function_keys, 1),
    KeyType(numPadKeys, R.string.numpad_keys, 1)
)
var keyTypesEncoder = listOf(
    KeyType(mediaKeys, R.string.media_keys, 2),
    KeyType(otherKeys, R.string.other_keys, 2),
    KeyType(functionKeys, R.string.function_keys, 1),
    KeyType(numPadKeys, R.string.numpad_keys, 1)
)
var keyTypeExpanded by mutableStateOf<Int?>(0)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyCombinationConfig() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 64.dp),
        modifier = Modifier
            .fillMaxSize()
    ){
        keyTypes.forEachIndexed(){
            index, keyType->
            item(span = { GridItemSpan(maxCurrentLineSpan) }){
                var itemSelected by remember { mutableStateOf(false) }
                LaunchedEffect(selectedKeysString) {
                    itemSelected = false
                    Handler().postDelayed({
                    selectedKeysString.split('+').forEach() {
                        keyType.list.forEach { (key, stringId) ->
                            if(key == it.removeSurrounding("+")){
                                itemSelected = true
                            }
                        }
                    }}, 1000)
            }

                KeyHeader(title = stringResource(id = keyType.titleId), itemSelected, isExpanded = keyTypeExpanded == index) {
                    keyTypeExpanded = if(keyTypeExpanded == index){
                        null
                    }else{
                        index
                    }
                }
            }
            keyType.list.forEach { (key, stringId) ->
                item (span = { GridItemSpan(keyType.span) }){
                    var selected by remember {
                        mutableStateOf(false)
                    }
                    LaunchedEffect(selectedKeysString){
                        selected = false
                        selectedKeysString.split('+').forEach() {
                           if (key == it.removeSurrounding("+")) {
                                 selected = true
                                 Log.d(key, selected.toString())
                           }
                        }
                    }


                    AnimatedVisibility(keyTypeExpanded == index, label = "",
                        enter = fadeIn(),
                        exit = fadeOut(),
                        ) {
                        Log.e("test: "+key, selected.toString())
                        FilterChip(selected, modifier = Modifier.padding(horizontal = 2.dp),
                            onClick = {
                                selected = !selected
                                if (selected) {
                                    selectedKeysString += "${if (selectedKeysString.isNotEmpty() && selectedKeysString[selectedKeysString.lastIndex] != '+') "+" else ""}$key+"
                                } else {
                                    selectedKeysString = selectedKeysString.replace("$key+", "")
                                }
                                Log.d("selectedKeys", selectedKeysString)
                            }, label = { Text(stringResource(id = stringId), textAlign = TextAlign.Center, modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = if (!selected) 8.dp else 0.dp)) },
                            leadingIcon = {
                                if (selected) {
                                    Icon(
                                        imageVector =
                                        Icons.Rounded.Check,
                                        contentDescription = null,
                                        Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                            )
                        )

                            //KeyFilterChip(key, stringId, selected) {

                            //}
                    }
                }
            }
        }

        item(span = { GridItemSpan(maxCurrentLineSpan) }){
            val index = keyTypes.size+1
            KeyHeader(title = stringResource(id = R.string.character_key), selectedCharKey != null, isExpanded = keyTypeExpanded == index) {
                keyTypeExpanded = if(keyTypeExpanded == index){
                    null
                }else{
                    index
                }
            }
        }
        item (span = { GridItemSpan(maxCurrentLineSpan) }){
            AnimatedVisibility(keyTypeExpanded == keyTypes.size+1, label = "",
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Absolute.SpaceEvenly) {
                    OutlinedTextField(
                        modifier = Modifier.width(50.dp),
                        textStyle = TextStyle(fontWeight = FontWeight.Normal, textAlign = TextAlign.Center, fontSize = 20.sp),
                        value = if (selectedCharKey != null) selectedCharKey else {
                            ""
                        }.toString(),
                        onValueChange = {
                            selectedCharKey = try {
                                it.toCharArray()[it.length - 1]
                            } catch (e: ArrayIndexOutOfBoundsException) {
                                null
                            }
                        },
                        singleLine = true,
                    )
                    OutlinedButton(modifier = Modifier
                        .width(128.dp)
                        .padding(start = 8.dp),
                        onClick = { selectedCharKey = null },
                        enabled = selectedCharKey != null) {
                        Text(stringResource(id = androidx.constraintlayout.widget.R.string.abc_menu_delete_shortcut_label))
                    }
                }
            }
        }



    }
}

@Composable
fun KeyHeader(title: String, itemSelected: Boolean, isExpanded: Boolean, onHeaderClicked: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onHeaderClicked() },
        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
        ){
        Row(Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically){
            Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
            AnimatedVisibility(visible = itemSelected, enter = scaleIn(), exit = scaleOut()) {
                Box(
                    Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .size(8.dp)){}
            }
        }
        AnimatedContent(targetState = isExpanded, label = "") {
            if(it){
                Icon(Icons.Rounded.KeyboardArrowDown, null)
            }else{
                Icon(Icons.Rounded.KeyboardArrowRight, null)
            }
        }

    }
}


fun modifyEncoderKeysString(mode: Int, keyString: String){
    var newString = ""
    if(selectedKeysStringEncoder.indexOfLast{ it == '+' }==-1){
        selectedKeysStringEncoder = " + + "
    }
    selectedKeysStringEncoder.split('+').forEachIndexed { index, oldKeyString ->
        newString = if(mode == index){
            keyString
        }else{
            oldKeyString
        } + "+"
    }
    selectedKeysStringEncoder = newString.removeSuffix("+")
    Log.e("modified", selectedKeysStringEncoder)
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyCombinationConfigEncoder(mode: Int) {
    //modes: 0 -> cw, 1 -> ccw, 2 -> press
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 64.dp),
        modifier = Modifier
            .fillMaxSize()
    ){
        keyTypesEncoder.forEachIndexed(){
                index, keyType->
            item(span = { GridItemSpan(maxCurrentLineSpan) }){
                var itemSelected by remember { mutableStateOf(false) }
                LaunchedEffect(selectedKeysStringEncoder) {
                    itemSelected = false
                    Handler().postDelayed({
                        selectedKeysStringEncoder.split('+').forEachIndexed() { index, it->
                            if(index == mode) {
                                keyType.list.forEach { (key, stringId) ->
                                    if (key == it.removeSurrounding("+")) {
                                        itemSelected = true
                                    }
                                }
                            }
                        }}, 1000)

                }

                KeyHeader(title = stringResource(id = keyType.titleId), itemSelected, isExpanded = keyTypeExpanded == index) {
                    keyTypeExpanded = if(keyTypeExpanded == index){
                        null
                    }else{
                        index
                    }
                }
            }
            keyType.list.forEach { (key, stringId) ->
                item (span = { GridItemSpan(keyType.span) }){
                    var selected by remember {
                        mutableStateOf(false)
                    }
                    LaunchedEffect(selectedKeysStringEncoder){
                        selected = false
                        selectedKeysStringEncoder.split('+').forEach() {
                            if (key == it.removeSurrounding("+")) {
                                selected = true
                                Log.d(key, selected.toString())
                            }
                        }
                    }


                    AnimatedVisibility(keyTypeExpanded == index, label = "",
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Log.e("test: "+key, selected.toString())
                        FilterChip(selected, modifier = Modifier.padding(horizontal = 2.dp),
                            onClick = {
                                selected = !selected
                                if (selected) {
                                    selectedKeysStringEncoder += "${if (selectedKeysStringEncoder.isNotEmpty() && selectedKeysStringEncoder[selectedKeysStringEncoder.lastIndex] != '+') "+" else ""}$key+"
                                } else {
                                    selectedKeysStringEncoder = selectedKeysStringEncoder.replace("$key+", "")
                                }
                                Log.d("selectedKeys", selectedKeysStringEncoder)
                            }, label = { Text(stringResource(id = stringId), textAlign = TextAlign.Center, modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = if (!selected) 8.dp else 0.dp)) },
                            leadingIcon = {
                                if (selected) {
                                    Icon(
                                        imageVector =
                                        Icons.Rounded.Check,
                                        contentDescription = null,
                                        Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        //KeyFilterChip(key, stringId, selected) {

                        //}
                    }
                }
            }
        }

        item(span = { GridItemSpan(maxCurrentLineSpan) }){
            val index = keyTypes.size+1
            KeyHeader(title = stringResource(id = R.string.character_key), selectedCharKeyEncoder != null, isExpanded = keyTypeExpanded == index) {
                keyTypeExpanded = if(keyTypeExpanded == index){
                    null
                }else{
                    index
                }
            }
        }
        item (span = { GridItemSpan(maxCurrentLineSpan) }){
            AnimatedVisibility(keyTypeExpanded == keyTypes.size+1, label = "",
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Absolute.SpaceEvenly) {
                    OutlinedTextField(
                        modifier = Modifier.width(50.dp),
                        textStyle = TextStyle(fontWeight = FontWeight.Normal, textAlign = TextAlign.Center, fontSize = 20.sp),
                        value = if (selectedCharKeyEncoder != null) selectedCharKeyEncoder else {
                            ""
                        }.toString(),
                        onValueChange = {
                            selectedCharKeyEncoder = try {
                                it.toCharArray()[it.length - 1]
                            } catch (e: ArrayIndexOutOfBoundsException) {
                                null
                            }
                        },
                        singleLine = true,
                    )
                    OutlinedButton(modifier = Modifier
                        .width(128.dp)
                        .padding(start = 8.dp),
                        onClick = { selectedCharKeyEncoder = null },
                        enabled = selectedCharKeyEncoder != null) {
                        Text(stringResource(id = androidx.constraintlayout.widget.R.string.abc_menu_delete_shortcut_label))
                    }
                }
            }
        }



    }
}
