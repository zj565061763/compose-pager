package com.sd.demo.compose.pager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sd.demo.compose.pager.theme.AppTheme
import com.sd.lib.compose.pager.InfinitePagerState
import com.sd.lib.compose.pager.LoopToNext
import com.sd.lib.compose.pager.LoopToPrevious
import com.sd.lib.compose.pager.realPageOf
import com.sd.lib.compose.pager.rememberInfinitePagerState

class SampleActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AppTheme {
        Content()
      }
    }
  }
}

@Composable
private fun Content(
  modifier: Modifier = Modifier,
) {
  var pageCount by remember { mutableStateOf(0) }
  val pagerState = rememberInfinitePagerState(pageCount)
  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    CountRow(pageCount = pageCount) { pageCount = it }
    if (pageCount > 0) {
      ScrollView(pagerState = pagerState)
      LoopView(pagerState = pagerState)
    }
    PagerView(
      modifier = Modifier.weight(1f),
      pagerState = pagerState,
    )
  }
}

@Composable
private fun CountRow(
  modifier: Modifier = Modifier,
  pageCount: Int,
  onClickCount: (Int) -> Unit,
) {
  LazyRow(modifier = modifier) {
    items(10) { index ->
      val selected = index == pageCount
      TextButton(
        onClick = { onClickCount(index) },
        colors = ButtonDefaults.textButtonColors(
          contentColor = if (selected) Color.Red else MaterialTheme.colorScheme.primary
        ),
      ) {
        Text(text = index.toString())
      }
    }
  }
}

@Composable
private fun ScrollView(
  modifier: Modifier = Modifier,
  pagerState: InfinitePagerState,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Button(onClick = { pagerState.animateScrollToPagePreviousAsync() }) {
      Text(text = "<")
    }
    Button(onClick = { pagerState.animateScrollToPageNextAsync() }) {
      Text(text = ">")
    }
  }
}

@Composable
private fun LoopView(
  modifier: Modifier = Modifier,
  pagerState: InfinitePagerState,
) {
  val list = remember { listOf(-1, 0, 1) }
  var loop by remember { mutableStateOf(0) }
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    for (item in list) {
      Button(onClick = { loop = item }) {
        Text(
          text = when (item) {
            1 -> ">"
            -1 -> "<"
            else -> "x"
          },
          color = if (item == loop) Color.Red else Color.Unspecified,
        )
      }
    }
  }
  when (loop) {
    1 -> pagerState.LoopToNext()
    -1 -> pagerState.LoopToPrevious()
  }
}

@Composable
private fun PagerView(
  modifier: Modifier = Modifier,
  pagerState: InfinitePagerState,
) {
  HorizontalPager(
    modifier = modifier.fillMaxSize(),
    state = pagerState,
  ) { p ->
    val page = pagerState.realPageOf(p)
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(if (page % 2 == 0) Color.Red else Color.Blue),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = page.toString(),
        fontSize = 24.sp,
        color = Color.White,
      )
    }
  }

  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.realCurrentPage to pagerState.currentPage }
      .collect {
        logMsg { "currentPage:${it.first} -> ${it.second}" }
      }
  }
}