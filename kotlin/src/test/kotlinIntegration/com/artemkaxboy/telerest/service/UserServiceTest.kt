package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.entity.User
import com.artemkaxboy.telerest.repo.UserRepo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.repository.findByIdOrNull

@DataJpaTest
internal class UserServiceTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var userRepo: UserRepo

    @Test
    fun save() {
        val alex = User(chatId = 123, name = "alex")
        val insertedId = entityManager.persist(alex).id
        entityManager.flush()

        val found = userRepo.findByIdOrNull(insertedId)

        Assertions.assertEquals(alex, found)
    }
}
