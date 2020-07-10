package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.dto.ResponseDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TestingWebApplicationTests {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @LocalServerPort
    private var port = 0

    private fun getUrl() = "http://localhost:$port/api/v1/version"

    @Test
    fun `fail to return version`() {

        val entity = restTemplate
            .getForEntity<ResponseDto>(getUrl())

        assertEquals(HttpStatus.OK.value(), entity.statusCode.value())
        assertNotNull(entity.body?.data?.items?.get(0))
    }
}
