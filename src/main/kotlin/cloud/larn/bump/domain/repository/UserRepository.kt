package cloud.larn.bump.domain.repository

import cloud.larn.bump.domain.model.Email
import cloud.larn.bump.domain.model.User

interface UserRepository {
    fun save(user: User): User
    fun existsByEmail(email: Email): Boolean
    fun findByEmail(email: Email): User?
}
