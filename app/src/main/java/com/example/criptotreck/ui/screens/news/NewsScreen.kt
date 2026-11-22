package com.example.criptotreck.ui.screens.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.criptotreck.data.model.CategoryNews
import com.example.criptotreck.data.model.News
import com.example.criptotreck.data.model.Portfolio

@Preview(showBackground = true)
@Composable
fun NewsPreview() {
    NewsScreenIternal(
        TextFieldState(),
        {},
        {},
        CategoryNews.DEFI,
        Portfolio(12.45, 10.2),
        listOf(
            News(
                titleText = "Ethereum готовится к крупному ралли",
                descriptionText = "Торговые объемы ETH на спотовых биржах выросли на 40% за последние сутки.",
                categoryNews = CategoryNews.BURSE,
                timePublication = "35 мин. назад"
            ),
            News(
                titleText = "Как защитить сид-фразу от вредоносного ПО",
                descriptionText = "Разбираем лучшие практики хранения ключей и настройки аппаратных кошельков.",
                categoryNews = CategoryNews.SAFETY,
                timePublication = "6 часов назад"
            )
        ),
        true,
        SnackbarHostState()
    )
}

@Composable
fun NewsScreen() {
    val viewModel: NewsViewModel = viewModel()
    val state by viewModel.allNewsState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorChannel.collect { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage, actionLabel = "Ok"
            )
        }
    }
    NewsScreenIternal(
        viewModel.textField,
        viewModel::onCategorySelected,
        viewModel::refreshNews,
        state.selectedCategory,
        state.portfolio,
        state.listNews,
        state.isLoading,
        snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreenIternal(
    searchBarState: TextFieldState,
    onCategoryClick: (CategoryNews?) -> Unit,
    onRefreshClick: () -> Unit,
    selectCategory: CategoryNews?,
    portfolio: Portfolio?,
    listNews: List<News>,
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Аналитика и Новости",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                actions = {
                    IconButton(
                        onClick = { onRefreshClick() },
                        content = {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Обновить"
                            )
                        }
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column {
            Row(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                val signPercent = if ((portfolio?.percent ?: 0.0) >= 0) "+" else ""
                Element(Modifier.weight(1f), "Портфель:", "$${portfolio?.cash ?: 0.0}")
                Element(
                    Modifier.weight(1f), "Прибыль:", "$signPercent${portfolio?.percent ?: 0.0}%"
                )
            }
            OutlinedTextField(
                state = searchBarState,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search, contentDescription = "Поиск"
                    )
                },
                placeholder = { Text("Поиск по новостям...") },
                lineLimits = TextFieldLineLimits.SingleLine,
                trailingIcon = {
                    if (searchBarState.text.isNotEmpty()) {
                        IconButton(onClick = { searchBarState.clearText() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                }
            )
            val categories = listOf(null) + CategoryNews.entries
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = (category == selectCategory),
                        onClick = { onCategoryClick(category) },
                        label = {
                            Text(
                                text = category?.title ?: "Все"
                            )
                        }
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (listNews.isEmpty()) {
                    Text(
                        "Ничего не найдено", textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        itemsIndexed(
                            items = listNews, key = { _, news -> news.idNews }
                        ) { index, news ->
                            CardNews(news = news)
                            if (index < listNews.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Element(
    modifier: Modifier,
    textName: String,
    value: String
) {
    Card(modifier = modifier.padding(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                textName, fontWeight = FontWeight.Bold
            )
            Text(
                value, fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CardNews(news: News) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            news.titleText, fontWeight = FontWeight.Bold
        )
        Text(
            news.descriptionText, fontWeight = FontWeight.Light
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            news.categoryNews?.let { categoryNews ->
                Text(
                    text = categoryNews.title,
                    color = categoryNews.getContainerColor(),
                    fontSize = 12.sp
                )
            }

            Text(text = news.timePublication)
        }
    }
}
