// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin

import helium314.keyboard.latin.settings.Defaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceKeyboardDefaultsTest {
    @Test
    fun privateReleaseStaysAboveHistoricalHeliBoardMigrations() {
        assertTrue(BuildConfig.VERSION_CODE > 3901)
    }

    @Test
    fun portraitGeometryUsesCompactGapsAndPadding() {
        assertEquals(0.92f, Defaults.PREF_KEYBOARD_HEIGHT_SCALE[0])
        assertEquals(0.70f, Defaults.PREF_KEY_GAP_SCALE[0])
        assertEquals(0.25f, Defaults.PREF_BOTTOM_PADDING_SCALE[0])
    }
}
