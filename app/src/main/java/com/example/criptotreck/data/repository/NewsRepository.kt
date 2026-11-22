package com.example.criptotreck.data.repository

import android.util.Log
import com.example.criptotreck.data.model.CategoryNews
import com.example.criptotreck.data.model.News
import com.example.criptotreck.data.model.Portfolio
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class NewsRepository {
    suspend fun loadPortfolioStats(): Portfolio {
        delay(3000.milliseconds)
        return Portfolio(45.23, 12.4)
    }

    suspend fun loadNewYorkNewsList(): List<News> {
        delay(7000.milliseconds)
        Log.i("NewsRepository", "loadNewYorkNewsList")
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
                )
            )
        } else {
            throw Exception("Сервер нью-йорка времено не доступен")
        }
    }

    suspend fun loadLondonNewsList(): List<News> {
        delay(2000.milliseconds)
        Log.i("NewsRepository", "loadLondonNewsList")
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
            )
        )
    }

    suspend fun loadMockNewsList(): List<News> {
        delay(5000.milliseconds)
        Log.i("NewsRepository", "loadMockNewsList")
        return listOf(
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
            News(
                idNews = "7",
                titleText = "Кредитный протокол Aave запускает новый пул",
                descriptionText = "Теперь под залог стейблкоинов можно получить ликвидность с минимальным процентом.",
                categoryNews = CategoryNews.DEFI,
                timePublication = "4 часа назад"
            ),
            News(
                idNews = "8",
                titleText = "Новая волна фишинга через фейковые аирдропы",
                descriptionText = "Эксперты предупреждают: не подписывайте подозрительные транзакции в своем кошельке.",
                categoryNews = CategoryNews.SAFETY,
                timePublication = "20 мин. назад"
            )
        )
    }
}
