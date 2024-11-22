import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import org.wordpress.android.R

/**
 * Text composables don't support HTML at this time so we get around it by resorting
 * to a standard TextView
 */
@Composable
fun HtmlTextM3(
    text: String,
    modifier: Modifier = Modifier,
    @ColorRes color: Int = R.color.text,
    @ColorRes linkColor: Int = R.color.link_reader,
    fontSize: Float? = null,
    alignment: Int? = null,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                fontSize?.let { setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
                alignment?.let { textAlignment = it }
                setTextColor(context.getColor(color))
                setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY))
                setLinkTextColor(context.getColor(linkColor))
            }
        }
    )
}
