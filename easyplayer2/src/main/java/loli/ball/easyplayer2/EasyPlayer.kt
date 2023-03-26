package loli.ball.easyplayer2

import android.app.Activity
import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import loli.ball.easyplayer2.utils.*

/**
 * Created by HeYanLe on 2023/3/9 11:23.
 * https://github.com/heyanLE
 */
@Composable
fun EasyPlayerScaffold(
    modifier: Modifier = Modifier,
    vm: ControlViewModel,
    videoFloat: (@Composable (ControlViewModel) -> Unit)? = null,
    control: (@Composable (ControlViewModel) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {

    val ctx = LocalContext.current as Activity
    val ui = rememberSystemUiController()

    DisposableEffect(Unit) {
        vm.onLaunch()
        onDispose {
            vm.onDisposed()
            ctx.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    LaunchedEffect(vm.fullScreenState) {
        if (vm.isFullScreen) {
            ctx.requestedOrientation =
                if (vm.isReverse) ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            ui.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            ui.isSystemBarsVisible = false
        } else {
            ctx.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            ui.isSystemBarsVisible = true
        }
    }

    // 根据传感器来横竖屏
    OnOrientationEvent { _, orientation ->
        vm.onOrientation(orientation, act = ctx)
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> ui.isSystemBarsVisible = !vm.isFullScreen
            Lifecycle.Event.ON_PAUSE -> vm.exoPlayer.pause()
            else -> Unit
        }
    }

    BackHandler(vm.isFullScreen) {
        vm.onFullScreen(false, ctx = ctx)
    }

    Column(modifier) {
        EasyPlayer(
            modifier = Modifier.fillMaxWidth(),
            controlViewModel = vm,
            control = control,
            videoFloat = videoFloat
        )
        this@Column.content()
    }

}

@Composable
fun EasyPlayer(
    modifier: Modifier,
    controlViewModel: ControlViewModel,
    control: (@Composable (ControlViewModel) -> Unit)? = null,
    videoFloat: (@Composable (ControlViewModel) -> Unit)? = null,
) {

    BackgroundBasedBox(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .let {
                if (controlViewModel.isFullScreen) it
                else it.statusBarsPadding()
            }
            .then(modifier),
        background = {
            val surModifier = remember(controlViewModel.isFullScreen) {
                if (controlViewModel.isFullScreen) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(ControlViewModel.ratioWidth / ControlViewModel.ratioHeight)
                }
            }
            Box(modifier = surModifier, contentAlignment = Alignment.Center) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { controlViewModel.surfaceView }
                )
            }
        },
        foreground = {
            Box(modifier = Modifier.fillMaxSize()) {
                control?.invoke(controlViewModel)
                videoFloat?.invoke(controlViewModel)
            }
        }
    )

}

@Composable
fun GestureController(
    vm: ControlViewModel,
    modifier: Modifier,
    slideFullTime: Long = 300000,
) {

    val ctx = LocalContext.current as Activity
    var viewSize by remember { mutableStateOf(IntSize.Zero) }

    val showBrightVolumeUi = remember { mutableStateOf<DragType?>(null) }
    var brightVolumeUiIcon by remember { mutableStateOf(Icons.Filled.LightMode) }
    var brightVolumeUiText by remember { mutableStateOf(0) }

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
            .pointerInput("长按倍速", enableGuest) {
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
                brightVolumeUiIcon = when (type) {
                    DragType.BRIGHTNESS -> Icons.Filled.LightMode
                    DragType.VOLUME -> Icons.Filled.VolumeUp
                }
                brightVolumeUiText = (when (type) {
                    DragType.BRIGHTNESS -> ctx.windowBrightness
                    DragType.VOLUME -> with(ctx) { systemVolume }
                } * 100).toInt()
            }
    ) {

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
                brightVolumeUiText
            )
        }

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
                    text = TimeUtils.toString(vm.horizontalScrollPosition) + "/" +
                            TimeUtils.toString(vm.during),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }


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
}

@Composable
fun SimpleTopBar(
    vm: ControlViewModel,
    modifier: Modifier,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = vm.isShowOverlay(),
        exit = fadeOut(),
        enter = fadeIn(),
    ) {
        TopControl {
            val ctx = LocalContext.current as Activity
            BackBtn {
                vm.onFullScreen(false, ctx = ctx)
            }
            Text(text = vm.title)
        }
    }
}

@Composable
fun SimpleBottomBar(
    vm: ControlViewModel,
    modifier: Modifier,
    otherAction: (@Composable RowScope.(ControlViewModel) -> Unit)? = null,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = vm.isShowOverlay(),
        exit = fadeOut(),
        enter = fadeIn(),
    ) {
        BottomControl {
            PlayPauseBtn(isPlaying = vm.playWhenReady, onClick = {
                vm.onPlayPause(it)
            })
            TimeText(time = vm.position, Color.White)

            val position =
                if (vm.controlState == ControlViewModel.ControlState.Normal) vm.position
                else if (vm.controlState == ControlViewModel.ControlState.HorizontalScroll) vm.horizontalScrollPosition
                else 0

            TimeSlider(
                during = vm.during,
                position = position,
                onValueChange = {
                    vm.onPositionChange(it)
                },
                onValueChangeFinish = {
                    vm.onActionUP()
                }
            )

            TimeText(time = vm.during, Color.White)

            otherAction?.invoke(this, vm)

            val ctx = LocalContext.current as Activity
            FullScreenBtn(isFullScreen = vm.isFullScreen, onClick = {
                vm.onFullScreen(it, ctx = ctx)
            })
        }
    }

}

@Composable
fun BoxScope.LockBtn(
    vm: ControlViewModel
) {
    val isShowLock = when (vm.controlState) {
        ControlViewModel.ControlState.Normal -> vm.isNormalLockedControlShow
        ControlViewModel.ControlState.Locked -> vm.isNormalLockedControlShow
        ControlViewModel.ControlState.Ended -> false
        else -> true
    }

    AnimatedVisibility(
        modifier = Modifier.align(Alignment.CenterStart),
        visible = vm.isFullScreen && isShowLock,
        exit = fadeOut(),
        enter = fadeIn(),
    ) {
        Box(modifier = Modifier
            .padding(4.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable {
                vm.onLockedChange(vm.controlState != ControlViewModel.ControlState.Locked)
            }
            .padding(8.dp)
        ) {
            Icon(
                if (vm.controlState == ControlViewModel.ControlState.Locked) Icons.Filled.Lock
                else Icons.Filled.LockOpen,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
                contentDescription = null
            )
        }
    }
}

@Composable
fun BoxScope.ProgressBox(
    vm: ControlViewModel
) {
    if (vm.isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}