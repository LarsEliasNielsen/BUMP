package cloud.larn.bump.domain.model

@JvmInline
value class Email(val value: String) {
    init {
        require(EMAIL_REGEX.matches(value)) { "Invalid email address: $value" }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
