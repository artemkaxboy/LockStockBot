package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.entity.Currency
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CurrencyRepo : JpaRepository<Currency, String>
