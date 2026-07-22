// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.voice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import helium314.keyboard.latin.LatinIME
import helium314.keyboard.latin.R
import helium314.keyboard.latin.settings.Settings
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

class VoiceDictationController(private val service: LatinIME) {
    private val main = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor()
    private var recorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var state = IDLE
    private var generation = 0

    fun toggle() {
        when (state) {
            RECORDING -> stopAndUpload()
            UPLOADING -> Unit
            else -> startRecording()
        }
    }

    fun consumeEnterTap(): Boolean = when (state) {
        RECORDING -> {
            stopAndUpload()
            true
        }
        UPLOADING -> true
        else -> false
    }

    fun cancel() {
        generation += 1
        if (state == RECORDING) stopRecorderSafely(save = false)
        state = IDLE
        service.setVoiceDictationState(IDLE)
    }

    fun cancelByUser() {
        if (state != RECORDING) return
        cancel()
        toast(R.string.voice_recording_cancelled)
    }

    fun destroy() {
        cancel()
        worker.shutdownNow()
    }

    private fun startRecording() {
        if (!VoiceIdentity.isEnrolled(service)) {
            toast(R.string.voice_not_paired)
            openSetup()
            return
        }
        if (ContextCompat.checkSelfPermission(service, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            toast(R.string.voice_permission_needed)
            openSetup()
            return
        }
        if (!Settings.getValues().mShowsVoiceInputKey) return

        val directory = File(service.cacheDir, "voice-dictation").apply { mkdirs() }
        val file = File(directory, "${UUID.randomUUID()}.m4a")
        val created = try {
            @Suppress("DEPRECATION")
            val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(service)
            } else {
                MediaRecorder()
            }
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder.setAudioChannels(1)
            mediaRecorder.setAudioSamplingRate(44_100)
            mediaRecorder.setAudioEncodingBitRate(64_000)
            mediaRecorder.setMaxDuration(180_000)
            mediaRecorder.setMaxFileSize(8L * 1024 * 1024)
            mediaRecorder.setOutputFile(file.absolutePath)
            mediaRecorder.setOnInfoListener { _, what, _ ->
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
                    what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED
                ) {
                    main.post { if (state == RECORDING) stopAndUpload() }
                }
            }
            mediaRecorder.prepare()
            mediaRecorder.start()
            recorder = mediaRecorder
            recordingFile = file
            true
        } catch (_: Exception) {
            recorder?.release()
            recorder = null
            file.delete()
            false
        }
        if (!created) {
            toast(R.string.voice_recording_failed)
            return
        }
        state = RECORDING
        generation += 1
        service.setVoiceDictationState(RECORDING)
        toast(R.string.voice_recording)
    }

    private fun stopAndUpload() {
        val file = recordingFile ?: return
        if (!stopRecorderSafely(save = true) || file.length() < 512) {
            file.delete()
            recordingFile = null
            state = IDLE
            service.setVoiceDictationState(IDLE)
            toast(R.string.voice_recording_failed)
            return
        }
        state = UPLOADING
        service.setVoiceDictationState(UPLOADING)
        toast(R.string.voice_uploading)
        val requestGeneration = generation
        worker.execute {
            val result = runCatching { VoiceApi.dictate(service, file) }
            file.delete()
            main.post {
                if (requestGeneration != generation || state != UPLOADING) return@post
                recordingFile = null
                state = IDLE
                service.setVoiceDictationState(IDLE)
                val text = result.getOrNull()?.trim().orEmpty()
                when {
                    result.isFailure -> toast(R.string.voice_transcription_failed)
                    text.isEmpty() -> toast(R.string.voice_empty_transcription)
                    service.isInputViewShown -> service.onTextInput("$text ")
                }
            }
        }
    }

    private fun stopRecorderSafely(save: Boolean): Boolean {
        val current = recorder ?: return false
        recorder = null
        val file = recordingFile
        val stopped = try {
            if (save) current.stop() else current.reset()
            true
        } catch (_: RuntimeException) {
            false
        } finally {
            current.release()
        }
        if (!save) {
            file?.delete()
            recordingFile = null
        }
        return stopped
    }

    private fun openSetup() {
        service.startActivity(
            Intent(service, VoiceSetupActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    private fun toast(message: Int) {
        Toast.makeText(service, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val IDLE = 0
        const val RECORDING = 1
        const val UPLOADING = 2
    }
}
