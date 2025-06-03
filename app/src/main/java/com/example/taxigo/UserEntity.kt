package com.example.taxigo.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String?,
    val email: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean,
    val isGoogle: Boolean
)
