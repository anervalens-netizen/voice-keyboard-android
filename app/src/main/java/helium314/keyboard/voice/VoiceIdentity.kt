// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.voice

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.UUID

object VoiceIdentity {
    private const val PREFS = "voice_dictation"
    private const val PREF_DEVICE_ID = "device_id"
    private const val PREF_ENROLLED = "enrolled"
    private const val KEY_ALIAS = "voice_keyboard_request_signing_v1"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun deviceId(context: Context): String {
        val preferences = prefs(context)
        preferences.getString(PREF_DEVICE_ID, null)?.let { return it }
        val id = UUID.randomUUID().toString()
        preferences.edit().putString(PREF_DEVICE_ID, id).apply()
        return id
    }

    fun deviceName(): String = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

    fun isEnrolled(context: Context): Boolean =
        prefs(context).getBoolean(PREF_ENROLLED, false) && hasKey()

    fun markEnrolled(context: Context) {
        prefs(context).edit().putBoolean(PREF_ENROLLED, true).apply()
    }

    @Synchronized
    fun publicKeyBase64(): String = Base64.encodeToString(
        ensureKey().certificate.publicKey.encoded,
        Base64.NO_WRAP,
    )

    @Synchronized
    fun signCanonical(canonical: String): String {
        val signer = Signature.getInstance("SHA256withECDSA")
        signer.initSign(ensureKey().privateKey)
        signer.update(canonical.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(
            signer.sign(),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
        )
    }

    private fun hasKey(): Boolean = keyStore().containsAlias(KEY_ALIAS)

    private fun ensureKey(): KeyStore.PrivateKeyEntry {
        val store = keyStore()
        (store.getEntry(KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry)?.let { return it }

        val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
        val builder = KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_SIGN)
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setUserAuthenticationRequired(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) builder.setUnlockedDeviceRequired(true)
        generator.initialize(builder.build())
        generator.generateKeyPair()
        return keyStore().getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
}
