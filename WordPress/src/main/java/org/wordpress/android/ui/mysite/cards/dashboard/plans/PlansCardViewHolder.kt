package org.wordpress.android.ui.mysite.cards.dashboard.plans

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import org.wordpress.android.R
import org.wordpress.android.databinding.DashboardCardPlansBinding
import org.wordpress.android.ui.mysite.MySiteCardAndItem.Card.DashboardPlansCard
import org.wordpress.android.ui.mysite.MySiteCardAndItemViewHolder
import org.wordpress.android.ui.utils.ListItemInteraction
import org.wordpress.android.ui.utils.UiHelpers
import org.wordpress.android.util.extensions.viewBinding

class PlansCardViewHolder(
    parent: ViewGroup,
    private val uiHelpers: UiHelpers
) : MySiteCardAndItemViewHolder<DashboardCardPlansBinding>(
    parent.viewBinding(DashboardCardPlansBinding::inflate)
) {
    fun bind(card: DashboardPlansCard) = with(binding) {
        uiHelpers.setTextOrHide(dashboardCardPlansTitle, card.title)
        dashboardCardPlansCta.setOnClickListener { card.onClick.click() }
        dashboardPlansCardMore.contentDescription = itemView.context.getString(R.string.more_content_description_plans)
        dashboardPlansCardMore.setOnClickListener {
            showMoreMenu(
                card.onHideMenuItemClick,
                card.onMoreMenuClick,
                dashboardPlansCardMore,
            )
        }
    }

    private fun showMoreMenu(
        onHideMenuItemClick: ListItemInteraction,
        onMoreMenuClick: ListItemInteraction,
        anchor: View
    ) {
        onMoreMenuClick.click()
        val popupMenu = PopupMenu(itemView.context, anchor)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.dashboard_card_plans_menu_item_hide_this -> {
                    onHideMenuItemClick.click()
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener true
            }
        }
        popupMenu.inflate(R.menu.dashboard_card_plans_menu)
        popupMenu.show()
    }
}
