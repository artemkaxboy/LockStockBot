package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.entity.User
import com.artemkaxboy.telerest.repo.UserRepo
import com.artemkaxboy.telerest.tool.Result
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UserService(private val userRepo: UserRepo) {

    fun findAll(pageRequest: Pageable = defaultPageRequest): Page<User> {
        return userRepo.findAll(pageRequest.defaultSortIfUnsorted())
    }

    fun findById(id: Long) = userRepo.findByIdOrNull(id)

    fun isCustomReadAllowed(chatId: Long?) =
        chatId?.let { userRepo.findByChatId(it) }?.customReadAllowed ?: false

    fun count() = Result.of { userRepo.count() }

    @Transactional
    fun save(user: User) = Result.of { userRepo.save(user) }

    @Transactional
    fun saveAll(users: List<User>): Result<List<User>> = Result.of { userRepo.saveAll(users) }

    @Suppress("unused") // common function will be handy
    @Transactional
    fun deleteAll() = Result.of { userRepo.deleteAllInBatch() }
}

private fun Pageable.defaultSortIfUnsorted(): Pageable =
    this.takeUnless { it.sort == Sort.unsorted() }
        ?: PageRequest.of(this.pageNumber, this.pageSize, Sort.by(User::name.name))

private val defaultPageRequest = PageRequest.of(0, 10)
