package com.example.data.model

data class RecentSound(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val uriString: String,
    val dateAdded: Long = System.currentTimeMillis()
)
