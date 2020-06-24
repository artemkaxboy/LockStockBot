package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.model.User
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : ReactiveCrudRepository<User, String>
