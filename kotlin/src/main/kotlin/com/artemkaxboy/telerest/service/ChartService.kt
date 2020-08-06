package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.business.ChartMessage
import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.service.storage.LiveDataService
import com.artemkaxboy.telerest.service.storage.TickerService
import com.artemkaxboy.telerest.tool.Result
import com.artemkaxboy.telerest.tool.extensions.toDate
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.CombinedDomainXYPlot
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.chart.ui.RectangleInsets
import org.jfree.data.time.Day
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.data.xy.XYDataset
import org.springframework.stereotype.Service
import java.awt.BasicStroke
import java.awt.Color
import java.time.LocalDate
import kotlin.math.PI

private const val CHART_GRID_COLOR = 0xcccccc
private const val CHART_BACKGROUND_COLOR = 0xffffff

private const val PRICE_COLOR = 0xff0000
private const val PRICE_STROKE = 3.0f
private const val PRICE_LEGEND = "price"

private const val FORECAST_COLOR = 0x0000ff
private const val FORECAST_STROKE = 2.0f
private const val FORECAST_LEGEND = "forecast"

private const val POTENTIAL_COLOR = 0xf4b40d
private const val POTENTIAL_STROKE = 3.0f
private const val POTENTIAL_LEGEND = "potential"

private const val DEFAULT_HISTORY_DAYS = 100

private const val CHARTS_GAP = 10.0
private const val MAIN_CHART_WEIGHT = 3
private const val SECONDARY_CHART_WEIGHT = 2

private const val CHART_HEIGHT = 300
private const val CHART_WIDTH = 600

@Service
class ChartService(
    private val liveDataService: LiveDataService,
    private val tickerService: TickerService
) {

    private val dateAxis = DateAxis()

    fun getChartMessage(ticker: String? = null, todayData: LiveData? = null): Result<ChartMessage> = Result.of {

        val resolvedTicker = todayData?.ticker
            ?: ticker?.let { tickerService.findById(it) }
            ?: return Result.failure("Ticker not found")

        val liveData = fetchLiveDataHistory(resolvedTicker.id)
            /* add the latest tick instead of last saved in DB */
            .let { history ->
                todayData?.let { history.dropLast(1) + it } ?: history
            }

        val datasets = generateChartDataCollections(liveData)

        val priceAndForecastXyPlot = generateTickerPriceAndForecastXyPlot(
            axisLabel = resolvedTicker.currency.sign,
            dataset = datasets.values
        )

        val potentialXyPlot = generateTickerPotentialXyPlot(datasets.potential)

        val combinedPlot = generateCombinedXyPlot(priceAndForecastXyPlot, potentialXyPlot)

        val chart = generateChart("${resolvedTicker.id}: ${resolvedTicker.name}", combinedPlot)

        val lastPair = liveData.takeLast(2)
        ChartMessage(chart, CHART_WIDTH, CHART_HEIGHT, lastPair.firstOrNull(), lastPair.lastOrNull())
    }

    private fun fetchLiveDataHistory(ticker: String, days: Int = DEFAULT_HISTORY_DAYS):
        MutableList<LiveData> {

        val now = LocalDate.now()
        val periodStart = now.minusDays(days.toLong())

        return liveDataService
            .findByTickerTickerAndDateBetweenOrderByDate(ticker, periodStart, now)
    }

    private fun generateChartDataCollections(data: List<LiveData>): ChartCollections {

        val price = TimeSeries(PRICE_LEGEND)
        val forecast = TimeSeries(FORECAST_LEGEND)
        val potential = TimeSeries(POTENTIAL_LEGEND)

        data.forEach { liveData ->
            val day = Day(liveData.date.toDate())

            price.add(day, liveData.price)
            forecast.add(day, liveData.consensus)
            potential.add(day, liveData.getPotential())
        }

        return ChartCollections(
            TimeSeriesCollection(price).apply { addSeries(forecast) },
            TimeSeriesCollection(potential)
        )
    }

    private fun generateTickerPriceAndForecastXyPlot(
        axisLabel: String,
        dataset: XYDataset
    ): XYPlot {
        val valuesAxis = NumberAxis(axisLabel)
            .apply {
                this.autoRangeIncludesZero = false
                this.labelAngle = PI / 2
                this.labelInsets = RectangleInsets.ZERO_INSETS
            }

        val valuesRenderer = XYLineAndShapeRenderer().apply {
            setSeriesPaint(0, Color(PRICE_COLOR))
            setSeriesStroke(0, BasicStroke(PRICE_STROKE))
            setSeriesPaint(1, Color(FORECAST_COLOR))
            setSeriesStroke(1, BasicStroke(FORECAST_STROKE))
        }

        return XYPlot(dataset, dateAxis, valuesAxis, valuesRenderer).apply {
            rangeGridlinePaint = Color(CHART_GRID_COLOR)
            domainGridlinePaint = Color(CHART_GRID_COLOR)
        }
    }

    private fun generateTickerPotentialXyPlot(
        dataset: XYDataset
    ): XYPlot {

        val percentAxis = NumberAxis("%")
            .apply {
                this.autoRangeIncludesZero = false
                this.labelAngle = PI / 2
            }

        val percentRenderer = XYLineAndShapeRenderer().apply {
            setSeriesPaint(0, Color(POTENTIAL_COLOR))
            setSeriesStroke(0, BasicStroke(POTENTIAL_STROKE))
        }

        return XYPlot(dataset, dateAxis, percentAxis, percentRenderer).apply {
            rangeGridlinePaint = Color(CHART_GRID_COLOR)
            domainGridlinePaint = Color(CHART_GRID_COLOR)
        }
    }

    private fun generateCombinedXyPlot(
        mainPlot: XYPlot,
        secondaryPlot: XYPlot
    ) = CombinedDomainXYPlot(dateAxis).apply {
        this.gap = CHARTS_GAP
        this.orientation = PlotOrientation.VERTICAL
        this.add(mainPlot, MAIN_CHART_WEIGHT)
        this.add(secondaryPlot, SECONDARY_CHART_WEIGHT)
    }

    private fun generateChart(
        caption: String,
        combinedPlot: CombinedDomainXYPlot
    ) = JFreeChart(
        caption,
        JFreeChart.DEFAULT_TITLE_FONT,
        combinedPlot,
        true
    ).apply {
        this.backgroundPaint = Color(CHART_BACKGROUND_COLOR)
    }
}

private class ChartCollections(val values: XYDataset, val potential: XYDataset)
