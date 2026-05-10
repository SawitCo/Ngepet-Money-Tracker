package com.example.ngepet.domain.model

data class Category(
    val id: String,
    val name: String,
    val iconName: String,
    val colorHex: String = "#3B6D11"
)
