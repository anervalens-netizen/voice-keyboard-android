// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.voice

import android.content.Context
import android.util.Base64
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

object VoiceApi {
    private const val PUBLIC_BASE = "https://astra.astancu.eu/keyboard-stt"
    private const val TAILSCALE_BASE = "http://100.86.113.108:39300"
    private val endpoints = listOf(PUBLIC_BASE, TAILSCALE_BASE)

    fun enroll(context: Context, pairingCode: String) {
        val payload = JSONObject()
            .put("code", pairingCode.trim().uppercase())
            .put("deviceId", VoiceIdentity.deviceId(context))
            .put("deviceName", VoiceIdentity.deviceName())
            .put("publicKey", VoiceIdentity.publicKeyBase64())
            .toString()
            .toByteArray(Charsets.UTF_8)

        requestWithFallback { base ->
            val connection = open("$base/v1/enroll")
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.doOutput = true
            connection.setFixedLengthStreamingMode(payload.size)
            connection.outputStream.use { it.write(payload) }
            expectSuccess(connection)
        }
        VoiceIdentity.markEnrolled(context)
    }

    fun dictate(context: Context, audioFile: File): String {
        val audio = audioFile.readBytes()
        val deviceId = VoiceIdentity.deviceId(context)
        val requestId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis().toString()
        val nonceBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val nonce = Base64.encodeToString(
            nonceBytes,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
        )
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(audio)
            .joinToString("") { "%02x".format(it) }
        val language = "ro"
        val canonical = listOf(
            "POST", "/v1/dictate", deviceId, requestId, timestamp, nonce, digest, language,
        ).joinToString("\n")
        val signature = VoiceIdentity.signCanonical(canonical)
        val boundary = "voice-keyboard-${UUID.randomUUID()}"

        return requestWithFallback { base ->
            val connection = open("$base/v1/dictate", readTimeoutMs = 125_000)
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            connection.setRequestProperty("X-Device-Id", deviceId)
            connection.setRequestProperty("X-Request-Id", requestId)
            connection.setRequestProperty("X-Timestamp", timestamp)
            connection.setRequestProperty("X-Nonce", nonce)
            connection.setRequestProperty("X-Audio-Sha256", digest)
            connection.setRequestProperty("X-Language", language)
            connection.setRequestProperty("X-Signature", signature)
            connection.doOutput = true

            val prefix = ("--$boundary\r\n" +
                "Content-Disposition: form-data; name=\"audio\"; filename=\"dictation.m4a\"\r\n" +
                "Content-Type: audio/mp4\r\n\r\n").toByteArray(Charsets.UTF_8)
            val suffix = "\r\n--$boundary--\r\n".toByteArray(Charsets.UTF_8)
            connection.setFixedLengthStreamingMode(prefix.size + audio.size + suffix.size)
            BufferedOutputStream(connection.outputStream).use {
                it.write(prefix)
                it.write(audio)
                it.write(suffix)
            }
            JSONObject(expectSuccess(connection)).getString("text").trim()
        }
    }

    private fun open(url: String, readTimeoutMs: Int = 20_000): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = readTimeoutMs
            useCaches = false
            instanceFollowRedirects = false
            setRequestProperty("Accept", "application/json")
        }

    private fun expectSuccess(connection: HttpURLConnection): String {
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use {
                it.readText().take(65_536)
            }.orEmpty()
            if (status !in 200..299) throw VoiceHttpException(status)
            return body
        } finally {
            connection.disconnect()
        }
    }

    private inline fun <T> requestWithFallback(request: (String) -> T): T {
        var last: Exception? = null
        for (endpoint in endpoints) {
            try {
                return request(endpoint)
            } catch (error: VoiceHttpException) {
                if (error.status < 500) throw error
                last = error
            } catch (error: IOException) {
                last = error
            }
        }
        throw last ?: IOException("No dictation endpoint available")
    }

    private class VoiceHttpException(val status: Int) : IOException("Voice server returned HTTP $status")
}
