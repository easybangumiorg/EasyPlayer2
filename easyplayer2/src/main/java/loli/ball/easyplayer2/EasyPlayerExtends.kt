package loli.ball.easyplayer2

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Created by LoliBall on 2023/4/3 0:32.
 * https://github.com/WhichWho
 */

@Composable
fun SimpleTopBar(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    isShowOnNormalScreen: Boolean = false,
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
    modifier: Modifier = Modifier,
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
fun BoxScope.LockBtn(vm: ControlViewModel) {

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
fun BoxScope.ProgressBox(vm: ControlViewModel) {
    if (vm.isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
