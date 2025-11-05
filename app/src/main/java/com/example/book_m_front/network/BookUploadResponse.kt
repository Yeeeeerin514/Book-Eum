package com.example.book_m_front.network

data class BookUploadResponse(
    val success: Boolean,
    val message: String,
    val bookId: String? = null
)