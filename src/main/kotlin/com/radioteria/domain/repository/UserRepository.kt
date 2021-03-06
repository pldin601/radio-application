package com.radioteria.domain.repository

import com.radioteria.domain.entity.User
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

interface UserRepository : Repository<User, Long> {
    fun findOne(id: Long?): User?
    fun findByEmail(@Param(value = "email") email: String): User?
    fun findAll(): List<User>
    fun save(user: User)
    fun delete(user: User)
}