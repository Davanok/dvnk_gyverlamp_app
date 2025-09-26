package com.davanok.firelamp.data.utils

import android.util.Log


inline fun <T, R> T.runLogging(name: String, block: T.() -> R): Result<R> = runCatching {
    Log.i(null, "call $name")
    block()
}.onSuccess {
    Log.d(null, "success $name: $it")
}.onFailure {
    Log.e(null, "failed $name", it)
}
