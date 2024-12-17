package org.wordpress.android.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.text.TextUtils
import androidx.preference.PreferenceManager
import org.wordpress.android.R
import java.text.Collator
import java.util.Locale
import java.util.regex.Pattern

/**
 * Helper class for working with localized strings. Ensures updates to the users
 * selected language is properly saved and resources appropriately updated for the
 * android version.
 */
object LocaleManager {
    /**
     * Key used for saving the language selection to shared preferences.
     */
    private const val LOCALE_PREF_KEY_STRING: String = "language-pref"

    /**
     * Pattern to split a language string (to parse the language and region values).
     */
    private val LANGUAGE_SPLITTER: Pattern = Pattern.compile("_")

    /**
     * Activate the locale associated with the provided context.
     *
     * @param context The current context.
     */
    @JvmStatic
    fun setLocale(context: Context): Context {
        return updateResources(context, getLanguage(context))
    }

    /**
     * Compare the language for the current context with another language.
     *
     * @param language The language to compare
     * @return True if the languages are the same, else false
     */
    fun isSameLanguage(language: String): Boolean {
        val newLocale = languageLocale(language)
        return Locale.getDefault().toString() == newLocale.toString()
    }

    /**
     * If the user has selected a language other than the device default, return that
     * language code, else just return the device default language code.
     *
     * @return The 2-letter language code (example "en")
     */
    @JvmStatic
    fun getLanguage(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(LOCALE_PREF_KEY_STRING, LanguageUtils.getCurrentDeviceLanguageCode())!!
    }

    /**
     * Convert the device language code (codes defined by ISO 639-1) to a Language ID.
     * Language IDs, used only by WordPress, are integer values that map to a language code.
     * http://bit.ly/2H7gksN
     */
    fun getLanguageWordPressId(context: Context): String {
        val deviceLanguageCode = LanguageUtils.getPatchedCurrentDeviceLanguage(context)

        val languageCodeToID = generateLanguageMap(context)
        var langID: String? = null
        if (languageCodeToID.containsKey(deviceLanguageCode)) {
            langID = languageCodeToID[deviceLanguageCode]
        } else {
            val pos = deviceLanguageCode.indexOf("_")
            if (pos > -1) {
                val newLang = deviceLanguageCode.substring(0, pos)
                if (languageCodeToID.containsKey(newLang)) {
                    langID = languageCodeToID[newLang]
                }
            }
        }

        return langID ?: deviceLanguageCode
    }

    /**
     * Update resources for the current session.
     *
     * @param context  The current active context
     * @param language The 2-letter language code (example "en")
     * @return The modified context containing the updated localized resources
     */
    @SuppressLint("AppBundleLocaleChanges")
    private fun updateResources(context: Context, language: String): Context {
        val locale = languageLocale(language)
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)

        // NOTE: Earlier versions of Android require both of these to be set, otherwise
        // RTL may not be implemented properly.
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    /**
     * Method gets around a bug in the java.util.Formatter for API 7.x as detailed here
     * [https://bugs.openjdk.java.net/browse/JDK-8167567]. Any strings that contain
     * locale-specific grouping separators should use:
     * `
     * String.format(LocaleManager.getSafeLocale(context), baseString, val)
     *
     * An example of a string that contains locale-specific grouping separators:
     * `
     * <string name="test">%,d likes</string>
    ` *
     */
    @JvmStatic
    fun getSafeLocale(context: Context?): Locale {
        val baseLocale: Locale
        if (context == null) {
            baseLocale = Locale.getDefault()
        } else {
            val config = context.resources.configuration
            baseLocale = config.locales[0]
        }

        return languageLocale(baseLocale.language)
    }

    /**
     * Gets a locale for the given language code.
     *
     * @param languageCode The language code (example "en" or "es-US"). If null or empty will return
     * the current default locale.
     */
    @JvmStatic
    fun languageLocale(languageCode: String?): Locale {
        if (languageCode == null || TextUtils.isEmpty(languageCode)) {
            return Locale.getDefault()
        }
        // Attempt to parse language and region codes.
        val opts = LANGUAGE_SPLITTER.split(languageCode, 0)
        return if (opts.size > 1) {
            Locale(opts[0], opts[1])
        } else {
            Locale(opts[0])
        }
    }

    /**
     * Creates a map from language codes to WordPress language IDs.
     */
    @JvmStatic
    fun generateLanguageMap(context: Context): Map<String, String> {
        val languageIds = context.resources.getStringArray(R.array.lang_ids)
        val languageCodes = context.resources.getStringArray(R.array.language_codes)

        val languageMap: MutableMap<String, String> = HashMap()
        var i = 0
        while (i < languageIds.size && i < languageCodes.size) {
            languageMap[languageCodes[i]] = languageIds[i]
            ++i
        }

        return languageMap
    }

    /**
     * Generates display strings for given language codes. Used as entries in language preference.
     */
    @JvmStatic
    fun createSortedLanguageDisplayStrings(
        languageCodes: Array<CharSequence>?,
        locale: Locale
    ): Triple<Array<String?>, Array<String?>, Array<String?>>? {
        if (languageCodes.isNullOrEmpty()) {
            return null
        }

        val entryStrings = ArrayList<String>(languageCodes.size)
        for (i in languageCodes.indices) {
            // "__" is used to sort the language code with the display string so both arrays are sorted at the same time
            entryStrings.add(
                i, StringUtils.capitalize(
                    getLanguageString(languageCodes[i].toString(), locale)
                ) + "__" + languageCodes[i]
            )
        }

        entryStrings.sortWith(Collator.getInstance(locale))

        val sortedEntries = arrayOfNulls<String>(languageCodes.size)
        val sortedValues = arrayOfNulls<String>(languageCodes.size)
        val detailStrings = arrayOfNulls<String>(languageCodes.size)

        for (i in entryStrings.indices) {
            // now, we can split the sorted array to extract the display string and the language code
            val split =
                entryStrings[i].split("__".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            sortedEntries[i] = split[0]
            sortedValues[i] = split[1]
            detailStrings[i] =
                StringUtils.capitalize(
                    getLanguageString(
                        sortedValues[i], languageLocale(
                            sortedValues[i]
                        )
                    )
                )
        }

        return Triple(sortedEntries, sortedValues, detailStrings)
    }


    /**
     * Return a non-null display string for a given language code.
     */
    @JvmStatic
    fun getLanguageString(languageCode: String?, displayLocale: Locale): String {
        if (languageCode == null || languageCode.length < 2 || languageCode.length > 6) {
            return ""
        }

        val languageLocale = languageLocale(languageCode)
        val displayLanguage =
            StringUtils.capitalize(languageLocale.getDisplayLanguage(displayLocale))
        val displayCountry = languageLocale.getDisplayCountry(displayLocale)

        if (!TextUtils.isEmpty(displayCountry)) {
            return "$displayLanguage ($displayCountry)"
        }
        return displayLanguage
    }

    @JvmStatic
    fun getLocalePrefKeyString(): String = LOCALE_PREF_KEY_STRING
}
