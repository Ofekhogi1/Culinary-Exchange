package com.college.culinaryexchange.util

object Validators {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = email.isNotBlank() && EMAIL_REGEX.matches(email)

    fun isValidPassword(password: String): Boolean = password.length >= 6

    fun isValidName(name: String): Boolean = name.trim().length >= 2

    /**
     * Strong password: ≥8 chars, at least one uppercase letter and one digit.
     * Use this for new registrations; [isValidPassword] is kept for backwards compat.
     */
    fun isStrongPassword(password: String): Boolean =
        password.length >= 8 &&
        password.any { it.isUpperCase() } &&
        password.any { it.isDigit() }
}
