package com.college.culinaryexchange.util

object Validators {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = email.isNotBlank() && EMAIL_REGEX.matches(email)

    fun isValidPassword(password: String): Boolean = password.length >= 6

    fun isValidName(name: String): Boolean = name.trim().length >= 2
}
