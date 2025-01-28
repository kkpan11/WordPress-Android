package org.wordpress.android.ui.mysite.cards.nocards

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Modifier
import org.wordpress.android.databinding.NoCardsMessageBinding
import org.wordpress.android.ui.compose.theme.AppThemeM3
import org.wordpress.android.ui.mysite.MySiteCardAndItem
import org.wordpress.android.ui.mysite.MySiteCardAndItemViewHolder
import org.wordpress.android.util.extensions.viewBinding

class NoCardsMessageViewHolder(parent: ViewGroup) :
    MySiteCardAndItemViewHolder<NoCardsMessageBinding>(parent.viewBinding(NoCardsMessageBinding::inflate)) {
    fun bind(cardModel: MySiteCardAndItem.Card.NoCardsMessage) = with(binding) {
        noCardsMessage.setContent {
            AppThemeM3 {
                NoCardsMessage(
                    model = cardModel, modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )
            }
        }
    }
}
