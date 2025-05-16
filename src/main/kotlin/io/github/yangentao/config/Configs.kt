@file:Suppress("MemberVisibilityCanBePrivate")

package  io.github.yangentao.config

import io.github.yangentao.charcode.CharCode
import java.io.File

/**
 * Parse map or list. single value is string or null
 * ```
 * {
 *    host: google.com
 *    port: 80
 *    fav: [apple, orange, melo]
 * }
 * ```
 */
object Configs {
    fun serialize(value: Any, pretty: Boolean = false): String {
        val buf = StringBuilder(512)
        if (pretty) {
            serializePrety(buf, value, 0)
        } else {
            serializeTo(buf, value)
        }
        return buf.toString()
    }

    fun serializeTo(buf: StringBuilder, value: Any) {
        when (value) {
            is ConfigMap -> {
                buf.append('{')
                var first = true
                for ((k, v) in value.data) {
                    if (first) {
                        first = false
                    } else {
                        buf.append(',')
                    }
                    buf.append(k)
                    buf.append(':')
                    serializeTo(buf, v)
                }
                buf.append('}')
            }

            is ConfigList -> {
                buf.append('[')
                for (i in value.data.indices) {
                    if (i != 0) {
                        buf.append(',')
                    }
                    serializeTo(buf, value.data[i])
                }
                buf.append(']')
            }

            else -> buf.append(encodeString(value.toString()))
        }
    }

    fun serializePrety(buf: Appendable, value: Any, ident: Int) {
        when (value) {
            is ConfigMap -> {
                buf.append('{')
                if (value.data.isNotEmpty()) buf.append(CharCode.LF)
                for (e in value.data.entries) {
                    buf.space(ident + 1)
                    buf.append(e.key).append(':')
                    serializePrety(buf, e.value, ident + 1)
                    buf.append(CharCode.LF)
                }
                if (value.data.isNotEmpty()) buf.space(ident)
                buf.append('}')
            }

            is ConfigList -> {
                val need = value.data.isNotEmpty() && (value.data.first() is ConfigList || value.data.first() is ConfigMap)
                if (!need) {
                    serializePrety(buf, value, ident + 1)
                } else {
                    buf.append('[')
                    buf.append(CharCode.LF)
                    for (i in value.data.indices) {
                        buf.space(ident)
                        serializePrety(buf, value.data[i], ident + 1)
                        buf.append(CharCode.LF)
                    }
                    buf.space(ident)
                    buf.append(']')
                }
            }

            else -> buf.append(encodeString(value.toString()))
        }
    }

    fun tryParse(text: String, allowKeyPath: Boolean = true): Any? {
        try {
            return ConfigParser(text).parse()
        } catch (ex: Exception) {
            return null
        }
    }

    fun parse(text: String, allowKeyPath: Boolean = true): Any? {
        return ConfigParser(text).parse()
    }

    fun parseFile(file: File, allowKeyPath: Boolean = true): Any? {
        try {
            val text = file.readText()
            return ConfigParser(text).parse()
        } catch (ex: Exception) {
            println("Parse file error: ${file.canonicalPath}")
            throw ex
        }
    }

}

private fun Appendable.space(n: Int): Appendable {
    for (i in 1..(n * 4)) {
        append(' ')
    }
    return this
}

internal fun String.toBooleanValue(): Boolean {
    if (this == "true" || this == "yes" || this == "1") return true
    if (this == "false" || this == "no" || this == "0") return false
    error("NOT a bool value: $this")
}



