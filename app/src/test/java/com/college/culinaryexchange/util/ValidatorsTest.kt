package com.college.culinaryexchange.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    // isValidEmail
    @Test fun `valid email passes`() = assertTrue(Validators.isValidEmail("user@example.com"))
    @Test fun `email without at sign fails`() = assertFalse(Validators.isValidEmail("userexample.com"))
    @Test fun `email without domain fails`() = assertFalse(Validators.isValidEmail("user@"))
    @Test fun `blank email fails`() = assertFalse(Validators.isValidEmail(""))
    @Test fun `email with spaces fails`() = assertFalse(Validators.isValidEmail("user @example.com"))
    @Test fun `subdomain email passes`() = assertTrue(Validators.isValidEmail("a@b.co"))

    // isValidPassword
    @Test fun `password with 6 chars passes`() = assertTrue(Validators.isValidPassword("abc123"))
    @Test fun `password longer than 6 passes`() = assertTrue(Validators.isValidPassword("securepassword"))
    @Test fun `password with 5 chars fails`() = assertFalse(Validators.isValidPassword("abc12"))
    @Test fun `empty password fails`() = assertFalse(Validators.isValidPassword(""))

    // isValidName
    @Test fun `name with 2 chars passes`() = assertTrue(Validators.isValidName("Jo"))
    @Test fun `name with spaces passes when trimmed length is ok`() = assertTrue(Validators.isValidName("  Jo  "))
    @Test fun `single char name fails`() = assertFalse(Validators.isValidName("J"))
    @Test fun `blank name fails`() = assertFalse(Validators.isValidName(""))
    @Test fun `whitespace only name fails`() = assertFalse(Validators.isValidName("   "))
}
