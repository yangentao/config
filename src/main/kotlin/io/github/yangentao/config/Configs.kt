@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package  io.github.yangentao.config

import java.io.File
import java.util.stream.IntStream

//operator fun ConfigMap.getValue(thisRef: ConfigMap, property: KProperty<*>): String? {
//    return thisRef.getPath(property.userName)?.asString
//}
//
//operator fun ConfigMap.setValue(thisRef: ConfigMap, property: KProperty<*>, value: String?) {
//    if (value == null) {
//        thisRef.setPath(property.userName, YConfigNull)
//    } else {
//        thisRef.setPath(property.userName, YConfigString(value))
//    }
//}
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
//    private val KClass<*>.configableProperties: List<KMutableProperty<*>>
//        get() = this.declaredMemberPropertiesSorted.filter {
//            it is KMutableProperty<*> && it.isPublic && !it.isLateinit && !it.excluded
//        }.cast()

    //只支持数字和字符串
//    fun <T : Any> toModel(map: YConfigMap, cls: KClass<T>): T {
//        val inst: T = cls.createInstance()
//        val ps: List<KMutableProperty<*>> = cls.configableProperties
//        for (p in ps) {
//            val yv: YConfigValue? = map[p.userName]
//            when (yv) {
//                null, is YConfigNull -> {}
//
//                is YConfigString -> {
//                    val v = yv.toPropertyValue(p)
//                    if (v != null) {
//                        p.setPropValue(inst, v)
//                    }
//                }
//
//                else -> error("Unsupport property: $p,  value: $yv")
//            }
//
//        }
//        return inst
//    }

    //只支持数字和字符串
//    fun <T : Any> fromModel(model: T): YConfigMap {
//        val ps: List<KMutableProperty<*>> = model::class.configableProperties
//        val map = YConfigMap()
//        for (p in ps) {
//            map.putAny(p.userName, p.getPropValue(model) ?: "")
//        }
//        return map
//    }

    fun serialize(map: ConfigMap, pretty: Boolean = true): String {
        return map.serialize(pretty)
    }

    fun tryParse(text: String, allowKeyPath: Boolean = true): ConfigValue? {
        try {
            val v = ConfigParser(text, allowKeyPath).parse()
            if (v.isNull) return null
            return v
        } catch (ex: Exception) {
            return null
        }
    }

    fun parse(text: String, allowKeyPath: Boolean = true): ConfigValue {
        return ConfigParser(text, allowKeyPath).parse()
    }

    fun parseFile(file: File, allowKeyPath: Boolean = true): ConfigValue {
        try {
            val text = file.readText()
            return ConfigParser(text, allowKeyPath).parse()
        } catch (ex: Exception) {
            println("Parse file error: ${file.canonicalPath}")
            throw ex
        }
    }

    fun escape(value: String): String {
        return escapeYConfigValue(value)
    }
}

/**
 * Parse error.
 */
class ConfigError(msg: String) : Exception(msg)

/**
 * Abstract config value
 */
abstract class ConfigValue {
    open val isNull: Boolean get() = false
    val asList: ConfigList? get() = this as? ConfigList
    val asMap: ConfigMap? get() = this as? ConfigMap
    val asString: String? get() = (this as? ConfigString)?.data
    val asInt: Int? get() = asString?.trim()?.toIntOrNull()
    val asDouble: Double? get() = asString?.trim()?.toDoubleOrNull()
    val asBoolean: Boolean? get() = asString?.trim()?.toBooleanValue()

//    open fun toPropertyValue(p: KProperty<*>): Any? {
//        return null
//    }

    override fun toString(): String {
        return this.serialize()
    }

    fun serialize(pretty: Boolean = false): String {
        val buf = StringBuilder(512)
        if (pretty) {
            serializeTo(buf, 0)
        } else {
            serializeTo(buf)
        }
        return buf.toString()
    }

    abstract fun serializeTo(buf: StringBuilder)
    abstract fun serializeTo(buf: StringBuilder, ident: Int)

    // int, string, double ...
//    inline fun <reified T> readTo(p: KMutableProperty0<T>, miss: T? = null) {
//        val v = getPath(p)
//        val setter = p.setter
//        setter.isAccessible = true
//        if (v != null) {
//            setter.call(p.decodeValue(v.asString))
//        } else if (miss != null) {
//            setter.call(miss)
//        } else if (p.returnType.isMarkedNullable) {
//            setter.call(null)
//        } else {
//            error("${p.name} is null")
//        }
//    }

//    fun getPath(p: KProperty<*>): YConfigValue? {
//        return getPath(p.userName)
//    }

    fun getPath(paths: String): ConfigValue? {
        return getPath(paths.split(DOT).map { it.trim() })
    }

    fun getPath(paths: List<String>): ConfigValue? {
        if (paths.isEmpty()) return null
        val v = this.getByKey(paths.first(), false) ?: return null
        if (paths.size == 1) return v
        return v.getPath(paths.sublist(1))
    }

//    fun setPath(key: KProperty<*>, value: Any?) {
//        this.setPath(key.userName, value)
//    }

    fun setPath(paths: String, value: Any?): Boolean {
        return setPath(paths.split(DOT).map { it.trim() }, value)
    }

    fun setPath(paths: List<String>, value: Any?): Boolean {
        if (paths.isEmpty()) return false
        if (paths.size == 1) {
            val v: ConfigValue = when (value) {
                null -> ConfigNull
                is ConfigValue -> value
                is Boolean -> ConfigString(value.toString())
                is Number -> ConfigString(value.toString())
                else -> ConfigString(value.toString())
            }
            putByKey(paths.first(), v)
            return true
        }
        val oldValue = this.getByKey(paths.first(), true) ?: return false
        return oldValue.setPath(paths.sublist(1), value)
    }

    fun removePath(paths: String): ConfigValue? {
        return removePath(paths.split(DOT).map { it.trim() })
    }

    fun removePath(paths: List<String>): ConfigValue? {
        if (paths.isEmpty()) return null
        if (paths.size == 1) {
            return this.removeByKey(paths.first())
        }

        return getByKey(paths.first(), false)?.removePath(paths.sublist(1))
    }

    protected abstract fun putByKey(key: String, value: ConfigValue): Boolean
    protected abstract fun getByKey(key: String, autoCreate: Boolean): ConfigValue?
    protected abstract fun removeByKey(key: String): ConfigValue?

}

/**
 * Config null value,  empty string is null
 * ```
 * values:[1,2,3,]
 * ```
 * values => [1,2,3]
 * but.
 * ```
 * values:[1,2,3,,]
 * ```
 * values => [1,2,3,null]
 */
object ConfigNull : ConfigValue() {
    override val isNull: Boolean get() = true
    override fun toString(): String {
        return "null"
    }

//    override fun toPropertyValue(p: KProperty<*>): Any? {
//        val s = p.findAnnotation<NullValue>()?.value
//        if (s != null) {
//            return p.decodeValue(s)
//        }
//        return null
//    }

    override fun serializeTo(buf: StringBuilder) {
        // do nothing
    }

    override fun serializeTo(buf: StringBuilder, ident: Int) {

    }

    override fun getByKey(key: String, autoCreate: Boolean): ConfigValue? {
        return null
    }

    override fun putByKey(key: String, value: ConfigValue): Boolean {
        return false
    }

    override fun removeByKey(key: String): ConfigValue? {
        return null
    }
}

/**
 * empty string is trait to null.
 * user '\' to escape frirst/last blank
 * ```
 * info: hello my friend.
 * info: hello\
 * info: \ hello
 * ```
 */
class ConfigString(val data: String) : ConfigValue(), Comparable<String> by data, CharSequence by data {
    override fun serializeTo(buf: StringBuilder) {
        buf.append(escapeYConfigValue(data))
    }

    override fun serializeTo(buf: StringBuilder, ident: Int) {
        buf.append(escapeYConfigValue(data))
    }

    override fun toString(): String {
        return data
    }

//    override fun toPropertyValue(p: KProperty<*>): Any? {
//        return p.decodeValue(data)
//    }

    override fun getByKey(key: String, autoCreate: Boolean): ConfigValue? {
        return null
    }

    override fun putByKey(key: String, value: ConfigValue): Boolean {
        return false
    }

    override fun removeByKey(key: String): ConfigValue? {
        return null
    }

    override fun chars(): IntStream {
        return data.chars()
    }

    override fun codePoints(): IntStream {
        return data.codePoints()
    }
}

/**
 * List values.
 * ```
 * [
 *   [1,2,3]
 *   [1,2,3,]          // same as [1,2,3]
 *   [1,2,3,,]         // [1,2,3,null]
 * ]
 * ```
 */
@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
class ConfigList(val data: ArrayList<ConfigValue> = ArrayList()) : ConfigValue(), MutableList<ConfigValue> by data {

    fun removeNulls(): ConfigList {
        data.removeIf { it.isNull }
        return this
    }

    override fun serializeTo(buf: StringBuilder) {
        buf.append('[')
        for (i in data.indices) {
            if (i != 0) {
                buf.append(',')
            }
            data[i].serializeTo(buf)
        }
        buf.append(']')
    }

    override fun serializeTo(buf: StringBuilder, ident: Int) {
        val need = data.isNotEmpty() && (data.first() is ConfigList || data.first() is ConfigMap)
        if (!need) {
            serializeTo(buf)
        } else {
            buf.append('[')
            buf.append(LF)
            for (i in data.indices) {
                buf.space(ident)
                data[i].serializeTo(buf, ident)
                buf.append(LF)
            }
            buf.space(ident)
            buf.append(']')
        }
    }

    override fun getByKey(key: String, autoCreate: Boolean): ConfigValue? {
        val idx = key.toIntOrNull() ?: return null
        val v = data.getOrNull(idx)
        if (v != null) return v
        if (autoCreate) {
            val map = ConfigMap()
            putByKey(key, map)
            return map
        }
        return null
    }

    override fun putByKey(key: String, value: ConfigValue): Boolean {
        val idx = key.toIntOrNull() ?: return false
        while (data.size <= idx) {
            data.add(ConfigNull)
        }
        data[idx] = value
        return true
    }

    override fun removeByKey(key: String): ConfigValue? {
        val n = key.toIntOrNull() ?: return null
        if (n in data.indices) return data.removeAt(n)
        return null
    }
}

/**
 * Config map.
 * ```
 * {
 *    a:{name:tom, age:20}
 *    b:{name:tom; age:20}
 *    c:{
 *      name:tom
 *      age:20
 *      }
 * }
 * ```
 */
class ConfigMap(val data: LinkedHashMap<String, ConfigValue> = LinkedHashMap()) : ConfigValue(), MutableMap<String, ConfigValue> by data {
    fun getInt(key: String): Int? = get(key)?.asInt
    fun getDouble(key: String): Double? = get(key)?.asDouble
    fun getString(key: String): String? = get(key)?.asString
    fun getList(key: String): ConfigList? = get(key)?.asList
    fun getMap(key: String): ConfigMap? = get(key)?.asMap

    fun removeNulls(): ConfigMap {
        val ks = HashSet<String>()
        for ((k, v) in data) {
            if (v.isNull) ks.add(k)
        }
        for (k in ks) data.remove(k)
        return this
    }

    override fun serializeTo(buf: StringBuilder) {
        buf.append('{')
        var first = true
        for ((k, v) in data) {
            if (first) {
                first = false
            } else {
                buf.append(',')
            }
            buf.append(k)
            buf.append(':')
            v.serializeTo(buf)
        }
        buf.append('}')
    }

    override fun serializeTo(buf: StringBuilder, ident: Int) {
        buf.append('{')
        if (data.isNotEmpty()) buf.append(LF)
        for (e in data.entries) {
            buf.space(ident + 1)
            buf.append(e.key).append(':')
            e.value.serializeTo(buf, ident + 1)
            buf.append(LF)
        }
        if (data.isNotEmpty()) buf.space(ident)
        buf.append('}')
    }

    fun putAny(key: String, value: Any) {
        when (value) {
            is Number -> set(key, value)
            is String -> set(key, value)
            is ConfigValue -> put(key, value)
            else -> set(key, value.toString())
        }
    }

    operator fun set(key: String, value: Number) {
        this.put(key, ConfigString(value.toString()))
    }

    operator fun set(key: String, value: String) {
        this.put(key, ConfigString(value))
    }

    override fun getByKey(key: String, autoCreate: Boolean): ConfigValue? {
        val v = this[key]
        if (v != null) return v
        if (autoCreate) {
            val m = ConfigMap()
            this[key] = m
            return m
        }
        return null
    }

    override fun putByKey(key: String, value: ConfigValue): Boolean {
        this[key] = value
        return true
    }

    override fun removeByKey(key: String): ConfigValue? {
        return data.remove(key)
    }

}

private fun StringBuilder.space(n: Int): StringBuilder {
    for (i in 1..(n * 4)) {
        append(' ')
    }
    return this
}

private class ConfigParser(private val text: String, private val allowKeyPath: Boolean = true) {
    private val data: CharArray = text.toCharArray()
    private var current: Int = 0

    private val end: Boolean get() = current >= data.size
    private val currentChar: Char get() = data[current]

    fun parse(): ConfigValue {
        skipSpTabCrLf()
        if (end) return ConfigNull
        if (currentChar == '[') return parseArray()
        return parseObject(currentChar != '{')
    }

    private val leftString: String
        get() {
            if (current >= data.size) {
                return ""
            }
            val sb = StringBuilder()
            var n = 0
            while (n < 20) {
                if (current + n >= data.size) {
                    break
                }
                sb.append(data[current + n])
                ++n
            }
            return sb.toString()
        }

    private fun err(msg: String = "YCParse error"): Nothing {
        if (!end) {
            throw ConfigError("$msg: position: $current, char: $currentChar, left: $leftString")
        } else {
            throw ConfigError(msg)
        }
    }

    private fun parseValue(): ConfigValue {
        skipSpTab()
        if (end) {
            return ConfigNull
        }
        val ch = currentChar
        when (ch) {
            '{' -> return parseObject()
            '[' -> return parseArray()
            else -> {
                val s = parseString(isKey = false)
                if (s.isEmpty()) return ConfigNull
                return ConfigString(s)
            }
        }
    }

    private fun parseArray(): ConfigList {
        skipSpTab()
        tokenc('[')
        val ya = ConfigList()
        while (!end) {
            skipSpTabCrLf()
            if (currentChar == ']') {
                break
            }
            val yv = parseValue()
            ya.data.add(yv)
            if (currentChar in SEPS) {
                next()
                continue
            }
        }
        skipSpTabCrLf()
        tokenc(']')
        return ya
    }

    private fun parseObject(isRoot: Boolean = false): ConfigMap {
        skipSpTab()
        if (!isRoot) {
            tokenc('{')
            skipSpTabCrLf()
        }
        val yo = ConfigMap()
        while (!end) {
            skipSpTab()
            if (end) break
            if (currentChar == '}') {
                skipSpTabCrLf()
                break
            }
            if (currentChar in SEPS) {
                next()
                continue
            }
            skipSpTabCrLf()
            val key: String = parseString(isKey = true)
            if (key.isEmpty()) err("key is empty.")
            tokenc(COLON, EQUAL)
            val yv = parseValue()
            if (allowKeyPath) {
                yo.setPath(key, yv)
            } else {
                yo[key] = yv
            }
        }
        if (!isRoot) {
            tokenc('}')
        }
        skipSpTabCrLf()
        return yo
    }

    private fun parseString(isKey: Boolean): String {
        skipSpTab()
        val buf = StringBuilder(64)
        var escing = false
        while (!end) {
            val ch = currentChar
            if (!escing) {
                if (isKey) {
                    if (ch in STR_END_KEY) break
                } else {
                    if (ch in STR_END_VALUE) break
                }
                if (ch.isCRLF) break //空字符串
                next()
                if (ch == BACKSLASH) {//开始转义
                    escing = true
                    continue
                }
                buf.append(ch)
            } else {
                escing = false
                next()
                when (ch) {
                    '/' -> buf.append(ch)
                    'b' -> buf.append('\b')
                    'f' -> buf.append(12.toChar())
                    'n' -> buf.append(LF)
                    'r' -> buf.append(CR)
                    't' -> buf.append(TAB)
                    'u', 'U' -> {
                        if (current + 4 < text.length && text[current + 0].isHex && text[current + 1].isHex && text[current + 2].isHex && text[current + 3].isHex) {
                            val sb = StringBuilder(4)
                            sb.append(text[current + 0])
                            sb.append(text[current + 1])
                            sb.append(text[current + 2])
                            sb.append(text[current + 3])
                            current += 4
                            val n = sb.toString().toInt(16)
                            buf.append(n.toChar())
                            //UTF16 解码 https://cloud.tencent.com/developer/article/1625557
                        } else {
                            err("期望是unicode字符")
                        }
                    }

                    else -> {
                        buf.append(ch)
                    }

                }
            }
        }
        if (escing) {
            err("解析错误,转义,")
        }
        //trim end
        var idx: Int = 0
        while (idx < buf.length) {
            val bufChar = data[current - 1 - idx]
            if (bufChar != ' ') break
            val preIdx = current - 1 - idx - 1
            if (preIdx > 0 && data[preIdx] == BACKSLASH) break

            idx += 1
        }
        buf.setLength(buf.length - idx)
        return buf.toString()
    }

    private fun next() {
        current += 1
    }

    private fun skipSpTab() {
        while (!end) {
            if (currentChar.isSpTab) {
                next()
            } else {
                return
            }
        }
    }

    private fun skipSpTabCrLf() {
        while (!end) {
            if (currentChar.isWhite) {
                next()
            } else {
                return
            }
        }
    }

    private fun tokenc(vararg cs: Char) {
        skipSpTab()
        if (end) {
            err("Expect ${cs.map { "'$it'" }}, but text is end.")
        }
        if (currentChar !in cs) {
            err("期望是字符${cs.toList()}")
        }
        next()
        skipSpTab()
    }
}

private fun String.toBooleanValue(): Boolean? {
    if (this == "true" || this == "yes" || this == "1") return true
    if (this == "false" || this == "no" || this == "0") return false
    return null
}

private fun <T> List<T>.sublist(i: Int): List<T> {
    return this.subList(i, this.size)
}

private fun escapeYConfigValue(s: String): String {
    var n = 0
    for (c in s) {
        if (c in ESCAPE_CHARS) {
            n += 1
        }
    }

    if (n == 0) {
        return s
    }
    val sb = StringBuilder(s.length + n)
    for (i in s.indices) {
        val c = s[i]
        if (c in ESCAPE_CHARS) {
            sb.append(BACKSLASH)
        }
        sb.append(c)
    }
    return sb.toString()
}

private val Char.isHex: Boolean get() = (this in '0'..'9') || (this in 'a'..'f') || (this in 'A'..'F')

private const val EQUAL: Char = '='
private const val COLON: Char = ':'
private const val SEM: Char = ';'
private const val COMMA: Char = ','
private const val CR: Char = '\r'
private const val LF: Char = '\n'
private const val SP: Char = ' '
private const val TAB: Char = '\t'
private const val DOT: Char = '.'
private const val BACKSLASH: Char = '\\'

private val WHITES: Set<Char> = hashSetOf(CR, LF, SP, TAB)

private val Char.isWhite: Boolean get() = this in WHITES
private val Char.isSpTab: Boolean get() = this == SP || this == TAB
private val Char.isCRLF: Boolean get() = this == CR || this == LF

private val BRACKETS: Set<Char> = hashSetOf('[', ']', '{', '}')
private val ASSIGNS: Set<Char> = hashSetOf(COLON, EQUAL)
private val SEPS: Set<Char> = hashSetOf(CR, LF, SEM, COMMA)
private val STR_END_KEY: Set<Char> = SEPS + BRACKETS + ASSIGNS
private val STR_END_VALUE: Set<Char> = SEPS + BRACKETS
private val ESCAPE_CHARS: Set<Char> = SEPS + BRACKETS + ASSIGNS + BACKSLASH