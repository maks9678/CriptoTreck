package com.example.criptotreck.ui.screens.news

import android.util.Log
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.criptotreck.data.model.CategoryNews
import com.example.criptotreck.data.model.News
import com.example.criptotreck.data.model.Portfolio
import com.example.criptotreck.data.repository.NewsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.time.Duration.Companion.milliseconds

data class NewsState(
    val listNews: List<News> = emptyList(),
    val portfolio: Portfolio? = null,
    val selectedCategory: CategoryNews? = null,
    val isLoading: Boolean = true,
) {
    fun filteredInSelectedCategory() = if (selectedCategory == null) {
        listNews
    } else {
        listNews.filter { it.categoryNews == selectedCategory }
    }
}

@OptIn(FlowPreview::class)
class NewsViewModel(
    private val repository: NewsRepository = NewsRepository()
) : ViewModel() {
    private val _errorChannel = Channel<String>()
    val errorChannel = _errorChannel.receiveAsFlow()
    private var loadJob: Job? = null
    private val _screenState = MutableStateFlow(NewsState())
    val searchQuery = MutableStateFlow("")

    val allNewsState: StateFlow<NewsState> = combine(
        _screenState,
        searchQuery.debounce(300.milliseconds)
    ) { state, inputText ->
        val categoryFiltered = state.filteredInSelectedCategory()
        val filterList = if (inputText.isBlank()) {
            categoryFiltered
        } else {
            categoryFiltered.filter { news ->
                news.titleText.contains(inputText, ignoreCase = true) ||
                        news.descriptionText.contains(inputText, ignoreCase = true)
            }
        }.sortedBy { it.minutesAgo }
        state.copy(listNews = filterList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NewsState()
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    init {
        refreshNews()
    }

    fun refreshNews() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _screenState.update { it.copy(isLoading = true) }

            supervisorScope {
                val portfolioDeferred = async { repository.loadPortfolioStats() }
                val newYorkDeferred = async { repository.loadNewYorkNewsList() }
                val mockDeferred = async { repository.loadMockNewsList() }
                val londonDeferred = async { repository.loadLondonNewsList() }
                val nyNews = runCatching { newYorkDeferred.await() }
                    .onFailure {
                        if (it is CancellationException) throw it
                        _errorChannel.send(it.message ?: "Ошибка загрузки Нью-Йорка")
                    }
                    .getOrDefault(emptyList())

                val mockNews = runCatching { mockDeferred.await() }
                    .getOrDefault(emptyList())

                val londonNews = runCatching { londonDeferred.await() }
                    .onFailure { _errorChannel.send(it.message ?: "Ошибка загрузки Лондона") }
                    .getOrDefault(emptyList())

                val mergeList = nyNews + mockNews + londonNews

                val mergePortfolio = runCatching { portfolioDeferred.await() }
                    .onFailure { _errorChannel.send("Не удалось загрузить баланс") }
                    .getOrNull()

                Log.i("NewsViewModel", "ответы получены")
                _screenState.update {
                    it.copy(
                        listNews = mergeList,
                        portfolio = mergePortfolio,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onCategorySelected(category: CategoryNews?) {
        _screenState.update { it.copy(selectedCategory = category) }
    }
}
