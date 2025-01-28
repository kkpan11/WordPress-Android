package org.wordpress.android.ui.reader.discover.viewholders

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.wordpress.android.databinding.ReaderRecommendedBlogItemBinding
import org.wordpress.android.ui.reader.discover.ReaderCardUiState.ReaderRecommendedBlogsCardUiState.ReaderRecommendedBlogUiState
import org.wordpress.android.util.extensions.viewBinding
import org.wordpress.android.util.image.ImageManager
import org.wordpress.android.util.image.ImageType.BLAVATAR_CIRCULAR

class ReaderRecommendedBlogViewHolder(
    parent: ViewGroup,
    private val imageManager: ImageManager,
    private val binding: ReaderRecommendedBlogItemBinding =
        parent.viewBinding(ReaderRecommendedBlogItemBinding::inflate)
) : RecyclerView.ViewHolder(binding.root) {
    fun onBind(uiState: ReaderRecommendedBlogUiState) =
        with(binding) {
            siteName.text = uiState.name
            siteUrl.text = uiState.url
            updateSiteFollowButton(uiState, this)
            updateBlogImage(uiState.iconUrl)
            root.setOnClickListener {
                uiState.onItemClicked(uiState.blogId, uiState.feedId, uiState.isFollowed)
            }
        }

    private fun updateSiteFollowButton(
        uiState: ReaderRecommendedBlogUiState,
        binding: ReaderRecommendedBlogItemBinding
    ) {
        with(binding.siteFollowButton) {
            isEnabled = uiState.isFollowEnabled
            setIsFollowed(uiState.isFollowed)
            contentDescription = context.getString(uiState.followContentDescription.stringRes)
            setOnClickListener {
                uiState.onFollowClicked(uiState)
            }
        }
    }

    private fun updateBlogImage(iconUrl: String?) = with(binding) {
        if (iconUrl != null) {
            imageManager.loadIntoCircle(
                imageView = siteIcon,
                imageType = BLAVATAR_CIRCULAR,
                imgUrl = iconUrl
            )
        } else {
            imageManager.cancelRequestAndClearImageView(siteIcon)
        }
    }
}
