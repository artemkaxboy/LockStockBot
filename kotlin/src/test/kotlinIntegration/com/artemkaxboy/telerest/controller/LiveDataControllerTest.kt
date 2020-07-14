package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.dto.LiveDataDto
import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.mapper.LiveDataToLiveDataDtoMapper
import com.artemkaxboy.telerest.repo.LiveDataRepo
import com.artemkaxboy.telerest.service.LiveDataService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration
import java.time.LocalDate
import kotlin.random.Random

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
internal class LiveDataControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var liveDataService: LiveDataService

    @Autowired
    private lateinit var liveDataRepo: LiveDataRepo

    @Autowired
    private lateinit var liveDataToLiveDataDtoMapper: LiveDataToLiveDataDtoMapper

    @Autowired
    private lateinit var modelMapper: ModelMapper

    // @doc https://www.callicoder.com/spring-5-reactive-webclient-webtestclient-examples/
    @Test
    fun `fail to return get existing ticker`() {

        val expected = LiveData.random()
            .also { liveDataService.save(it) }

        webTestClient.get()
            .uri("$BASE_URL/liveData/{ticker}", expected.ticker.ticker)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson200()
            .expectBody()
            .jsonPath(JSON_FIRST_ITEM_PATH)
            .value<Map<String, Any>> {

                Assertions.assertThat(it[LiveData::ticker.name] as? String)
                    .isNotNull()
                    .isEqualTo(expected.ticker.ticker)

                Assertions.assertThat(it[LiveData::price.name] as? Double)
                    .isNotNull()
                    .isEqualTo(expected.price)

                Assertions.assertThat(it[LiveData::consensus.name] as? Double)
                    .isNotNull()
                    .isEqualTo(expected.consensus)

                Assertions.assertThat(it[LiveData::date.name] as? String)
                    .isNotNull()
                    .isEqualTo(expected.date.toString())
            }
    }

    @Test
    fun `fail to return error on getting non-existent ticker`() {

        val tickerCode = "TICK"
        val expectedStatus = HttpStatus.NOT_FOUND

        webTestClient.get()
            .uri("$BASE_URL/liveData/{ticker}", tickerCode)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson(expectedStatus)
            .expectBody()
            .jsonPath("$JSON_ERROR_PATH.code")
            .value<Int> {

                Assertions.assertThat(it)
                    .isEqualTo(expectedStatus.value())
            }
    }

    @Test
    fun `fail to return error on no ticker`() {

        val tickerCode = ""
        val expectedStatus = HttpStatus.NOT_FOUND

        webTestClient.get()
            .uri("$BASE_URL/liveData/{ticker}", tickerCode)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson(expectedStatus)
    }

    @Test
    fun `fail to change existing live data`() {

        val days = Random.nextInt(365)

        val liveData = LiveData.random()
            .copy(date = LocalDate.now().minusDays(days.toLong()))
            .also { liveDataService.save(it) }

        val newConsensus = Random.nextDouble()
        val newPrice = Random.nextDouble()

        val newDto = liveData
            .copy(consensus = newConsensus, price = newPrice)
            .let { liveDataToLiveDataDtoMapper.toDto(it) }

        webTestClient
            .mutate().responseTimeout(Duration.ofMinutes(5)).build()
            .post()
            .uri { uriBuilder ->
                uriBuilder.path("$BASE_URL/liveData/{ticker}")
                    .queryParam("days", days)
                    .queryParam("consensus", newConsensus)
                    .queryParam("price", newPrice)
                    .build(liveData.ticker.ticker)
            }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson200()
            .expectBody()
            .jsonPath(JSON_FIRST_ITEM_PATH)
            .value<Map<String, String>> {

                Assertions.assertThat(modelMapper.map(it, LiveDataDto::class.java))
                    .isEqualTo(newDto)
            }
    }

    @AfterEach
    fun clearDb() {
        liveDataRepo.deleteAll()
    }
}
