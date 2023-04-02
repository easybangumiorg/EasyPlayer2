package loli.ball.easyplayer2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Created by HeYanLe on 2023/3/9 11:23.
 * https://github.com/heyanLE
 */
@Composable
fun EasyPlayerScaffold(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    videoFloat: (@Composable (ControlViewModel) -> Unit)? = null,
    control: (@Composable (ControlViewModel) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    EasyPlayerStateSync(vm)
    Column(modifier) {
        EasyPlayer(
            modifier = Modifier.fillMaxWidth(),
            vm = vm,
            control = control,
            videoFloat = videoFloat
        )
        this@Column.content()
    }
}

@Composable
fun EasyPlayer(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    control: (@Composable (ControlViewModel) -> Unit)? = null,
    videoFloat: (@Composable (ControlViewModel) -> Unit)? = null,
) {
    BackgroundBasedBox(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .let {
                if (vm.isFullScreen) it
                else it.statusBarsPadding()
            }
            .then(modifier),
        background = {
            val surModifier = remember(vm.isFullScreen) {
                if (vm.isFullScreen) {
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
                    factory = { vm.surfaceView }
                )
            }
        },
        foreground = {
            Box(modifier = Modifier.fillMaxSize()) {
                control?.invoke(vm)
                videoFloat?.invoke(vm)
            }
        }
    )
}
