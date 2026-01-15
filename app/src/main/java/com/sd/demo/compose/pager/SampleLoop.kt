package com.sd.demo.compose.pager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sd.demo.compose.pager.theme.AppTheme
import com.sd.lib.compose.pager.InfinitePagerState
import com.sd.lib.compose.pager.LoopToNext
import com.sd.lib.compose.pager.LoopToPrevious
import com.sd.lib.compose.pager.rememberInfinitePagerState

class SampleLoop : ComponentActivity() {
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
  val pagerState = rememberInfinitePagerState(3)
  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    LoopView(pagerState = pagerState)
    AppPagerView(
      modifier = Modifier.weight(1f),
      pagerState = pagerState,
    )
  }
}

@Composable
private fun LoopView(
  modifier: Modifier = Modifier,
  pagerState: InfinitePagerState,
) {
  val list = remember { listOf("<", "x", ">") }
  var loop by remember { mutableStateOf("x") }
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    for (item in list) {
      Button(onClick = { loop = item }) {
        Text(
          text = item,
          color = if (item == loop) Color.Red else Color.Unspecified,
        )
      }
    }
  }
  when (loop) {
    ">" -> pagerState.LoopToNext(getInterval = { 1000 })
    "<" -> pagerState.LoopToPrevious(getInterval = { 1000 })
  }
}