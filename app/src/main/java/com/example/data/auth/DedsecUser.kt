package com.example.data.auth

data class DedsecUser(
    val email: String,
    val displayName: String = "",
    val isAdmin: Boolean = false,
    val isBlocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)
