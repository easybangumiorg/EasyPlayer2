package loli.ball.easyplayer2

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import loli.ball.easyplayer2.utils.TimeUtils
import loli.ball.easyplayer2.utils.pointerInput
import loli.ball.easyplayer2.utils.systemVolume
import loli.ball.easyplayer2.utils.windowBrightness

/**
 * Created by HeYanLe on 2023/3/27 21:47.
 * https://github.com/heyanLE
 */
class GestureControllerScope(
    boxScope: BoxScope,
    val vm: ControlViewModel,
    val showBrightVolumeUi: MutableState<DragType?> = mutableStateOf<DragType?>(null),
    val brightVolumePercent: MutableState<Int> = mutableStateOf(0)
) : BoxScope by boxScope

@Composable
fun SimpleGestureController(
    vm: ControlViewModel,
    modifier: Modifier,
    slideFullTime: Long = 300000,
) {

    GestureController(vm = vm, modifier = modifier, slideFullTime = slideFullTime) {
        BrightVolumeUI()
        SlideUI()
        LongTouchUI()
        LongTouchUI()
    }
}

@Composable
fun SimpleGestureController(
    vm: ControlViewModel,
    modifier: Modifier,
    slideFullTime: Long = 300000,
    longTouchText: String,
) {
    GestureController(vm = vm, modifier = modifier, slideFullTime = slideFullTime) {
        BrightVolumeUI()
        SlideUI()
        LongTouchUI(longTouchText)
    }
}

@Composable
fun GestureController(
    vm: ControlViewModel,
    modifier: Modifier,
    slideFullTime: Long = 300000,
    content: @Composable GestureControllerScope.(ControlViewModel) -> Unit,
) {

    val ctx = LocalContext.current as Activity
    var viewSize by remember { mutableStateOf(IntSize.Zero) }

    val showBrightVolumeUi = remember { mutableStateOf<DragType?>(null) }
    val brightVolumeUiText = remember { mutableStateOf(0) }

    val enableGuest by remember {
        derivedStateOf {
            vm.isFullScreen && vm.controlState != ControlViewModel.ControlState.Locked
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
            .onSizeChanged { viewSize = it }
            .pointerInput("单机双击", true) {
                // 双击
                detectTapGestures(
                    onTap = {
                        vm.onSingleClick()
                    },
                    onDoubleTap = {
                        vm.onPlayPause(!vm.playWhenReady)
                    }
                )
            }
            .pointerInput("长按倍速", true) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { vm.onLongPress() },
                    onDragCancel = { vm.onActionUP() },
                    onDragEnd = { vm.onActionUP() },
                    onDrag = { _, _ -> }
                )
            }
            .pointerInput("横向滑动", enableGuest) {
                var horizontalOffset = 0F
                var oldPosition = 0L
                // 横向滑动
                detectHorizontalDragGestures(
                    onDragStart = {
                        oldPosition = vm.position
                        horizontalOffset = 0F
                    },
                    onDragCancel = { vm.onActionUP() },
                    onDragEnd = { vm.onActionUP() },
                    onHorizontalDrag = { _: PointerInputChange, dragAmount: Float ->
                        horizontalOffset += dragAmount
                        val percent = horizontalOffset / viewSize.width
                        vm.onPositionChange(oldPosition + (slideFullTime * percent).toLong())
                    },
                )
            }
            .brightVolume(enableGuest, showBrightVolumeUi) { type -> // 音量、亮度
                brightVolumeUiText.value = (when (type) {
                    DragType.BRIGHTNESS -> ctx.windowBrightness
                    DragType.VOLUME -> with(ctx) { systemVolume }
                } * 100).toInt()
            }
    ) {
        val scope = remember(this, vm) {
            GestureControllerScope(this, vm, showBrightVolumeUi, brightVolumeUiText)
        }
        scope.content(vm)


    }
}

// 音量 亮度
@Composable
fun GestureControllerScope.BrightVolumeUI() {


    val brightVolumeUiIcon = remember(showBrightVolumeUi.value) {
        when (showBrightVolumeUi.value) {
            DragType.BRIGHTNESS -> Icons.Filled.LightMode
            DragType.VOLUME -> Icons.Filled.VolumeUp
            else -> Icons.Filled.VolumeUp
        }
    }

    // 音量、亮度
    AnimatedVisibility(
        visible = showBrightVolumeUi.value != null,
        modifier = Modifier.align(Alignment.Center),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        BrightVolumeUi(
            brightVolumeUiIcon,
            showBrightVolumeUi.value.toString(),
            brightVolumePercent.value
        )
    }


}

@Composable
fun GestureControllerScope.SlideUI() {

    // 横向滑动
    AnimatedVisibility(
        visible = vm.controlState == ControlViewModel.ControlState.HorizontalScroll,
        modifier = Modifier.align(Alignment.Center),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = TimeUtils.toString(this@GestureControllerScope.vm.horizontalScrollPosition) + "/" +
                        TimeUtils.toString(this@GestureControllerScope.vm.during),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun GestureControllerScope.LongTouchUI() {
    // 长按倍速
    AnimatedVisibility(
        visible = vm.isLongPress,
        modifier = Modifier.align(Alignment.TopCenter),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.FastForward,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    text = ">>>",
                    color = Color.White
                )

            }
        }
    }
}

@Composable
fun GestureControllerScope.LongTouchUI(text: String) {
    // 长按倍速
    AnimatedVisibility(
        visible = vm.isLongPress,
        modifier = Modifier.align(Alignment.TopCenter),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.FastForward,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    text = text,
                    color = Color.White
                )

            }
        }
    }
}