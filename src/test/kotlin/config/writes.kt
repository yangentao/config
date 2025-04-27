package config

import io.github.yangentao.config.ConfigMap
import kotlin.test.Test
import kotlin.test.assertEquals

internal class YConfigTests {

    @Test
    fun testMapWrite() {
        val m = ConfigMap()
//    m.path("name", "entao")
//    m.path("user.name", "yang")
        m.setPath("user.name.first", "yang")
        m.setPath("user.name.last", "entao")
        println(m)
        assertEquals("{first:yang,last:entao}", m.getPath("user.name").toString())
        assertEquals("yang", m.getPath("user.name.first").toString())
    }

}