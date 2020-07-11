package com.artemkaxboy.telerest.controller

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

fun WebTestClient.ResponseSpec.expectJson200() =
    expectStatus().isOk.expectHeader().contentType(MediaType.APPLICATION_JSON)

fun WebTestClient.ResponseSpec.expectJson(status: HttpStatus) =
    expectStatus().isEqualTo(status).expectHeader().contentType(MediaType.APPLICATION_JSON)
