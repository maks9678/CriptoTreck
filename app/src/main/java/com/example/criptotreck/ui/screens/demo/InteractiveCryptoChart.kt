package com.example.criptotreck.ui.screens.demo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Модель данных отдельной точки графика (цены, объём, дата, скользящее среднее MA20)
 */
data class ChartPoint(
    val timestampIndex: Int,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Float,
    val dateLabel: String,
    val ma20: Float = close
)

/**
 * Вид графика: Линейный или Японские свечи
 */
enum class ChartType {
    LINE, CANDLESTICK
}

/**
 * Временные интервалы (таймфреймы)
 */
enum class TimeFrame(val label: String, val pointCount: Int) {
    DAY_1("1Д", 24),
    WEEK_1("1Н", 40),
    MONTH_1("1М", 60),
    YEAR_1("1Г", 90),
    ALL("Всё", 120)
}

/**
 * Генератор реалистичных тестовых данных для криптовалютного графика (совместим с Android API 24+)
 * с поддержкой привязки к базовой цене конкретного актива.
 */
object ChartDataGenerator {
    fun generateData(timeFrame: TimeFrame, basePrice: Float = 92000f): List<ChartPoint> {
        val points = mutableListOf<ChartPoint>()
        // Корректируем стартовую цену так, чтобы последняя точка была близка к переданной basePrice
        var currentPrice = max(0.00001f, basePrice * (1f - ((timeFrame.pointCount / 2f) * 0.002f)))
        val random = Random(timeFrame.ordinal * 42 + basePrice.toInt())

        val calendar = Calendar.getInstance()
        val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dayMonthFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val yearMonthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

        val stepMillis: Long = when (timeFrame) {
            TimeFrame.DAY_1 -> 3600_000L
            TimeFrame.WEEK_1, TimeFrame.MONTH_1 -> 86400_000L
            TimeFrame.YEAR_1, TimeFrame.ALL -> 86400_000L * 3L
        }

        val startTime = System.currentTimeMillis() - (timeFrame.pointCount * stepMillis)

        for (i in 0 until timeFrame.pointCount) {
            val deltaPercent = (random.nextFloat() - 0.485f) * 0.03f
            val open = currentPrice
            val close = if (i == timeFrame.pointCount - 1) basePrice else open * (1f + deltaPercent)
            val spread = open * (0.003f + random.nextFloat() * 0.012f)
            val high = max(open, close) + spread * random.nextFloat()
            val low = max(0.00001f, min(open, close) - spread * random.nextFloat())
            val volume = 1500f + random.nextFloat() * 8500f
            currentPrice = close

            calendar.timeInMillis = startTime + (i * stepMillis)
            val dateStr = when (timeFrame) {
                TimeFrame.DAY_1 -> hourFormat.format(calendar.time)
                TimeFrame.WEEK_1, TimeFrame.MONTH_1 -> dayMonthFormat.format(calendar.time)
                TimeFrame.YEAR_1, TimeFrame.ALL -> yearMonthFormat.format(calendar.time)
            }

            points.add(
                ChartPoint(
                    timestampIndex = i,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = volume,
                    dateLabel = dateStr
                )
            )
        }

        // Расчёт скользящей средней MA (20 периодов)
        val result = mutableListOf<ChartPoint>()
        for (i in points.indices) {
            val startIndex = max(0, i - 19)
            val ma = points.subList(startIndex, i + 1).map { it.close }.average().toFloat()
            result.add(points[i].copy(ma20 = ma))
        }

        return result
    }
}

/**
 * Сложный интерактивный компоузабл графика криптовалюты, адаптированный под конкретный актив (coinSymbol, basePrice):
 * 1. Обработка касаний и жестов (pointerInput: перетаскивание прицела, масштабирование 2 пальцами, сдвиг)
 * 2. Управление состояниями анимаций (анимация прорисовки линии Animatable, пульсация активной точки, переключение таймфреймов)
 * 3. Кастомная отрисовка Canvas (сглаженная кривая Безье, градиентная заливка, японские свечи, гистограмма объёмов, MA20, перекрестье с тактильным откликом)
 */
@Composable
fun InteractiveCryptoChartScreen(
    modifier: Modifier = Modifier,
    coinSymbol: String = "BTC",
    basePrice: Float = 92000f,
    initialPriceChange: Float = 2.5f
) {
    var selectedTimeFrame by remember { mutableStateOf(TimeFrame.WEEK_1) }
    var chartType by remember { mutableStateOf(ChartType.LINE) }
    var showMA20 by remember { mutableStateOf(true) }
    var showVolume by remember { mutableStateOf(true) }

    // Данные графика с учетом базовой цены конкретной монеты
    var chartData by remember(selectedTimeFrame, coinSymbol) {
        mutableStateOf(ChartDataGenerator.generateData(selectedTimeFrame, basePrice))
    }

    // Скользящее окно в реальном времени при изменении цены (добавление новой точки справа, удаление старой слева)
    LaunchedEffect(basePrice) {
        if (chartData.isNotEmpty()) {
            val lastPoint = chartData.last()
            if (kotlin.math.abs(lastPoint.close - basePrice) > 0.00001f) {
                val newTimeIndex = lastPoint.timestampIndex + 1
                val spread = basePrice * 0.004f
                val newPoint = ChartPoint(
                    timestampIndex = newTimeIndex,
                    open = lastPoint.close,
                    high = max(lastPoint.close, basePrice) + spread * Random.nextFloat(),
                    low = max(0.00001f, min(lastPoint.close, basePrice) - spread * Random.nextFloat()),
                    close = basePrice,
                    volume = 1000f + Random.nextFloat() * 4000f,
                    dateLabel = "Live"
                )
                val updatedList = chartData.drop(1) + newPoint
                chartData = updatedList.mapIndexed { index, pt ->
                    val startIndex = max(0, index - 19)
                    val ma = updatedList.subList(startIndex, index + 1).map { it.close }.average().toFloat()
                    pt.copy(ma20 = ma)
                }
            }
        }
    }

    // Интерактивное состояние (выбранная точка при касании, масштаб, сдвиг)
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableFloatStateOf(0.0f) }

    // Состояние анимации прорисовки линии при смене данных (по умолчанию 1f, чтобы лайв-обновления не дергали анимацию)
    val animatableProgress = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    // Запуск анимации прорисовки ТОЛЬКО при переключении интервала или монеты
    LaunchedEffect(selectedTimeFrame, coinSymbol) {
        animatableProgress.snapTo(0f)
        animatableProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val currentPoint = selectedPointIndex?.let { index ->
        chartData.getOrNull(index)
    } ?: chartData.lastOrNull()

    val firstPoint = chartData.firstOrNull() ?: ChartPoint(0, 0f, 0f, 0f, 0f, 0f, "")
    val priceChange = (currentPoint?.close ?: basePrice) - firstPoint.open
    val priceChangePercent = if (firstPoint.open != 0f) (priceChange / firstPoint.open) * 100f else initialPriceChange
    val isPositive = priceChangePercent >= 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- Шапка: Курс в реальном времени и процент изменения ---
        ChartHeaderSection(
            coinSymbol = coinSymbol,
            currentPoint = currentPoint,
            priceChangePercent = priceChangePercent,
            isPositive = isPositive,
            onRefresh = {
                chartData = ChartDataGenerator.generateData(selectedTimeFrame, basePrice)
                scope.launch {
                    animatableProgress.snapTo(0f)
                    animatableProgress.animateTo(1f, tween(800))
                }
            }
        )

        // --- Панель управления: Таймфреймы, индикаторы и тип графика ---
        ChartControlsRow(
            selectedTimeFrame = selectedTimeFrame,
            onTimeFrameSelected = {
                selectedTimeFrame = it
                selectedPointIndex = null
                zoomScale = 1.0f
                panOffset = 0.0f
            },
            chartType = chartType,
            onChartTypeToggle = {
                chartType = if (chartType == ChartType.LINE) ChartType.CANDLESTICK else ChartType.LINE
            },
            showMA20 = showMA20,
            onToggleMA20 = { showMA20 = !showMA20 },
            showVolume = showVolume,
            onToggleVolume = { showVolume = !showVolume }
        )

        // --- Главная область интерактивного Canvas ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                ChartCanvas(
                    data = chartData,
                    chartType = chartType,
                    showMA20 = showMA20,
                    showVolume = showVolume,
                    animationProgress = animatableProgress.value,
                    zoomScale = zoomScale,
                    panOffset = panOffset,
                    selectedIndex = selectedPointIndex,
                    onIndexSelected = { index -> selectedPointIndex = index },
                    onTransform = { _, pan, zoom ->
                        zoomScale = (zoomScale * zoom).coerceIn(1.0f, 4.0f)
                        panOffset = (panOffset + pan.x).coerceIn(-1000f, 1000f)
                    }
                )

                // Подсказка по жестам управления
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Удерживайте палец • Pinch-zoom для масштаба",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // --- Нижняя панель с детальной информацией выбранной точки ---
        ScrubberDetailsFooter(
            selectedPoint = currentPoint,
            isScrubbing = selectedPointIndex != null,
            onResetScrub = { selectedPointIndex = null }
        )
    }
}

/**
 * Верхняя секция с информацией об активе и динамике цены
 */
@Composable
private fun ChartHeaderSection(
    coinSymbol: String,
    currentPoint: ChartPoint?,
    priceChangePercent: Float,
    isPositive: Boolean,
    onRefresh: () -> Unit
) {
    val priceValue = currentPoint?.close ?: 0f
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val formattedPrice = remember(priceValue) {
        if (priceValue < 1f) {
            "$${String.format(Locale.US, "%.5f", priceValue)}"
        } else {
            currencyFormatter.format(priceValue.toDouble())
        }
    }

    val changeColor = if (isPositive) Color(0xFF00C853) else Color(0xFFFF3D00)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$coinSymbol / USDT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = coinSymbol,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formattedPrice,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = changeColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%.2f", priceChangePercent)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = changeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Обновить данные",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Ряд чипов переключения таймфреймов и параметров отображения
 */
@Composable
private fun ChartControlsRow(
    selectedTimeFrame: TimeFrame,
    onTimeFrameSelected: (TimeFrame) -> Unit,
    chartType: ChartType,
    onChartTypeToggle: () -> Unit,
    showMA20: Boolean,
    onToggleMA20: () -> Unit,
    showVolume: Boolean,
    onToggleVolume: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Переключение таймфреймов
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TimeFrame.entries.forEach { tf ->
                    val selected = tf == selectedTimeFrame
                    FilterChip(
                        selected = selected,
                        onClick = { onTimeFrameSelected(tf) },
                        label = {
                            Text(
                                text = tf.label,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.height(32.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Переключение типа графика (Линейный / Свечи)
            IconButton(
                onClick = onChartTypeToggle,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = if (chartType == ChartType.LINE) Icons.Default.CandlestickChart else Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = "Сменить вид графика",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Переключатели технических индикаторов
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = showMA20,
                onClick = onToggleMA20,
                label = { Text("MA (20)", fontSize = 11.sp) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB300))
                    )
                },
                modifier = Modifier.height(28.dp)
            )

            FilterChip(
                selected = showVolume,
                onClick = onToggleVolume,
                label = { Text("Объём", fontSize = 11.sp) },
                modifier = Modifier.height(28.dp)
            )
        }
    }
}

/**
 * Кастомная отрисовка Canvas с обработкой сложных жестов (pointerInput)
 */
@Composable
private fun ChartCanvas(
    data: List<ChartPoint>,
    chartType: ChartType,
    showMA20: Boolean,
    showVolume: Boolean,
    animationProgress: Float,
    zoomScale: Float,
    panOffset: Float,
    selectedIndex: Int?,
    onIndexSelected: (Int?) -> Unit,
    onTransform: (centroid: Offset, pan: Offset, zoom: Float) -> Unit
) {
    if (data.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val haptic = LocalHapticFeedback.current
    var lastHapticIndex by remember { mutableIntStateOf(-1) }

    // Анимация пульсации активного маркера при касании
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(data) {
                // Обработка жестов масштабирования (pinch zoom) и сдвига (pan) 2 пальцами
                detectTransformGestures { centroid, pan, zoom, _ ->
                    onTransform(centroid, pan, zoom)
                }
            }
            .pointerInput(data, zoomScale, panOffset) {
                val paddingRight = 110f
                val chartWidth = size.width.toFloat() - paddingRight

                // Обработка касания и скроллинга пальцем для прицела/перекрестья
                detectDragGestures(
                    onDragStart = { offset ->
                        val index = calculateClosestPointIndex(
                            touchX = offset.x,
                            chartWidth = chartWidth,
                            dataSize = data.size,
                            zoomScale = zoomScale,
                            panOffset = panOffset
                        )
                        onIndexSelected(index)
                        if (index != lastHapticIndex) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            lastHapticIndex = index
                        }
                    },
                    onDrag = { change, _ ->
                        val index = calculateClosestPointIndex(
                            touchX = change.position.x,
                            chartWidth = chartWidth,
                            dataSize = data.size,
                            zoomScale = zoomScale,
                            panOffset = panOffset
                        )
                        onIndexSelected(index)
                        if (index != lastHapticIndex && index in data.indices) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            lastHapticIndex = index
                        }
                    },
                    onDragEnd = {},
                    onDragCancel = {}
                )
            }
    ) {
        val width = size.width
        val height = size.height

        val paddingRight = 110f // Место для меток цены по оси Y
        val paddingBottom = 40f // Место для дат по оси X
        val chartWidth = width - paddingRight
        val chartHeight = height - paddingBottom

        val minPrice = data.minOf { it.low }
        val maxPrice = data.maxOf { it.high }
        val priceRange = max(0.00001f, maxPrice - minPrice)

        val maxVolume = data.maxOf { it.volume }

        // --- 1. Отрисовка горизонтальной сетки и оси Y (цены) ---
        drawGridAndYAxis(
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            minPrice = minPrice,
            maxPrice = maxPrice,
            textMeasurer = textMeasurer
        )

        // Применение трансформ-масштабирования и панорамирования для графика
        withTransform({
            translate(left = panOffset, top = 0f)
            scale(scaleX = zoomScale, scaleY = 1f, pivot = Offset(chartWidth / 2f, chartHeight / 2f))
        }) {

            // --- 2. Отрисовка гистограммы объёмов ---
            if (showVolume) {
                drawVolumeBars(
                    data = data,
                    chartWidth = chartWidth,
                    chartHeight = chartHeight,
                    maxVolume = maxVolume,
                    animationProgress = animationProgress
                )
            }

            // Вспомогательные функции масштабирования координат
            fun getX(index: Int): Float {
                val step = chartWidth / max(1, data.size - 1)
                return index * step
            }

            fun getY(price: Float): Float {
                val normalized = (price - minPrice) / priceRange
                return chartHeight - (normalized * chartHeight)
            }

            // --- 3. Отрисовка графика (Линейный или Свечи) ---
            if (chartType == ChartType.LINE) {
                drawLineChart(
                    data = data,
                    chartWidth = chartWidth,
                    chartHeight = chartHeight,
                    animationProgress = animationProgress,
                    getX = ::getX,
                    getY = ::getY
                )
            } else {
                drawCandlesticks(
                    data = data,
                    chartWidth = chartWidth,
                    animationProgress = animationProgress,
                    getX = ::getX,
                    getY = ::getY
                )
            }

            // --- 4. Отрисовка линии скользящей средней MA20 ---
            if (showMA20) {
                drawMA20Line(
                    data = data,
                    chartWidth = chartWidth,
                    animationProgress = animationProgress,
                    getX = ::getX,
                    getY = ::getY
                )
            }

            // --- 5. Отрисовка подписей дат по оси X ---
            drawXAxisDateLabels(
                data = data,
                chartHeight = chartHeight,
                getX = ::getX,
                textMeasurer = textMeasurer
            )

            // --- 6. Отрисовка интерактивного прицела (перекрестья) и маркера точки ---
            if (selectedIndex != null && selectedIndex in data.indices) {
                val selectedPoint = data[selectedIndex]
                val crosshairX = getX(selectedIndex)
                val crosshairY = getY(selectedPoint.close)

                // Вертикальная пунктирная линия прицела
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(crosshairX, 0f),
                    end = Offset(crosshairX, chartHeight),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Горизонтальная пунктирная линия цены
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(0f, crosshairY),
                    end = Offset(chartWidth, crosshairY),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Пульсирующий внешний ореол точки
                drawCircle(
                    color = Color(0xFF2979FF).copy(alpha = 0.35f),
                    radius = pulseRadius,
                    center = Offset(crosshairX, crosshairY)
                )
                drawCircle(
                    color = Color(0xFF2979FF),
                    radius = 6f,
                    center = Offset(crosshairX, crosshairY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f,
                    center = Offset(crosshairX, crosshairY)
                )

                // Плашка с точной ценой на оси Y справа
                val priceText = if (selectedPoint.close < 1f) String.format(Locale.US, "$%.5f", selectedPoint.close) else String.format(Locale.US, "$%.2f", selectedPoint.close)
                val badgeWidth = 100f
                val badgeHeight = 26f
                val badgeLeft = chartWidth + 6f
                val badgeTop = (crosshairY - badgeHeight / 2f).coerceIn(0f, chartHeight - badgeHeight)

                drawRoundRect(
                    color = Color(0xFF2979FF),
                    topLeft = Offset(badgeLeft, badgeTop),
                    size = Size(badgeWidth, badgeHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = priceText,
                    topLeft = Offset(badgeLeft + 8f, badgeTop + 4f),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

/**
 * Расчёт индекса ближайшей точки на основе X-координаты касания с учётом ширины графика, отступа оси Y, масштаба и сдвига
 */
private fun calculateClosestPointIndex(
    touchX: Float,
    chartWidth: Float,
    dataSize: Int,
    zoomScale: Float = 1.0f,
    panOffset: Float = 0.0f
): Int {
    if (dataSize <= 1 || chartWidth <= 0f) return 0

    val unscaledX = (touchX - panOffset - chartWidth / 2f) / zoomScale + chartWidth / 2f
    val step = chartWidth / (dataSize - 1)
    val index = kotlin.math.round(unscaledX / step).toInt()
    return index.coerceIn(0, dataSize - 1)
}

/**
 * Отрисовка горизонтальной сетки и шкалы цен оси Y
 */
private fun DrawScope.drawGridAndYAxis(
    chartWidth: Float,
    chartHeight: Float,
    minPrice: Float,
    maxPrice: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val gridLinesCount = 5
    val priceRange = max(0.00001f, maxPrice - minPrice)
    val priceStep = priceRange / (gridLinesCount - 1)

    for (i in 0 until gridLinesCount) {
        val y = chartHeight - (i * (chartHeight / (gridLinesCount - 1)))
        val price = minPrice + (i * priceStep)

        drawLine(
            color = Color.Gray.copy(alpha = 0.15f),
            start = Offset(0f, y),
            end = Offset(chartWidth, y),
            strokeWidth = 1f
        )

        val labelText = if (price < 1f) String.format(Locale.US, "$%.4f", price) else String.format(Locale.US, "$%.0f", price)
        drawText(
            textMeasurer = textMeasurer,
            text = labelText,
            topLeft = Offset(chartWidth + 10f, y - 10f),
            style = TextStyle(
                color = Color.Gray.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        )
    }
}

/**
 * Отрисовка сглаженной кривой Безье с градиентом под графиком и анимацией появления
 */
private fun DrawScope.drawLineChart(
    data: List<ChartPoint>,
    chartWidth: Float,
    chartHeight: Float,
    animationProgress: Float,
    getX: (Int) -> Float,
    getY: (Float) -> Float
) {
    if (data.size < 2) return

    val linePath = Path()
    val fillPath = Path()

    val startX = getX(0)
    val startY = getY(data[0].close)

    linePath.moveTo(startX, startY)
    fillPath.moveTo(startX, chartHeight)
    fillPath.lineTo(startX, startY)

    for (i in 0 until data.size - 1) {
        val p1X = getX(i)
        val p1Y = getY(data[i].close)
        val p2X = getX(i + 1)
        val p2Y = getY(data[i + 1].close)

        val cx1 = p1X + (p2X - p1X) / 2f
        val cx2 = p1X + (p2X - p1X) / 2f

        linePath.cubicTo(cx1, p1Y, cx2, p2Y, p2X, p2Y)
        fillPath.cubicTo(cx1, p1Y, cx2, p2Y, p2X, p2Y)
    }

    val lastX = getX(data.size - 1)
    fillPath.lineTo(lastX, chartHeight)
    fillPath.close()

    val isUp = data.last().close >= data.first().close
    val primaryColor = if (isUp) Color(0xFF00E676) else Color(0xFFFF3D00)

    clipRect(right = chartWidth * animationProgress) {
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.35f),
                    primaryColor.copy(alpha = 0.0f)
                ),
                startY = 0f,
                endY = chartHeight
            )
        )

        drawPath(
            path = linePath,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

/**
 * Отрисовка японских свечей (тело свечи + фитили)
 */
private fun DrawScope.drawCandlesticks(
    data: List<ChartPoint>,
    chartWidth: Float,
    animationProgress: Float,
    getX: (Int) -> Float,
    getY: (Float) -> Float
) {
    val candleWidth = max(2f, (chartWidth / data.size) * 0.6f)

    clipRect(right = chartWidth * animationProgress) {
        data.forEachIndexed { index, point ->
            val x = getX(index)
            val openY = getY(point.open)
            val closeY = getY(point.close)
            val highY = getY(point.high)
            val lowY = getY(point.low)

            val isBullish = point.close >= point.open
            val color = if (isBullish) Color(0xFF00C853) else Color(0xFFFF3D00)

            drawLine(
                color = color,
                start = Offset(x, highY),
                end = Offset(x, lowY),
                strokeWidth = 1.5f
            )

            val topY = min(openY, closeY)
            val bottomY = max(openY, closeY)
            val bodyHeight = max(2f, bottomY - topY)

            drawRoundRect(
                color = color,
                topLeft = Offset(x - candleWidth / 2f, topY),
                size = Size(candleWidth, bodyHeight),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
    }
}

/**
 * Отрисовка пунктирной линии скользящего среднего MA20
 */
private fun DrawScope.drawMA20Line(
    data: List<ChartPoint>,
    chartWidth: Float,
    animationProgress: Float,
    getX: (Int) -> Float,
    getY: (Float) -> Float
) {
    if (data.size < 2) return
    val maPath = Path()

    maPath.moveTo(getX(0), getY(data[0].ma20))
    for (i in 1 until data.size) {
        maPath.lineTo(getX(i), getY(data[i].ma20))
    }

    clipRect(right = chartWidth * animationProgress) {
        drawPath(
            path = maPath,
            color = Color(0xFFFFB300),
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )
        )
    }
}

/**
 * Отрисовка гистограммы объёмов торгов
 */
private fun DrawScope.drawVolumeBars(
    data: List<ChartPoint>,
    chartWidth: Float,
    chartHeight: Float,
    maxVolume: Float,
    animationProgress: Float
) {
    val maxBarHeight = chartHeight * 0.22f
    val barWidth = max(2f, (chartWidth / data.size) * 0.5f)

    clipRect(right = chartWidth * animationProgress) {
        data.forEachIndexed { index, point ->
            val step = chartWidth / max(1, data.size - 1)
            val x = index * step
            val barHeight = (point.volume / maxVolume) * maxBarHeight
            val isUp = point.close >= point.open
            val color = if (isUp) Color(0xFF00C853).copy(alpha = 0.25f) else Color(0xFFFF3D00).copy(alpha = 0.25f)

            drawRoundRect(
                color = color,
                topLeft = Offset(x - barWidth / 2f, chartHeight - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
    }
}

/**
 * Отрисовка меток дат по оси X
 */
private fun DrawScope.drawXAxisDateLabels(
    data: List<ChartPoint>,
    chartHeight: Float,
    getX: (Int) -> Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val labelStep = max(1, data.size / 5)

    for (i in data.indices step labelStep) {
        val x = getX(i)
        val label = data[i].dateLabel

        drawText(
            textMeasurer = textMeasurer,
            text = label,
            topLeft = Offset(x - 20f, chartHeight + 8f),
            style = TextStyle(
                color = Color.Gray.copy(alpha = 0.7f),
                fontSize = 9.sp
            )
        )
    }
}

/**
 * Нижняя карточка с детальной информацией о выбранной точке графика
 */
@Composable
private fun ScrubberDetailsFooter(
    selectedPoint: ChartPoint?,
    isScrubbing: Boolean,
    onResetScrub: () -> Unit
) {
    if (selectedPoint == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isScrubbing) "Точка (${selectedPoint.dateLabel}):" else "Текущие данные:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailItem(label = "Откр", value = if (selectedPoint.open < 1f) String.format(Locale.US, "$%.4f", selectedPoint.open) else String.format(Locale.US, "$%.2f", selectedPoint.open))
                    DetailItem(label = "Макс", value = if (selectedPoint.high < 1f) String.format(Locale.US, "$%.4f", selectedPoint.high) else String.format(Locale.US, "$%.2f", selectedPoint.high))
                    DetailItem(label = "Мин", value = if (selectedPoint.low < 1f) String.format(Locale.US, "$%.4f", selectedPoint.low) else String.format(Locale.US, "$%.2f", selectedPoint.low))
                    DetailItem(label = "Закр", value = if (selectedPoint.close < 1f) String.format(Locale.US, "$%.4f", selectedPoint.close) else String.format(Locale.US, "$%.2f", selectedPoint.close))
                    DetailItem(label = "Объём", value = String.format(Locale.US, "%.0f", selectedPoint.volume))
                }
            }

            if (isScrubbing) {
                IconButton(onClick = onResetScrub, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Сбросить выбор точки",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 9.sp, color = Color.Gray)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
