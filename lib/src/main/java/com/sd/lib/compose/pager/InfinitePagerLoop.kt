package com.sd.lib.compose.pager

import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay

/** 循环轮播，往下一项切换 */
@Composable
fun InfinitePagerState.LoopToNext(
  /** 间隔，默认3000毫秒 */
  getInterval: () -> Long = { 3000L },
) {
  Loop(
    state = this,
    getInterval = getInterval,
    loop = { animateScrollToPageNext() },
  )
}

/** 循环轮播，往上一项切换 */
@Composable
fun InfinitePagerState.LoopToPrevious(
  /** 间隔，默认3000毫秒 */
  getInterval: () -> Long = { 3000L },
) {
  Loop(
    state = this,
    getInterval = getInterval,
    loop = { animateScrollToPagePrevious() },
  )
}

/** 循环轮播 */
@Composable
private fun Loop(
  state: InfinitePagerState,
  getInterval: () -> Long,
  loop: suspend () -> Unit,
) {
  if (state.realPageCount <= 1) return

  val isDragged by state.interactionSource.collectIsDraggedAsState()
  if (isDragged) return

  val getIntervalUpdated by rememberUpdatedState(getInterval)
  val loopUpdated by rememberUpdatedState(loop)

  val lifecycleOwner = LocalLifecycleOwner.current
  LaunchedEffect(state, lifecycleOwner) {
    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
      while (true) {
        delay(getIntervalUpdated())
        loopUpdated()
      }
    }
  }
}