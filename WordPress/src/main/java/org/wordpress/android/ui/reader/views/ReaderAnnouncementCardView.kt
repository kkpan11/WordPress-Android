package org.wordpress.android.ui.reader.views

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.AbstractComposeView
import org.wordpress.android.ui.compose.theme.AppTheme
import org.wordpress.android.ui.reader.views.compose.ReaderAnnouncementCard
import org.wordpress.android.ui.reader.views.compose.ReaderAnnouncementCardItemData

class ReaderAnnouncementCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {
    private val items: MutableState<List<ReaderAnnouncementCardItemData>> = mutableStateOf(emptyList())

    private val onDoneClickListener: MutableState<OnDoneClickListener?> = mutableStateOf(null)

    @Composable
    override fun Content() {
        AppTheme {
            ReaderAnnouncementCard(
                shouldShow = true,
                items = items.value,
                onAnnouncementCardDoneClick = { onDoneClickListener.value?.onDoneClick() }
            )
        }
    }

    fun setItems(items: List<ReaderAnnouncementCardItemData>) {
        this.items.value = items
    }

    fun setOnDoneClickListener(listener: OnDoneClickListener) {
        this.onDoneClickListener.value = listener
    }

    interface OnDoneClickListener {
        fun onDoneClick()
    }
}
