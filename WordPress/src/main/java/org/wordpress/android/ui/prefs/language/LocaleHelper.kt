package org.wordpress.android.ui.prefs.language

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import org.wordpress.android.R
import org.wordpress.android.ui.prefs.AppPrefsWrapper
import java.util.Locale
import javax.inject.Inject

/**
 * Helper class to manage per-app language preferences
 * https://developer.android.com/guide/topics/resources/app-languages
 */
class LocaleHelper @Inject constructor(
    private val appPrefsWrapper: AppPrefsWrapper
) {
    private fun getCurrentLocale(): Locale {
        return if (isApplicationLocaleEmpty()) {
            Locale.getDefault()
        } else {
            getApplicationLocaleList()[0] ?: Locale.getDefault()
        }
    }

    fun getCurrentLocaleDisplayName(): String = getCurrentLocale().displayName

    /**
     * Important: this should only be called after Activity.onCreate()
     * https://developer.android.com/reference/androidx/appcompat/app/AppCompatDelegate#getApplicationLocales()
     */
    private fun getApplicationLocaleList() = AppCompatDelegate.getApplicationLocales()

    private fun isApplicationLocaleEmpty(): Boolean {
        val locales = getApplicationLocaleList()
        return (locales.isEmpty || locales == LocaleListCompat.getEmptyLocaleList())
    }

    /*
     * Useful during testing to clear the system stored app locale
     */
    @Suppress("unused")
    fun resetApplicationLocale() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }

    fun setCurrentLocaleByLanguageCode(languageCode: String) {
        // We shouldn't have to replace "_" with "-" but this is in order to work with our language picker
        val appLocale = LocaleListCompat.forLanguageTags(languageCode.replace("_", "-"))
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    /**
     * Previously the app locale was stored in SharedPreferences, so here we migrate to AndroidX per-app language prefs
     */
    fun performMigrationIfNecessary(context: Context) {
        if (isApplicationLocaleEmpty()) {
            val languagePrefKey = context.getString(R.string.pref_key_language)
            val previousLanguage = appPrefsWrapper.prefs().getString(languagePrefKey, "")
            if (previousLanguage?.isNotEmpty() == true) {
                setCurrentLocaleByLanguageCode(previousLanguage)
                appPrefsWrapper.prefs().edit().remove(languagePrefKey).apply()
            }
        }
    }
}
