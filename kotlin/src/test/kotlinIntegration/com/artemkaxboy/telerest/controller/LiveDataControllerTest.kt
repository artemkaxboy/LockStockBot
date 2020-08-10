package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.LiveDataId
import com.artemkaxboy.telerest.mapper.HashMapMapper
import com.artemkaxboy.telerest.repo.LiveDataRepo
import com.artemkaxboy.telerest.service.storage.LiveDataService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDate

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

    @Autowired
    private lateinit var hashMapMapper: HashMapMapper

    // @doc https://www.callicoder.com/spring-5-reactive-webclient-webtestclient-examples/
    @Test
    fun `fail to return existing ticker`() {

        val expectedCount = 1L
        val expected = LiveData.random()
            .also { liveDataRepo.save(it) }

        val response = webTestClient.get()
            .uri(LIVE_DATA_TICKER_URL, expected.tickerId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson200()
            .expectBody(ResponseDto::class.java)
            .returnResult().responseBody

        val actualCount = response?.data?.totalItems
        Assertions.assertEquals(expectedCount, actualCount)

        val actual = (response?.data?.items?.firstOrNull() as? Map<*, *>)
            ?.let { hashMapMapper.mapToObject(it, LiveData::class) }
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun `fail to return error on getting non-existent ticker`() {

        val tickerCode = "TICK"
        val expectedStatus = HttpStatus.NOT_FOUND

        val response = webTestClient.get()
            .uri(LIVE_DATA_TICKER_URL, tickerCode)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson(expectedStatus)
            .expectBody(ResponseDto::class.java)
            .returnResult().responseBody

        val actual = response?.error?.code

        Assertions.assertEquals(expectedStatus.value(), actual)
    }

    @Test
    fun `fail to return error on blank ticker`() {

        val tickerCode = " "
        val expectedStatus = HttpStatus.NOT_FOUND

        val response = webTestClient.get()
            .uri(LIVE_DATA_TICKER_URL, tickerCode)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson(expectedStatus)
            .expectBody(ResponseDto::class.java)
            .returnResult().responseBody

        val actual = response?.error?.code

        Assertions.assertEquals(expectedStatus.value(), actual)
    }

    @ParameterizedTest
    @ValueSource(strings = [" ", "bla-bla"])
    fun `fail to return error on getting liveData with wrong order key`(order: String) {

        val expectedStatus = HttpStatus.BAD_REQUEST

        val response = webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path(LIVE_DATA_URL)
                    .queryParam("order", order)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson(expectedStatus)
            .expectBody(ResponseDto::class.java)
            .returnResult().responseBody

        val actual = response?.error?.code

        Assertions.assertEquals(expectedStatus.value(), actual)
    }

    @ParameterizedTest
    @EnumSource(LiveDataService.Order::class)
    fun `fail to return ok on getting liveData with correct order key`(order: LiveDataService.Order) {

        val response = webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path(LIVE_DATA_URL)
                    .queryParam("order", order.name)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson200()
            .expectBody(ResponseDto::class.java)
            .returnResult().responseBody

        val actualError = response?.error?.code
        Assertions.assertNull(actualError)
    }

    @ParameterizedTest
    @ValueSource(strings = [" ", "bla-bla"])
    fun `fail to return error on getting liveData with wrong direction key`(direction: String) {

        val expectedStatus = HttpStatus.BAD_REQUEST

        val response = webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path(LIVE_DATA_URL)
                    .queryParam("direction", direction)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson(expectedStatus)
            .expectBody(ResponseDto::class.java)
            .returnResult().responseBody

        val actual = response?.error?.code

        Assertions.assertEquals(expectedStatus.value(), actual)
    }

    @ParameterizedTest
    @EnumSource(Sort.Direction::class)
    fun `fail to return ok on getting liveData with correct direction key`(direction: Sort.Direction) {

        val response = webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path(LIVE_DATA_URL)
                    .queryParam("direction", direction.name)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson200()
            .expectBody(ResponseDto::class.java)
            .returnResult().responseBody

        val actualError = response?.error?.code
        Assertions.assertNull(actualError)
    }

    @Test
    fun `fail to return data on getting liveData`() {

        val maxCount = 5
        val expectedCount = (1..maxCount).map { LiveData.random(date = LocalDate.now()) }
            .distinctBy { LiveDataId.of(it) }
            .also { liveDataRepo.saveAll(it) }
            .size.toLong()

        val response = webTestClient.get()
            .uri { uriBuilder ->
                uriBuilder.path(LIVE_DATA_URL)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson200()
            .expectBody(ResponseDto::class.java)
            .returnResult().responseBody

        val actualCount = response?.data?.totalItems
        Assertions.assertEquals(expectedCount, actualCount)
    }

    @AfterEach
    fun clean() {
        liveDataRepo.deleteAll()
    }
}
