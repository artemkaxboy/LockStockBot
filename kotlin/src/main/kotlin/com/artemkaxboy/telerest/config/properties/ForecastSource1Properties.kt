package com.artemkaxboy.telerest.config.properties

import java.time.Duration
import java.time.temporal.ChronoUnit
import org.hibernate.validator.constraints.time.DurationMin
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.convert.DurationUnit
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

private const val DEFAULT_TTL_PERIOD_DAYS = 30
private const val DEFAULT_QUORUM = 3
private const val DEFAULT_THRESHOLD_PERCENT = 5
private const val DEFAULT_PAGE_SIZE = 15
private const val DEFAULT_MAX_PAGES = 20
private const val DEFAULT_BUFFER_PAGES = 3
private const val DEFAULT_UPDATE_INTERVAL_MINUTES = 15

private const val DEFAULT_RESOLVER_START_DELAY = 15_000L

@Configuration
@ConfigurationProperties("forecast.source1")
@Validated
class ForecastSource1Properties {

    /**
     * Interval of renewing forecasts information. Cannot be less than one minute.
     * Default value is 15 minute.
     * */
    @Suppress("unused") // used in UpdateForecastsJob.update @Scheduled.fixedRateString
    @DurationUnit(ChronoUnit.MINUTES)
    @DurationMin(minutes = 1, message = "Update interval cannot be less than one minute.")
    var updateInterval: Duration = Duration.ofMinutes(DEFAULT_UPDATE_INTERVAL_MINUTES.toLong())

    /** Max page number. Fuse property in order not to fall in endless loop. */
    var maxPages: Int = DEFAULT_MAX_PAGES

    /** Ticker page size. Set page size to get from source 1 API. */
    var pageSize: Int = DEFAULT_PAGE_SIZE

    /** Size of buffer to load tickers in pages. */
    var bufferPages: Int = DEFAULT_BUFFER_PAGES

    /**
     * Threshold of forecast price difference comparing with the nearest one.
     * The extreme values are dropped from analysis when they difference is greater than threshold.
     * */
    var extremeThreshold: Int = DEFAULT_THRESHOLD_PERCENT

    /** Minimal count of active forecasts to make consensus forecast. */
    var quorum: Int = DEFAULT_QUORUM

    /** Time to live for forecasts. */
    @DurationUnit(ChronoUnit.DAYS)
    var ttl: Duration = Duration.ofDays(DEFAULT_TTL_PERIOD_DAYS.toLong())

    /** Base url to perform requests. */
    var baseUrl: String = ""

    var analystResolver: AnalystResolver = AnalystResolver()

    fun isEnabled() = baseUrl.isNotBlank() &&
        maxPages > 0 &&
        pageSize > 0 &&
        !updateInterval.isZero

    class AnalystResolver {

        /** Enable resolver. */
        var enabled: Boolean = false

        /** Resolver delay before start work. Needed to allow main update transaction to finish. */
        var startDelay: Long = DEFAULT_RESOLVER_START_DELAY

        var containerSelector: String = ""
        var elementSelector: String = ""
        var analystMark: String = ""
        var analystValueSelector: String = ""
        var analysisValueSelector: String = ""

        fun isEnabled() = enabled &&
            containerSelector.isNotBlank() &&
            elementSelector.isNotBlank() &&
            analystMark.isNotBlank() &&
            analystValueSelector.isNotBlank() &&
            analysisValueSelector.isNotBlank()
    }
}
