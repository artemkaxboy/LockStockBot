package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.entity.User
import com.artemkaxboy.telerest.repo.UserRepo
import com.artemkaxboy.telerest.tool.Result
import com.artemkaxboy.telerest.tool.sorting.Sorting
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UserService(private val userRepo: UserRepo) :
    BaseStorageService(
        listOf(Sorting(User::name))
    ) {

    /**
     * Finds entity by id.
     *
     * @see CrudRepository.findByIdOrNull
     * @return found entity or null
     */
    fun findById(id: Long): User? = userRepo.findByIdOrNull(id)

    /**
     * Finds all available entities, pageable.
     *
     * @see PagingAndSortingRepository.findAll
     */
    fun findAll(pageRequest: Pageable = defaultPageRequest): Page<User> =
        userRepo.findAll(defaultSortIfUnsorted(pageRequest))

    fun count() = Result.of { userRepo.count() }

    /**
     * Saves entity to repo.
     *
     * @see CrudRepository.save
     */
    @Transactional
    fun save(user: User) = Result.of { userRepo.save(user) }

    /**
     * Saves entities to repo.
     *
     * @see CrudRepository.saveAll
     */
    @Transactional
    fun saveAll(users: List<User>): Result<List<User>> = Result.of { userRepo.saveAll(users) }

    /**
     * Checks if it is allowed to read custom tickers from given chat.
     *
     * @return true if allowed, false - otherwise
     */
    fun isCustomReadAllowed(chatId: Long?) =
        chatId?.let { userRepo.findByChatId(it) }?.customReadAllowed ?: false
}
