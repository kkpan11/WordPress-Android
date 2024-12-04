package org.wordpress.android.ui.prefs.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale
import javax.inject.Inject

class LocaleHelper @Inject constructor() {
    private fun getCurrentLocale(): Locale {
        val locales = getApplicationLocaleList()
        return if (locales.isEmpty || locales == LocaleListCompat.getEmptyLocaleList()) {
            Locale.getDefault()
        } else {
            locales[0] ?: Locale.getDefault()
        }
    }

    fun getCurrentLocaleDisplayName(): String = getCurrentLocale().displayName

    /**
     * Important: this should only be called after Activity.onCreate()
     * https://developer.android.com/reference/androidx/appcompat/app/AppCompatDelegate#getApplicationLocales()
     */
    private fun getApplicationLocaleList() = AppCompatDelegate.getApplicationLocales()

    // Useful during testing to clear the stored app locale
    @Suppress("unused")
    private fun resetApplicationLocale() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }

    fun setCurrentLocaleByLanguageCode(languageCode: String) {
        // We shouldn't have to replace "_" with "-" but this is in order to work with our language picker
        val appLocale = LocaleListCompat.forLanguageTags(languageCode.replace("_", "-"))
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}
