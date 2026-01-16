package com.sd.lib.compose.pager

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 当[pageCount]大于1时，无限循环
 */
@Composable
fun rememberInfinitePagerState(pageCount: Int): InfinitePagerState {
  @Suppress("NAME_SHADOWING")
  val pageCount = pageCount.coerceAtLeast(0)
  return rememberSaveable(saver = InfinitePagerState.Saver) {
    InfinitePagerState(
      pageCount = pageCount,
      currentPage = if (pageCount > 1) CENTER_PAGE else 0,
    )
  }.also { state ->
    state.Init()
    LaunchedEffect(state, pageCount) {
      if (pageCount > 1) {
        val offset = state.realSettledPage.coerceAtMost(pageCount - 1)
        state.realPageCount = pageCount
        state.requestScrollToPage(CENTER_PAGE + offset)
      } else {
        state.realPageCount = pageCount
      }
    }
  }
}

class InfinitePagerState internal constructor(
  pageCount: Int,
  currentPage: Int,
) : PagerState(currentPage) {
  /** 真实页数 */
  var realPageCount by mutableStateOf(pageCount)
    internal set

  /** 真实[currentPage] */
  val realCurrentPage: Int
    get() = realPageOf(currentPage)

  /** 真实[settledPage] */
  val realSettledPage: Int
    get() = realPageOf(settledPage)

  override val pageCount: Int
    get() = realPageCount.let { if (it > 1) MAX_PAGE_COUNT else it }

  private var _coroutineScope: CoroutineScope? = null

  @Composable
  internal fun Init() {
    _coroutineScope = rememberCoroutineScope()
    LaunchedEffect(isScrollInProgress) {
      if (isScrollInProgress) return@LaunchedEffect
      if (pageCount == MAX_PAGE_COUNT && realPageCount < RECENTER_THRESHOLD) {
        val distance = abs(currentPage - CENTER_PAGE)
        if (distance > RECENTER_THRESHOLD) {
          requestScrollToPage(CENTER_PAGE + realCurrentPage)
        }
      }
    }
  }

  /** 把[page]映射为真实的page */
  fun realPageOf(page: Int): Int {
    val count = realPageCount
    return if (count > 1) {
      val offset = page - CENTER_PAGE
      ((offset % count) + count) % count
    } else {
      0
    }
  }

  /** 滚动到下一项（异步） */
  fun animateScrollToPageNextAsync(
    /** 动画参数 */
    animationSpec: AnimationSpec<Float> = tween(500),
  ) {
    _coroutineScope?.launch {
      animateScrollToPageNext(animationSpec)
    }
  }

  /** 滚动到上一项（异步） */
  fun animateScrollToPagePreviousAsync(
    /** 动画参数 */
    animationSpec: AnimationSpec<Float> = tween(500),
  ) {
    _coroutineScope?.launch {
      animateScrollToPagePrevious(animationSpec)
    }
  }

  /** 滚动到下一项 */
  suspend fun animateScrollToPageNext(
    /** 动画参数 */
    animationSpec: AnimationSpec<Float> = tween(500),
  ) {
    animateScrollToPageDelta(delta = 1, animationSpec)
  }

  /** 滚动到上一项 */
  suspend fun animateScrollToPagePrevious(
    /** 动画参数 */
    animationSpec: AnimationSpec<Float> = tween(500),
  ) {
    animateScrollToPageDelta(delta = -1, animationSpec)
  }

  private suspend fun animateScrollToPageDelta(
    /** 偏移量 */
    delta: Int,
    /** 动画参数 */
    animationSpec: AnimationSpec<Float>,
  ) {
    if (realPageCount <= 1) return

    val page = currentPage + delta
    if (page == targetPage) return

    if (page in 0..<pageCount) {
      animateScrollToPage(page, animationSpec = animationSpec)
    }
  }

  companion object {
    internal val Saver: Saver<InfinitePagerState, *> = listSaver(
      save = { listOf(it.realPageCount, it.currentPage) },
      restore = { InfinitePagerState(pageCount = it[0], currentPage = it[1]) }
    )
  }
}

/** 最大页数 */
private const val MAX_PAGE_COUNT = Int.MAX_VALUE

/** 中间页码 */
private const val CENTER_PAGE = Int.MAX_VALUE / 2

/** 回中阈值 */
private const val RECENTER_THRESHOLD = CENTER_PAGE - 10000