package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.model.User
import com.artemkaxboy.telerest.repo.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

    fun findAll(pageRequest: Pageable): Page<User> {
        return userRepository.findAll(pageRequest.fixSorting())
    }

    fun findById(id: Long) = userRepository.findByIdOrNull(id)

    fun save(user: User) = userRepository.save(user)
}

private fun Pageable.fixSorting(): Pageable =
    this.takeUnless { it.sort == Sort.unsorted() }
        ?: PageRequest.of(this.pageNumber, this.pageSize, Sort.by(User::name.name))
