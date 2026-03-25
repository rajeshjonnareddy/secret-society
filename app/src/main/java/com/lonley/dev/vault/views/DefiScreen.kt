package com.lonley.dev.vault.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lonley.dev.vault.model.PasswordEntry
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import java.text.NumberFormat
import java.util.Locale

private val chartColors = listOf(
    Color(0xFF6C63FF),
    Color(0xFFFF6584),
    Color(0xFF00C9A7),
    Color(0xFFFFB347),
    Color(0xFF4FC3F7),
    Color(0xFFBA68C8),
    Color(0xFFFF8A65),
    Color(0xFF81C784),
    Color(0xFFE57373),
    Color(0xFF64B5F6)
)

@Composable
fun DefiScreen(
    portfolioEntries: List<PasswordEntry>,
    onAddClick: () -> Unit
) {
    if (portfolioEntries.isEmpty()) {
        DefiEmptyState()
    } else {
        DefiContent(portfolioEntries)
    }
}

@Composable
private fun DefiEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "DeFi",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Portfolio",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(contentPadding = PaddingValues(24.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.CurrencyBitcoin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No investments yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Track your DeFi portfolio here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DefiContent(entries: List<PasswordEntry>) {
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale.US).apply {
            maximumFractionDigits = 2
        }
    }

    val holdings = remember(entries) {
        entries.mapNotNull { entry ->
            val amount = entry.tokenAmount?.toDoubleOrNull() ?: return@mapNotNull null
            val price = entry.tokenValueUsd?.toDoubleOrNull() ?: return@mapNotNull null
            val symbol = entry.tokenSymbol ?: return@mapNotNull null
            Holding(
                symbol = symbol,
                name = entry.name,
                amount = amount,
                priceUsd = price,
                totalUsd = amount * price,
                network = entry.network?.label,
                l2Network = entry.l2Network,
                createdAt = entry.createdAt
            )
        }
    }

    val totalValue = remember(holdings) { holdings.sumOf { it.totalUsd } }

    // Aggregate by symbol for the donut chart
    val aggregated = remember(holdings) {
        holdings.groupBy { it.symbol }
            .map { (symbol, group) ->
                symbol to group.sumOf { it.totalUsd }
            }
            .sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "DeFi",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Portfolio",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Summary card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp)
            ) {
                Column {
                    Text(
                        text = "Total Value",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currencyFormat.format(totalValue),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${holdings.size} ${if (holdings.size == 1) "holding" else "holdings"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Donut chart
        if (aggregated.size > 1) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Allocation",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                        DonutChart(
                            slices = aggregated.mapIndexed { index, (_, value) ->
                                DonutSlice(
                                    value = value.toFloat(),
                                    color = chartColors[index % chartColors.size]
                                )
                            },
                            modifier = Modifier
                                .size(180.dp)
                                .aspectRatio(1f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // Legend
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            aggregated.forEachIndexed { index, (symbol, value) ->
                                val pct = if (totalValue > 0) (value / totalValue * 100) else 0.0
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Canvas(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                    ) {
                                        drawCircle(chartColors[index % chartColors.size])
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = symbol,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "%.1f%%".format(pct),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Line chart — value over time
        if (holdings.size >= 2) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Value Timeline",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        PortfolioLineChart(
                            holdings = holdings,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Section header for token cards
        item {
            Text(
                text = "Holdings",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Token cards
        items(holdings, key = { "${it.symbol}_${it.createdAt}" }) { holding ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = holding.symbol,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = holding.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = currencyFormat.format(holding.totalUsd),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${holding.amount} ${holding.symbol}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "@${currencyFormat.format(holding.priceUsd)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val networkLabel = buildString {
                        holding.network?.let { append(it) }
                        holding.l2Network?.let {
                            if (isNotEmpty()) append(" / ")
                            append(it)
                        }
                    }
                    if (networkLabel.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = networkLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.18f
        val radius = (size.minDimension - strokeWidth) / 2f
        val topLeft = Offset(
            (size.width - radius * 2) / 2f,
            (size.height - radius * 2) / 2f
        )
        val arcSize = Size(radius * 2, radius * 2)
        var startAngle = -90f
        for (slice in slices) {
            val sweep = if (total > 0) (slice.value / total) * 360f else 0f
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun PortfolioLineChart(
    holdings: List<Holding>,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(holdings) {
        // Sort by creation date, accumulate value over time
        val sorted = holdings.sortedBy { it.createdAt }
        var cumulative = 0.0
        val values = sorted.map { h ->
            cumulative += h.totalUsd
            cumulative
        }
        if (values.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(values)
                }
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(),
        ),
        modelProducer = modelProducer,
        modifier = modifier
    )
}

private data class Holding(
    val symbol: String,
    val name: String,
    val amount: Double,
    val priceUsd: Double,
    val totalUsd: Double,
    val network: String?,
    val l2Network: String?,
    val createdAt: Long
)

private data class DonutSlice(
    val value: Float,
    val color: Color
)
