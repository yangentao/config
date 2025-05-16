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
class ConfigList(val data: ArrayList<Any> = ArrayList()) : MutableList<Any> by data {

    val stringList: List<String> get() = data.mapNotNull { it as? String }
    val intList: List<Int> get() = stringList.map { it.toInt() }
    val longList: List<Long> get() = stringList.map { it.toLong() }
    val floatList: List<Float> get() = stringList.map { it.toFloat() }
    val doubleList: List<Double> get() = stringList.map { it.toDouble() }
    val boolList: List<Boolean> get() = stringList.map { it.toBooleanValue() }

    override fun toString(): String {
        return Configs.serialize(this)
    }

}