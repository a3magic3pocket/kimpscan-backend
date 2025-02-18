package com.kimpscan.api.user.repository

import com.kimpscan.api.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByOauth2SubAndIsActive(oauth2Sub: String, isActive: Boolean = true): User?
}