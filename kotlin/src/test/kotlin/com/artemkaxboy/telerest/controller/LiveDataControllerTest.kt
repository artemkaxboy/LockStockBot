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

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class LiveDataControllerTest {

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

    // @important https://www.callicoder.com/spring-5-reactive-webclient-webtestclient-examples/
    @Test
    fun `fail to return get existing ticker`() {

        val tickerCode = "TICK"

        val liveDataDto = LiveData.DUMMY
            .let { it.copy(ticker = it.ticker.copy(ticker = tickerCode)) }
            .also { liveDataService.save(it) }
            .let { liveDataToLiveDataDtoMapper.toDto(it) }

        webTestClient.get()
            .uri("$BASE_URL/liveData/{ticker}", tickerCode)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson200()
            .expectBody()
            .jsonPath(JSON_FIRST_ITEM_PATH)
            .value<LinkedHashMap<String, String>> {

                Assertions.assertThat(modelMapper.map(it, LiveDataDto::class.java))
                    .isEqualTo(liveDataDto)
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
    fun `fail to return error on empty ticker`() {

        val tickerCode = " "
        val expectedStatus = HttpStatus.NOT_FOUND // todo add error type mapping

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

    @AfterEach
    fun clearDb() {
        liveDataRepo.deleteAll()
    }
}
