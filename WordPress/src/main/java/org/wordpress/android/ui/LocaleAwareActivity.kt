package org.wordpress.android.ui

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity

/**
 * Update Dec 2024: We've added support for per-app language preferences which negate
 * the need for this class. Instead of extending from this class, we should extend
 * from AppCompatActivity.
 */
abstract class LocaleAwareActivity : AppCompatActivity() {
    /**
     * Used to update locales on API 21 to API 25.
     */
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
    }

    /**
     * Used to update locales on API 26 and beyond.
     */
    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        super.applyOverrideConfiguration(overrideConfiguration)
    }
}
