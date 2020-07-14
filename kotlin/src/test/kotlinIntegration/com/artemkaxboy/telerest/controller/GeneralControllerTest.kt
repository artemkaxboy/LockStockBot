package com.artemkaxboy.telerest.controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
internal class GeneralControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `fail to return version`() {
        webTestClient.get()
            .uri("$BASE_URL/version")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectJson200()
            .expectBody()
            .jsonPath(JSON_FIRST_ITEM_PATH).isNotEmpty
    }
}
