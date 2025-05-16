package io.github.yangentao.config

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
class ConfigList(val data: ArrayList<Any> = ArrayList()) : List<Any> by data {

    val stringList: List<String> get() = data.mapNotNull { it as? String }
    val intList: List<Int> get() = stringList.map { it.toInt() }
    val longList: List<Long> get() = stringList.map { it.toLong() }
    val floatList: List<Float> get() = stringList.map { it.toFloat() }
    val doubleList: List<Double> get() = stringList.map { it.toDouble() }
    val boolList: List<Boolean> get() = stringList.map { it.toBooleanValue() }

    fun removeAt(index: Int) {
        data.removeAt(index)
    }

    fun add(value: Any) {
        data.add(anyToConfigValue(value))
    }

    fun addAll(values: Iterable<*>) {
        data.addAll(values.map { anyToConfigValue(it as Any) })
    }

    fun addAll(values: Array<*>) {
        data.addAll(values.map { anyToConfigValue(it as Any) })
    }

    operator fun plusAssign(value: Any) {
        add(value)
    }

    override fun toString(): String {
        return Configs.serialize(this)
    }

}

internal fun anyToConfigValue(value: Any): Any {
    return when (value) {
        is ConfigList -> value
        is ConfigMap -> value
        is String -> value
        is Number -> value.toString()
        is Boolean -> value.toString()
        is Char -> value.toString()
        is Map<*, *> -> ConfigMap(value.remap(keyBlock = { anyToConfigValue(it as Any).toString() }, valueBlock = { anyToConfigValue(it as Any) }))
        is Iterable<*> -> ConfigList().apply {
            data.addAll(value.map { anyToConfigValue(it as Any) })
        }

        else -> {
            if (value::class.java.isArray) {
                val ls = ConfigList()
                val length: Int = java.lang.reflect.Array.getLength(value)
                ls.data.ensureCapacity(length)
                for (i in 0..<length) {
                    val v = java.lang.reflect.Array.get(value, i)
                    ls.add(anyToConfigValue(v))
                }
                ls
            } else {
                value.toString()
            }
        }
    }
}

internal fun <K, V, K2, V2> Map<K, V>.remap(keyBlock: (K) -> K2, valueBlock: (V) -> V2): java.util.LinkedHashMap<K2, V2> {
    val m = LinkedHashMap<K2, V2>(this.size + this.size / 2)
    for (e in this) {
        m[keyBlock(e.key)] = valueBlock(e.value)
    }
    return m
}

internal fun <K, V, K2, V2> Map<K, V>.remapTo(newMap: MutableMap<K2, V2>, keyBlock: (K) -> K2, valueBlock: (V) -> V2) {
    for (e in this) {
        newMap[keyBlock(e.key)] = valueBlock(e.value)
    }
}