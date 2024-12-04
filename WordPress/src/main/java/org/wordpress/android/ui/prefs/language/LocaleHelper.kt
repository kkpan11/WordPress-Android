package org.wordpress.android.ui.prefs.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

class LocaleHelper {
    private fun getCurrentLocale(): Locale? = AppCompatDelegate.getApplicationLocales()[0]

    fun getCurrentLocaleDisplayName(): String = getCurrentLocale()?.displayName ?: ""

    fun setCurrentLocaleByLanguageCode(languageCode: String) {
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}
