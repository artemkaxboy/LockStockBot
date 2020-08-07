package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.repo.LiveDataRepo
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
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["application.time-zone=UTC"]
)
@AutoConfigureWebTestClient
internal class LiveDataControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var liveDataRepo: LiveDataRepo

    // @doc https://www.callicoder.com/spring-5-reactive-webclient-webtestclient-examples/
    @Test
    fun `fail to return existing ticker`() {

        val expected = LiveData.random()
            .also { liveDataRepo.save(it) }

        webTestClient.get()
            .uri(LIVE_DATA_URL, expected.tickerId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson200()
            .expectBody()
            .jsonPath(JSON_FIRST_ITEM_PATH)
            .value<Map<String, Any>> {

                Assertions.assertThat(it[LiveData::tickerId.name] as? String)
                    .isNotNull()
                    .isEqualTo(expected.tickerId)

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
            .uri(LIVE_DATA_URL, tickerCode)
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
            .uri(LIVE_DATA_URL, tickerCode)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson(expectedStatus)
    }

    @AfterEach
    fun clean() {
        liveDataRepo.deleteAll()
    }
}
