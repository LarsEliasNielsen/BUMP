package cloud.larn.bump.application.port

interface PasswordHasher {
    fun hash(plainText: String): String
    fun verify(plainText: String, hash: String): Boolean
}
