package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepo : JpaRepository<User, Long>
