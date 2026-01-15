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
    animationSpec: AnimationSpec<Float> = tween(500),
  ) {
    _coroutineScope?.launch {
      animateScrollToPageNext(animationSpec)
    }
  }

  /** 滚动到上一项（异步） */
  fun animateScrollToPagePreviousAsync(
    animationSpec: AnimationSpec<Float> = tween(500),
  ) {
    _coroutineScope?.launch {
      animateScrollToPagePrevious(animationSpec)
    }
  }

  /** 滚动到下一项 */
  suspend fun animateScrollToPageNext(
    animationSpec: AnimationSpec<Float> = tween(500),
  ) {
    if (realPageCount <= 1) return
    val page = currentPage + 1
    if (page < pageCount) {
      if (page != targetPage) {
        animateScrollToPage(page, animationSpec = animationSpec)
      }
    } else {
      scrollToPage(CENTER_PAGE)
    }
  }

  /** 滚动到上一项 */
  suspend fun animateScrollToPagePrevious(
    animationSpec: AnimationSpec<Float> = tween(500),
  ) {
    if (realPageCount <= 1) return
    val page = currentPage - 1
    if (page >= 0) {
      if (page != targetPage) {
        animateScrollToPage(page, animationSpec = animationSpec)
      }
    } else {
      scrollToPage(CENTER_PAGE)
    }
  }

  companion object {
    internal val Saver: Saver<InfinitePagerState, *> = listSaver(
      save = { listOf(it.realPageCount, it.currentPage) },
      restore = { InfinitePagerState(pageCount = it[0], currentPage = it[1]) }
    )
  }
}

private const val MAX_PAGE_COUNT = Int.MAX_VALUE
private const val CENTER_PAGE = Int.MAX_VALUE / 2 + 1