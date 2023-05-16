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
    isPadMode: Boolean = false,
    videoFloat: (@Composable (ControlViewModel) -> Unit)? = null,
    control: (@Composable (ControlViewModel) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    EasyPlayerStateSync(vm)
    if(isPadMode){
        Row {
            EasyPlayer(
                modifier = Modifier.weight(1f),
                vm = vm,
                control = control,
                isPadMode = isPadMode,
                videoFloat = videoFloat
            )
            Column (
                modifier = Modifier.weight(1f),
            ){
                this.content()
            }

        }
    }else{
        Column(modifier) {
            EasyPlayer(
                modifier = Modifier.fillMaxWidth(),
                vm = vm,
                control = control,
                isPadMode = isPadMode,
                videoFloat = videoFloat
            )
            this@Column.content()
        }
    }

}

@Composable
fun EasyPlayer(
    vm: ControlViewModel,
    modifier: Modifier = Modifier,
    isPadMode: Boolean = false,
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
                } else if(!isPadMode){
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(ControlViewModel.ratioWidth / ControlViewModel.ratioHeight)
                }else{
                    Modifier
                        .fillMaxSize()
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
