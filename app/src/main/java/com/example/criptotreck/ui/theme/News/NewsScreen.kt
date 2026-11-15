package com.example.criptotreck.ui.theme.News

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.criptotreck.ui.theme.crypto.CryptoAsset

@Composable
fun CategoryNews.getContainerColor(): Color {
    return when (this) {
        CategoryNews.BURSE -> Color(0xFF0878D5)  // Синий
        CategoryNews.DEFI -> Color(0xFF53E11C)   // Фиолетовый
        CategoryNews.SAFETY -> Color(0xFFFF0000) // Зеленый
    }
}

@Preview(showBackground = true)
@Composable
fun NewsPreview() {
    NewsScreenIternal(
        TextFieldState(),
        {},
        {},
        CategoryNews.DEFI,
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
            ),
        )
    )
}


@Composable
fun NewsScreen() {
    val viewModel: NewsViewModel = viewModel()
    val state by viewModel.allNewsState.collectAsStateWithLifecycle()

    NewsScreenIternal(
        viewModel.textField,
        viewModel::onCategorySelected,
        viewModel::refreshNews,
        state.selectedCategory,
        state.listNews,
        state.isLoading,
    )
}
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NewsScreenIternal(
        searchBarState: TextFieldState,
        onCategoryClick: (CategoryNews?) -> Unit,
        onRefreshClick: () -> Unit,
        selectCategory: CategoryNews?,
        listNews: List<News>,
        isLoading: Boolean = false,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Аналитика и Новости",
                            modifier = Modifier,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    },
                    actions = {
                        IconButton(
                            modifier = Modifier
                                .padding(0.dp)
                                .size(24.dp),
                            onClick = {onRefreshClick()},
                            content = {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Обновить"
                                )
                            }
                        )
                    },
                )
            },

            ) { paddingValues ->
            Column {
                Row(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {

                    Element(Modifier.weight(1f), "Портфель:", "$45,23")
                    Element(Modifier.weight(1f), "Прибыль:", "+12,4%")
                }
                OutlinedTextField(
                    state = searchBarState,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск"
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
                    modifier = Modifier
                        .padding()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = (category == selectCategory),
                            onClick = { onCategoryClick(category) },
                            label = {
                                Text(
                                    text = category?.title ?: "Все",
                                )
                            }
                        )
                    }

                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow // Или Color.White / surface
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        items(listNews) { news ->
                            CardNews(news)
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
        value: String,
    ) {
        Card(modifier = modifier.padding(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    textName,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    value,
                    fontWeight = FontWeight.Bold
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
                news.titleText,
                fontWeight = FontWeight.Bold
            )
            Text(
                news.descriptionText,
                fontWeight = FontWeight.Light
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                news.categoryNews?.let{categoryNews ->
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