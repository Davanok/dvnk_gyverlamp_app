package com.davanok.firelamp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LampAddress(
    val hostname: String,
    val port: Int,
    val name: String = ""
) {
    fun isValid(): Boolean {
        val parts = hostname.split('.')
        return parts.size == 4 && parts.all {
            val intValue = it.toIntOrNull()
            intValue != null && intValue in 0..255
        } && port >= 0
    }

    fun getDisplayName() = name.ifBlank { hostname }

    companion object {
        val Unknown = LampAddress("0.0.0.0", 8888)
    }
}
