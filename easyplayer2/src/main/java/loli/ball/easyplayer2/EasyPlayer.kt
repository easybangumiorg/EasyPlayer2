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
fun SimpleTopBar(
    isShowOnNormalScreen: Boolean = false,
    vm: ControlViewModel,
    modifier: Modifier,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = vm.isShowOverlay() && (vm.isFullScreen || isShowOnNormalScreen),
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