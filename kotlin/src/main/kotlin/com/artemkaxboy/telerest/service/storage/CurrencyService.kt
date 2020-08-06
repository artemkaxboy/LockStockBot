package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.entity.Currency
import com.artemkaxboy.telerest.repo.CurrencyRepo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CurrencyService(
    private val currencyRepo: CurrencyRepo
) {

    /**
     * Finds entity by id.
     *
     * @see CrudRepository.findByIdOrNull
     */
    fun findById(id: String) = currencyRepo.findByIdOrNull(id)

    /**
     * Saves entity to repos transparently.
     *
     * @see CrudRepository.save
     */
    fun save(currency: Currency) = currencyRepo.save(currency)

    /**
     * Saves currency if it does not exist.
     *
     * @param currency entity to save
     * @return saved entity if it did not exist, originally given entity otherwise
     */
    fun saveIfNotExist(currency: Currency) =
        currency.takeIf { currencyRepo.existsById(currency.id) }
            ?: currencyRepo.save(currency)

    /**
     * Saves entities to repos transparently.
     *
     * @see CrudRepository.saveAll
     */
    fun saveAll(list: List<Currency>): List<Currency> = currencyRepo.saveAll(list)

    /**
     * Deletes all entities in batch.
     *
     * @see JpaRepository.deleteAllInBatch
     */
    fun deleteAll() = currencyRepo.deleteAllInBatch()
}
