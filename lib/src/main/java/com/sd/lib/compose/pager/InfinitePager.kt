package com.sd.lib.compose.pager

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

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
    get() = realPageCount.let { if (it > 1) Int.MAX_VALUE else it }

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

  companion object {
    internal val Saver: Saver<InfinitePagerState, *> = listSaver(
      save = { listOf(it.realPageCount, it.currentPage) },
      restore = { InfinitePagerState(pageCount = it[0], currentPage = it[1]) }
    )
  }
}

internal const val CENTER_PAGE = Int.MAX_VALUE / 2 + 1