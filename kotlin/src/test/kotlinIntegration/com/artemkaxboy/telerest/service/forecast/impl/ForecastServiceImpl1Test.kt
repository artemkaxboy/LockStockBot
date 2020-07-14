package com.artemkaxboy.telerest.service.forecast.impl

import com.artemkaxboy.telerest.config.properties.ForecastSource1Properties
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration

@SpringBootTest
// @ActiveProfiles(profiles = ["local"])
internal class ForecastServiceImpl1Test {

    @Configuration
    class MyContextConfiguration : ForecastSource1Properties()

    // @Autowired
    // lateinit var forecastService: ForecastServiceImpl1

    @Test
    fun getList() {
        // println(forecastService.getBaseUrl())
        // val list = forecastService.getList()
        // println(list)
    }
}
