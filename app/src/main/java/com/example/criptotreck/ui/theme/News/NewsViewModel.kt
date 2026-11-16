package com.example.criptotreck.ui.theme.News

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

enum class CategoryNews(val title: String) {
    BURSE("Рынок"),
    DEFI("DeFi"),
    SAFETY("Безопасность"),
}

data class Portfolio(
    val cash: Double = 0.0,
    val percent: Double = 0.0,
)

data class News(
    val idNews: String = "",
    val titleText: String = "",
    val descriptionText: String = "",
    val categoryNews: CategoryNews? = null,
    val timePublication: String = "",
    val minutesAgo: Int = 0,
)

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

class NewsViewModel() : ViewModel() {
    private val _errorChannel = Channel<String>()
    val errorChannel = _errorChannel.receiveAsFlow()
    private var loadJob: Job? = null
    private val _screenState = MutableStateFlow(NewsState())
    val textField = TextFieldState()

    val allNewsState: StateFlow<NewsState> = combine(
        _screenState,
        snapshotFlow { textField.text.toString() }.debounce(300.milliseconds)
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

    init {
        refreshNews()
    }

    fun refreshNews() {

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _screenState.update { it.copy(isLoading = true) }

            supervisorScope {
                val portfolioDeferred = async { loadPortfolioStats() }
                val newYorkDeferred = async { loadNewYorkNewsList() }
                val mockDeferred = async { loadMockNewsList() }
                val londonDeferred = async { loadLondonNewsList() }
                val nyNews = runCatching { newYorkDeferred.await() }
                    .onFailure { if(it is CancellationException) throw it
                        _errorChannel.send(it.message ?: "Ошибка загрузки Нью-Йорка") }
                    .getOrDefault(emptyList())

                val mockNews = runCatching { mockDeferred.await() }
                    .getOrDefault(emptyList())

                val londonNews = runCatching { londonDeferred.await() }
                    .onFailure { _errorChannel.send(it.message ?: "Ошибка загрузки Лондона") }
                    .getOrDefault(emptyList())

                val mergeList = nyNews+mockNews+londonNews

                val mergePortfolio = runCatching { portfolioDeferred.await() }.onFailure{_errorChannel.send("Не удалось загрузить баланс") }.getOrNull()
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

suspend fun loadPortfolioStats(): Portfolio {
    delay(3000.milliseconds)
    return Portfolio(
        45.23,
        12.4
    )
}

suspend fun loadNewYorkNewsList(): List<News> {
    delay(7000.milliseconds)
    Log.i("NewsViewModel", "loadNewYorkNewsList")
    return if (Random.nextBoolean()) {
        listOf(
            News(
                idNews = "1",
                titleText = "Ethereum готовится к крупному ралли",
                descriptionText = "Торговые объемы ETH на спотовых биржах выросли на 40% за последние сутки.",
                categoryNews = CategoryNews.BURSE,
                timePublication = "35 мин. назад"

            ),
            News(
                idNews = "2",
                titleText = "Как защитить сид-фразу от вредоносного ПО",
                descriptionText = "Разбираем лучшие практики хранения ключей и настройки аппаратных кошельков.",
                categoryNews = CategoryNews.SAFETY,
                timePublication = "6 часов назад"
            ),

            )
    } else throw Exception("Сервер нью-йорка времено не доступен")
}

suspend fun loadLondonNewsList(): List<News> {
    delay(2000.milliseconds)
    Log.i("NewsViewModel", "loadMockNewsList")
    return listOf(
        News(
            idNews = "3",
            titleText = "Обнаружена уязвимость в популярном мосте",
            descriptionText = "Разработчики вовремя остановили смарт-контракт и спасли миллионы долларов пользователей.",
            categoryNews = CategoryNews.SAFETY,
            timePublication = "3 часа назад"
        ),

        News(
            idNews = "4",
            titleText = "Solana обогнала конкурентов по объемам",
            descriptionText = "Активность трейдеров в сети SOL бьет рекорды третью неделю подряд.",
            categoryNews = CategoryNews.BURSE,
            timePublication = "2 часа назад"
        ),
    )
}

suspend fun loadMockNewsList(): List<News> {
    delay(5000.milliseconds)
    Log.i("NewsViewModel", "loadMockNewsList")
    return listOf(
// --- РЫНОК (BURSE) ---
        News(
            idNews = "5",
            titleText = "Биткоин обновил годовой максимум",
            descriptionText = "Цена BTC уверенно закрепилась выше психологической отметки на фоне притока капитала.",
            categoryNews = CategoryNews.BURSE,
            timePublication = "5 мин. назад"
        ),

        News(
            idNews = "6",
            titleText = "Аналитики ждут снижения ставки ФРС",
            descriptionText = "Макроэкономические показатели дают зеленый свет для роста рисковых активов и крипторынка.",
            categoryNews = CategoryNews.BURSE,
            timePublication = "5 часов назад"
        ),

// --- DEFI ---

        News(
            idNews = "7",
            titleText = "Кредитный протокол Aave запускает новый пул",
            descriptionText = "Теперь под залог стейблкоинов можно получить ликвидность с минимальным процентом.",
            categoryNews = CategoryNews.DEFI,
            timePublication = "4 часа назад"
        ),

// --- БЕЗОПАСНОСТЬ (SAFETY) ---
        News(
            idNews = "8",
            titleText = "Новая волна фишинга через фейковые аирдропы",
            descriptionText = "Эксперты предупреждают: не подписывайте подозрительные транзакции в своем кошельке.",
            categoryNews = CategoryNews.SAFETY,
            timePublication = "20 мин. назад"
        ),
    )
}