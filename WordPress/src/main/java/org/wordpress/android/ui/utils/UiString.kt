package org.wordpress.android.ui.utils

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

/**
 * [UiString] is a utility sealed class that represents a string to be used in the UI. It allows a string to be
 * represented as both string resource and text.
 */
sealed class UiString : Parcelable {
    @Parcelize
    data class UiStringText(val text: CharSequence) : UiString()
    @Parcelize
    data class UiStringRes(@StringRes val stringRes: Int) : UiString()
    @Parcelize
    data class UiStringResWithParams(@StringRes val stringRes: Int, val params: List<UiString>) : UiString() {
        constructor(@StringRes stringRes: Int, vararg varargParams: UiString) : this(stringRes, varargParams.toList())
    }

    // Current localization process does not support <plurals> resource strings,
    // so we need to use multiple string resources. Switch to @PluralsRes when it is supported by localization process.
    @Parcelize
    data class UiStringPluralRes(
        @StringRes val zeroRes: Int,
        @StringRes val oneRes: Int,
        @StringRes val otherRes: Int,
        val count: Int
    ) : UiString()
}
