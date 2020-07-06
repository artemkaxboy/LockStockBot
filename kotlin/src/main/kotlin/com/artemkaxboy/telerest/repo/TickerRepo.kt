package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.entity.Ticker
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TickerRepo : JpaRepository<Ticker, String>
