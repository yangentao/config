package io.github.yangentao.config

import io.github.yangentao.anno.userName
import kotlin.reflect.KProperty

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
class ConfigMap(val data: LinkedHashMap<String, Any> = LinkedHashMap()) : Map<String, Any> by data {
    fun getString(key: String): String? = get(key) as? String
    fun getBool(key: String): Boolean? = getString(key)?.toBooleanValue()
    fun getInt(key: String): Int? = getString(key)?.toInt()
    fun getLong(key: String): Long? = getString(key)?.toLong()
    fun getFloat(key: String): Float? = getString(key)?.toFloat()
    fun getDouble(key: String): Double? = getString(key)?.toDouble()
    fun getList(key: String): ConfigList? = get(key) as? ConfigList
    fun getMap(key: String): ConfigMap? = get(key) as? ConfigMap

    override fun toString(): String {
        return Configs.serialize(this)
    }

    // TODO List, Set, Map
    inline operator fun <reified T : Any> getValue(thisRef: ConfigMap, property: KProperty<*>): T? {
        val cls = T::class
        val key = property.userName
        return when (cls) {
            String::class -> getString(key) as? T
            Int::class -> getInt(key) as? T
            Long::class -> getLong(key) as? T
            Float::class -> getFloat(key) as? T
            Double::class -> getDouble(key) as? T
            Boolean::class -> getBool(key) as? T
            ConfigList::class -> getList(key) as? T
            ConfigMap::class -> getMap(key) as? T
            else -> null
        }
    }

    inline operator fun <reified T : Any> setValue(thisRef: ConfigMap, property: KProperty<*>, value: T?) {
        val key = property.userName
        this[key] = value
    }

    fun put(key: String, value: Any?) {
        this[key] = value
    }

    operator fun set(key: String, value: Any?) {
        if (value == null) {
            data.remove(key)
        } else {
            data[key] = anyToConfigValue(value)
        }
    }

    fun remove(key: String): Any? = data.remove(key)

}