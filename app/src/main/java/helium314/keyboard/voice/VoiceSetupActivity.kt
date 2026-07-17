// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.voice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import helium314.keyboard.latin.R
import helium314.keyboard.latin.utils.Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VoiceSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Theme {
                Surface(Modifier.fillMaxSize()) {
                    VoiceSetupScreen()
                }
            }
        }
    }
}

@Composable
private fun VoiceSetupScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var paired by remember { mutableStateOf(VoiceIdentity.isEnrolled(context)) }
    var pairingCode by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember {
        mutableStateOf(context.getString(if (paired) R.string.voice_pairing_ready else R.string.voice_pairing_needed))
    }
    var microphoneGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        microphoneGranted = it
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(stringResource(R.string.voice_setup_title))
        Text(status)
        OutlinedTextField(
            value = pairingCode,
            onValueChange = {
                pairingCode = it.filterNot(Char::isWhitespace).take(16).uppercase()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.voice_pairing_code)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            enabled = !busy && !paired,
        )
        Button(
            onClick = {
                busy = true
                status = context.getString(R.string.voice_pairing_working)
                scope.launch {
                    val result = runCatching {
                        withContext(Dispatchers.IO) { VoiceApi.enroll(context, pairingCode) }
                    }
                    paired = result.isSuccess
                    status = context.getString(
                        if (paired) R.string.voice_pairing_ready else R.string.voice_pairing_failed,
                    )
                    busy = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy && !paired && pairingCode.length >= 6,
        ) { Text(stringResource(R.string.voice_pair_button)) }

        Button(
            onClick = { permission.launch(Manifest.permission.RECORD_AUDIO) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !microphoneGranted,
        ) {
            Text(
                stringResource(
                    if (microphoneGranted) R.string.voice_microphone_granted
                    else R.string.voice_microphone_grant,
                ),
            )
        }
        Spacer(Modifier.height(4.dp))
        TextButton(
            onClick = { context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.voice_open_keyboard_settings)) }
        TextButton(
            onClick = { context.getSystemService(InputMethodManager::class.java).showInputMethodPicker() },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.voice_choose_keyboard)) }
        Text(stringResource(R.string.voice_privacy_note))
    }
}
