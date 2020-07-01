package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.model.Ticker
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TickerRepo : JpaRepository<Ticker, Long>
