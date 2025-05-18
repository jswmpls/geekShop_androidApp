package com.example.geekshop.data.model

data class Products(
    val id: Int,
    val Name: String,
    val Cost: String,
    val inCart: Boolean,
    val imageId: Int,
    val Discription: String,  // Обратите внимание на написание (у вас было Discription)
    val Category: String
)