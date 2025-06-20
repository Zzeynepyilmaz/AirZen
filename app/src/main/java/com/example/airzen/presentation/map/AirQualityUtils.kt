package com.example.airzen.presentation.map

import androidx.compose.ui.graphics.Color

fun getAQIColor(aqi: Int): Color {
    return when (aqi) {
        1 -> Color(0xFF66BB6A) // Yeşil
        2 -> Color(0xFFFFEE58) // Sarı
        3 -> Color(0xFFFFA726) // Turuncu
        4 -> Color(0xFFEF5350) // Kırmızı
        5 -> Color(0xFFAB47BC) // Mor
        else -> Color.Gray
    }
}

fun getAQIDescription(aqi: Int): String {
    return when (aqi) {
        1 -> "İyi – Hava kalitesi sağlıklı 🍃"
        2 -> "Orta – Kabul edilebilir 🌤️"
        3 -> "Kötü – Hassas gruplar dikkat! ⚠️"
        4 -> "Sağlıksız – Maske önerilir 😷"
        5 -> "Tehlikeli – Dışarı çıkmayın! 🚨"
        else -> "Bilinmeyen seviye"
    }
}