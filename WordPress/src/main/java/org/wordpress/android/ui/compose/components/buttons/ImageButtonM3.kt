package org.wordpress.android.ui.compose.components.buttons

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import org.wordpress.android.R
import org.wordpress.android.ui.compose.utils.uiStringText
import org.wordpress.android.ui.utils.UiString

/**
 * Material3 version of [ImageButton] which re-uses some of that
 * composable's functionality.
 */
@Composable
fun ImageButtonM3(
    modifier: Modifier = Modifier,
    drawableLeft: Drawable? = null,
    drawableRight: Drawable? = null,
    drawableTop: Drawable? = null,
    drawableBottom: Drawable? = null,
    button: Button,
    onClick: () -> Unit
) {
    ConstraintLayout(modifier = modifier
        .clickable { onClick.invoke() }) {
        val (buttonTextRef) = createRefs()
        Box(modifier = Modifier
            .constrainAs(buttonTextRef) {
                top.linkTo(parent.top, drawableTop?.iconSize ?: 0.dp)
                bottom.linkTo(parent.bottom, drawableBottom?.iconSize ?: 0.dp)
                start.linkTo(parent.start, drawableLeft?.iconSize ?: 0.dp)
                end.linkTo(parent.end, drawableRight?.iconSize ?: 0.dp)
                width = Dimension.wrapContent
            }
        ) {
            val buttonTextValue: String = uiStringText(button.text)
            Text(
                text = buttonTextValue,
                fontSize = button.fontSize,
                fontWeight = button.fontWeight,
                color = button.color,
            )
        }

        drawableLeft?.let { drawable ->
            val (imageLeft) = createRefs()
            Image(
                modifier = Modifier.constrainAs(imageLeft) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                },
                painter = painterResource(id = drawable.resId),
                contentDescription = null
            )
        }

        drawableRight?.let { drawable ->
            val (imageRight) = createRefs()
            Image(
                modifier = Modifier.constrainAs(imageRight) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(buttonTextRef.end, margin = drawable.padding)
                }.size(drawable.iconSize),
                painter = painterResource(id = drawable.resId),
                contentDescription = null
            )
        }

        drawableTop?.let { drawable ->
            val (imageTop) = createRefs()
            Image(
                modifier = Modifier.constrainAs(imageTop) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                painter = painterResource(id = drawable.resId),
                contentDescription = null
            )
        }

        drawableBottom?.let { drawable ->
            val (imageBottom) = createRefs()
            Image(
                modifier = Modifier.constrainAs(imageBottom) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                painter = painterResource(id = drawable.resId),
                contentDescription = null
            )
        }
    }
}

@Preview
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewImageButtonM3() {
    ImageButtonM3(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                Color.Gray,
                shape = RoundedCornerShape(6.dp)
            ),
        drawableLeft = Drawable(R.drawable.ic_pages_white_24dp),
        drawableRight = Drawable(R.drawable.ic_pages_white_24dp),
        drawableTop = Drawable(R.drawable.ic_pages_white_24dp),
        drawableBottom = Drawable(R.drawable.ic_pages_white_24dp),
        button = Button(text = UiString.UiStringText("Button Text")),
        onClick = {}
    )
}
