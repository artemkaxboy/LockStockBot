package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.model.User
import com.artemkaxboy.telerest.repo.UserRepo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataJpaTest
internal class UserServiceTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var userRepo: UserRepo

    @Test
    fun findAll() {
    }

    @Test
    fun testFindAll() {
    }

    @Test
    fun findById() {
    }

    @Test
    fun save() {
        val alex = User(123, "alex")
        entityManager.persist(alex)
        entityManager.flush()


        val found = userRepo.findByIdOrNull(alex.chatId)

        Assertions.assertEquals(alex, found)
    }
}