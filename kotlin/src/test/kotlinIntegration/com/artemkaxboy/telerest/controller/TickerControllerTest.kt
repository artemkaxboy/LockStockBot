package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.controller.Constants.MAX_API_INT
import com.artemkaxboy.telerest.entity.Ticker
import com.artemkaxboy.telerest.service.storage.TickerService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
internal class TickerControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var tickerService: TickerService

    @Test
    fun `fail to get existing ticker`() {

        val expected = Ticker.random()
            .also { tickerService.save(it) }

        webTestClient
            // .mutate()
            // .responseTimeout(Duration.ofMinutes(5)) // @example timeout for webClient
            // .build()
            .get()
            .uri { uriBuilder ->
                uriBuilder.path("$BASE_URL/tickers")
                    .queryParam("page", 1)
                    .queryParam("pageSize", 10)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson200()
            .expectBody()
            .jsonPath(JSON_FIRST_ITEM_PATH)
            .value<Map<String, Any>> {

                Assertions.assertThat(it[Ticker::id.name] as? String)
                    .isNotNull()
                    .isEqualTo(expected.id)
            }
    }

    @Test
    fun `fail to decline incorrect page request param`() {

        val expectedStatus = HttpStatus.BAD_REQUEST
        val incorrectValues = setOf(0L, MAX_API_INT + 1)
        incorrectValues.forEach {
            webTestClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("$BASE_URL/tickers")
                        .queryParam("page", it)
                        .build()
                }
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
    }

    @Test
    fun `fail to allow correct page request param`() {

        val expectedStatus = HttpStatus.OK
        val correctValues = setOf(1L, MAX_API_INT)
        correctValues.forEach {

            webTestClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("$BASE_URL/tickers")
                        .queryParam("page", it)
                        .queryParam("pageSize", 10)
                        .build()
                }
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectJson(expectedStatus)
        }
    }

    @Test
    fun `fail to check pageSize`() {

        val expectedStatus = HttpStatus.BAD_REQUEST
        val incorrectValues = setOf(0, 101)

        incorrectValues.forEach {
            webTestClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("$BASE_URL/tickers")
                        .queryParam("page", 1)
                        .queryParam("pageSize", it)
                        .build()
                }
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
    }

    @AfterEach
    fun clearDb() {
        tickerService.deleteAll()
    }
}
