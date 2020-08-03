package com.artemkaxboy.telerest.business

import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.tool.Emoji
import com.artemkaxboy.telerest.tool.NumberUtils.getDiffOrNull
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
    val priceNiceDiffString = getNiceDiffStringOrEmpty(
        prevTick.price,
        lastTick.price,
        convertToPercent = true
    )
    val forecastNiceDiffString = getNiceDiffStringOrEmpty(
        prevTick.consensus,
        lastTick.consensus,
        4,
        convertToPercent = true
    )
    val potentialNiceDiffString = getNiceDiffStringOrEmpty(
        prevTick.getPotential(),
        lastTick.getPotential(),
        2)

    return "#${ticker.id} ${ticker.name}\n" +
        "\nPrice, ${ticker.currency.sign}: ${priceNiceDiffString}\n" +
        "\nForecast, ${ticker.currency.sign}: ${forecastNiceDiffString}\n" +
        "\nPotential, %: $potentialNiceDiffString"
}

private fun getNiceDiffStringOrEmpty(
    from: Double?,
    to: Double?,
    precision: Int = -1,
    convertToPercent: Boolean = false
): String {
    val fromRounded = from?.roundIfNeeded(precision) ?: return ""
    val toRounded = to?.roundIfNeeded(precision) ?: return ""

    val diff = getEmojiedDiffOrEmpty(getDiffOrNull(from, to, convertToPercent))

    return "$fromRounded -> $toRounded$diff"
}

private fun getEmojiedDiffOrEmpty(diff: Double?) =
    diff?.takeIf { it != 0.0 }
        ?.round(2)
        ?.let { rounded ->
            rounded.takeIf { it > 0 }
                ?.let { "\n${Emoji.WHITE_UP} $it%" }
                ?: "\n${Emoji.RED_DOWN} ${rounded.absoluteValue}%"
        }
        ?: ""
