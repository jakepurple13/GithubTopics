package com.programmersbox.githubtopics

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GithubTopicsTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GithubTopicUI(vm = viewModel { TopicViewModel(store = topics) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun GithubTopicUI(vm: TopicViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val state = rememberLazyListState()
    val showButton by remember { derivedStateOf { state.firstVisibleItemIndex > 0 } }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    BackHandler(drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerContent = { ModalDrawerSheet { TopicDrawer(vm) } },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    navigationIcon = {
                        IconsButton(
                            onClick = { scope.launch { drawerState.open() } },
                            icon = Icons.Default.Menu
                        )
                    },
                    title = { Text(text = "Github Topics") },
                    actions = {
                        AnimatedVisibility(visible = showButton) {
                            IconsButton(
                                onClick = { scope.launch { state.animateScrollToItem(0) } },
                                icon = Icons.Default.ArrowUpward
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { padding ->
            val pullRefreshState = rememberPullRefreshState(refreshing = vm.isLoading, onRefresh = vm::refresh)
            Box(
                modifier = Modifier
                    .padding(padding)
                    .pullRefresh(pullRefreshState)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize(),
                    state = state
                ) { items(vm.items) { TopicItem(it, vm.topicList, vm.currentTopics, vm::addTopic) } }

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
fun TopicItem(
    item: GitHubTopic,
    savedTopics: List<String>,
    currentTopics: List<String>,
    onTopicClick: (String) -> Unit
) {
    val context = LocalContext.current
    OutlinedCard(
        onClick = { context.openWebPage(item.htmlUrl) }
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            ListItem(
                headlineText = { Text(item.name) },
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
                trailingContent = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null)
                            Text(item.stars.toString())
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ForkLeft, contentDescription = null)
                            Text(item.forks.toString())
                        }
                    }
                }
            )

            FlowRow(modifier = Modifier.padding(4.dp)) {
                item.topics.forEach {
                    AssistChip(
                        label = { Text(it) },
                        modifier = Modifier.padding(2.dp),
                        onClick = { onTopicClick(it) },
                        leadingIcon = if (it in currentTopics) {
                            { Icon(Icons.Default.CatchingPokemon, null, modifier = Modifier.rotate(180f)) }
                        } else null,
                        border = AssistChipDefaults.assistChipBorder(
                            borderColor = when (it) {
                                in savedTopics -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        )
                    )
                }
            }

            item.license?.let {
                Text(
                    it.name,
                    modifier = Modifier.padding(4.dp)
                )
            }

            Row {
                Text(
                    text = item.pushedAt,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDrawer(vm: TopicViewModel) {
    var topicText by remember { mutableStateOf("") }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Topics") }) },
        bottomBar = {
            BottomAppBar {
                OutlinedTextField(
                    value = topicText,
                    onValueChange = { topicText = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            vm.addTopic(topicText)
                            topicText = ""
                        }
                    ),
                    trailingIcon = {
                        IconsButton(
                            onClick = {
                                vm.addTopic(topicText)
                                topicText = ""
                            },
                            icon = Icons.Default.Add
                        )
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(vm.topicList) {
                NavigationDrawerItem(
                    modifier = Modifier.padding(horizontal = 2.dp),
                    label = { Text(it) },
                    selected = it in vm.currentTopics,
                    onClick = { vm.setTopic(it) },
                    badge = { IconsButton(onClick = { vm.removeTopic(it) }, icon = Icons.Default.Close) }
                )
            }
        }
    }
}

@Composable
fun IconsButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) = IconButton(onClick, modifier, enabled, colors, interactionSource) { Icon(icon, null) }

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GithubTopicsTheme {
        GithubTopicUI()
    }
}