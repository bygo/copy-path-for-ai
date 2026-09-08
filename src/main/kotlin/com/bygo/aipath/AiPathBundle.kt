package com.bygo.aipath

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.AiPathBundle"

internal object AiPathBundle {
    private val INSTANCE = DynamicBundle(AiPathBundle::class.java, BUNDLE)

    fun message(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any,
    ): String = INSTANCE.getMessage(key, *params)
}
