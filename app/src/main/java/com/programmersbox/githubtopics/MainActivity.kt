package com.programmersbox.githubtopics

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.accompanist.flowlayout.FlowRow
import com.programmersbox.githubtopics.ui.theme.GithubTopicsTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GithubTopicsTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GithubTopicUI()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun GithubTopicUI(vm: TopicViewModel = viewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = { LargeTopAppBar(title = { Text(text = "Github Topics") }, scrollBehavior = scrollBehavior) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        val pullRefreshState = rememberPullRefreshState(refreshing = vm.isLoading, onRefresh = vm::refresh)
        Box(
            modifier = Modifier
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            val state = rememberLazyListState()
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxSize(),
                state = state
            ) { items(vm.items) { TopicItem(item = it) } }

            PullRefreshIndicator(
                refreshing = vm.isLoading, state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                scale = true
            )

            InfiniteListHandler(
                listState = state,
                onLoadMore = vm::newPage
            )
        }
    }
}

@Composable
fun InfiniteListHandler(
    listState: LazyListState,
    buffer: Int = 2,
    onLoadMore: () -> Unit
) {
    val loadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            lastVisibleItemIndex > (totalItemsNumber - buffer)
        }
    }

    LaunchedEffect(loadMore) {
        snapshotFlow { loadMore.value }
            .drop(1)
            .distinctUntilChanged()
            .collect { onLoadMore() }
    }
}

private fun Context.openWebPage(url: String) {
    val webpage: Uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, webpage)
    startActivity(intent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicItem(vm: TopicViewModel = viewModel(), item: GitHubTopic) {
    val context = LocalContext.current
    OutlinedCard(
        onClick = { context.openWebPage(item.htmlUrl) }
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            ListItem(
                headlineText = { Text(item.fullName) },
                overlineText = {
                    Text(
                        item.fullName,
                        textDecoration = TextDecoration.Underline
                    )
                },
                supportingText = { item.description?.let { Text(it) } },
                leadingContent = {
                    Surface(shape = CircleShape) {
                        AsyncImage(
                            model = item.owner.avatarUrl,
                            modifier = Modifier.size(48.dp),
                            contentDescription = null,
                        )
                    }
                },
            )

            FlowRow(modifier = Modifier.padding(4.dp)) {
                item.topics.forEach {
                    AssistChip(
                        label = { Text(it) },
                        modifier = Modifier.padding(2.dp),
                        onClick = {}
                    )
                }
            }

            Row {
                Text(
                    text = "Updated ${vm.formatTimestamp(item.pushedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(4.dp)
                        .weight(1f)
                )

                Text(
                    text = item.language,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .padding(4.dp)
                        .weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GithubTopicsTheme {
        GithubTopicUI()
    }
}