package config

import io.github.yangentao.config.ConfigList
import io.github.yangentao.config.ConfigMap
import kotlin.test.Test

internal class YConfigTests {

    @Test
    fun testMapWrite() {
        val m = ConfigMap()
        m["a"] = arrayOf(1, 2, 3)
        m["b"] = listOf(11, 22, 33)
        m["C"] = 123
        println(m)
    }

    @Test
    fun testListWrite() {
        val m = ConfigList()
        m.add(1)
        m.add("a")
        m.addAll(arrayOf(1, 2, 3))
        m.addAll(listOf(11, 22, 33))
        println(m)
    }

    @Test
    fun testListWrite2() {
        val m = ConfigList()
        m += 1
        m += "a"
        m += listOf("aa", "bb")
        println(m)
    }
}