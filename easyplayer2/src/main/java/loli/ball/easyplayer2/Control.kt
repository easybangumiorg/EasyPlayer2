package loli.ball.easyplayer2

import android.widget.ImageButton
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import loli.ball.easyplayer2.utils.TimeUtils

/**
 * Created by HeYanLe on 2023/3/8 22:04.
 * https://github.com/heyanLE
 */

@Composable
fun TopControl(
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color.Black, Color.Transparent),
                )
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
        content = content
    )
}

@Composable
fun BottomControl(
    padding: PaddingValues = PaddingValues(0.dp),
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black)
                )
            )
            .padding(padding)
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        content = content
    )
}

/**
 * 播放按钮
 */
@Composable
fun RowScope.PlayPauseBtn(
    isPlaying: Boolean,
    onClick: (Boolean) -> Unit,
) {
    Icon(
        if (isPlaying) Icons.Filled.Pause
        else Icons.Filled.PlayArrow,
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                onClick(!isPlaying)
            }
            .padding(4.dp)
            ,
        tint = Color.White,
        contentDescription = null
    )
}

/**
 * 时间字符串 00:00
 */
@Composable
fun RowScope.TimeText(
    time: Long,
    color: Color = Color.Unspecified,
) {
    Text(
        modifier = Modifier
            .padding(4.dp, 0.dp)
            .align(Alignment.CenterVertically),
        text = TimeUtils.toString(time),
        color = color
    )
}


@Composable
fun RowScope.TimeSlider(
    during: Long,
    position: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinish: () -> Unit,
) {
    Slider(
        modifier = Modifier
            .background(Color.Red)
            .weight(1f),
        value = position,
        onValueChange = {
            onValueChange(it)
        },
        onValueChangeFinished = onValueChangeFinish,
        valueRange = 0F..during.toFloat().coerceAtLeast(0F)
    )
}

@Composable
fun RowScope.FullScreenBtn(
    isFullScreen: Boolean,
    onClick: (Boolean) -> Unit,
) {
    Icon(
        if (isFullScreen) Icons.Filled.FullscreenExit
        else Icons.Filled.Fullscreen,
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                onClick(!isFullScreen)
            }
            .padding(4.dp),
        tint = Color.White,
        contentDescription = null
    )
}

@Composable
fun RowScope.BackBtn(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            Icons.Filled.ArrowBack,
            tint = Color.White,
            contentDescription = null
        )
    }
}
