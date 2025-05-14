package config

import kotlin.test.Test

internal  class DoubleTest {
    @Test
    fun doubleTest(){
        val a:Double = 123.4
        println(a.toString())
        val b :Double = 123.0
        println(b.toString())
//        println(b == 122 + 1.0)
        println(12.toDouble())
    }
}