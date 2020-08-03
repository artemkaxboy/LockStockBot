package com.artemkaxboy.telerest.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.MappedSuperclass

// @doc https://stackoverflow.com/q/221611/1452052
// different ways to set create/update time for entity

@MappedSuperclass
abstract class ChangeableEntity : AbstractEntity() {

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val created: LocalDateTime? = null

    @UpdateTimestamp
    @Column(nullable = false)
    val updated: LocalDateTime? = null
}
