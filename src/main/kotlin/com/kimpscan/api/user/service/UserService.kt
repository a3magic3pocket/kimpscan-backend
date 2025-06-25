package com.kimpscan.api.user.service

import com.kimpscan.api.global.exception.NotFoundException
import com.kimpscan.api.global.exception.ServiceException
import com.kimpscan.api.user.entity.User
import com.kimpscan.api.user.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository
){

    @Transactional(rollbackFor = [Exception::class])
    fun addFcm(userId: Long, fcmKey: String): User {
        val user = userRepository.findByIdOrNull(userId) ?: throw ServiceException(
            message = "없는 유저입니다"
        )

        user.fcmKey = fcmKey

        return userRepository.save(user)
    }
}