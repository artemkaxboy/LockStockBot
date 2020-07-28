package com.artemkaxboy.telerest.business

import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.tool.Emoji
import com.artemkaxboy.telerest.tool.Result
import com.artemkaxboy.telerest.tool.extensions.round
import com.artemkaxboy.telerest.tool.extensions.roundIfNeeded
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import java.io.ByteArrayOutputStream
import kotlin.math.absoluteValue

class ChartMessage(
    private val chart: JFreeChart,
    private val width: Int,
    private val height: Int,
    prevTick: LiveData? = null,
    lastTick: LiveData? = null,
    val caption: String = generateChartMessage(prevTick, lastTick)
) {

    fun getByteArray(): Result<ByteArray> {
        val byteOutputStream = ByteArrayOutputStream()

        Result.of {
            ChartUtils.writeChartAsPNG(byteOutputStream, chart, width, height)
        }.onFailure {
            return Result.failure("Cannot generate chart: $it")
        }

        return Result.success(byteOutputStream.toByteArray())
            .also { byteOutputStream.close() }
    }
}

private fun generateChartMessage(prevTick: LiveData?, lastTick: LiveData?): String {

    if (prevTick == null || lastTick == null) return ""

    val ticker = lastTick.ticker
    val priceNiceDiffString = getNiceDiffString(
        prevTick.price,
        lastTick.price,
        convertToPercent = true
    )
    val forecastNiceDiffString = getNiceDiffString(
        prevTick.consensus,
        lastTick.consensus,
        4,
        convertToPercent = true
    )
    val potentialNiceDiffString = getNiceDiffString(
        prevTick.getPotential(),
        lastTick.getPotential(),
        2)

    return "#${ticker.ticker} ${ticker.name}\n" +
        "\nPrice, ${ticker.currency.getSign()}: ${priceNiceDiffString}\n" +
        "\nForecast, ${ticker.currency.getSign()}: ${forecastNiceDiffString}\n" +
        "\nPotential, %: $potentialNiceDiffString"
}

private fun getNiceDiffString(
    from: Double,
    to: Double,
    precision: Int = -1,
    convertToPercent: Boolean = false
): String {
    val fromRounded = from.roundIfNeeded(precision)
    val toRounded = to.roundIfNeeded(precision)
    val diff = getEmojiedDiffOrEmpty(calcDiff(from, to, convertToPercent))

    return "$fromRounded -> $toRounded$diff"
}

private fun calcDiff(from: Double, to: Double, convertToPercent: Boolean = false): Double {
    return (to - from).let {
        it.takeUnless { convertToPercent }
            ?: it / from * 100
    }
}

private fun getEmojiedDiffOrEmpty(diff: Double) =
    diff.takeIf { it != 0.0 }
        ?.round(2)
        ?.let { rounded ->
            rounded.takeIf { it > 0 }
                ?.let { "\n${Emoji.WHITE_UP} $it%" }
                ?: "\n${Emoji.RED_DOWN} ${rounded.absoluteValue}%"
        }
        ?: ""