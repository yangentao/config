package config

import io.github.yangentao.config.Configs
import kotlin.test.Test
import kotlin.test.assertEquals

internal class YConfigTestsParse {

    @Test
    fun test1() {
        val stest = """{a=[ 1,2,,]}""".trimMargin()
        val value = Configs.parse(stest, true)
        println(value)
        assertEquals(2, value.getPath("a.1")?.asInt)
    }

    @Test
    fun testComment() {
        val s = """
            #comment
            {
            # comment
            a:123 # this is comment 
            b=[1,2,3]
            }
            #comment
        """.trimIndent()
        val v = Configs.parse(s)
        println(v)
    }

    @Test
    fun test2() {
        val value = Configs.parse(testText)
        println(value.getPath("aa"))
        println(value.getPath("ab"))
        println(value.getPath("ab.0"))
        println(value.getPath("ab.1"))
        println(value.toString())
        assertEquals("[1,2,3]", value.getPath("ls")?.toString())
    }

    private val testText: String = """
    aa = [,]
    
    ab=[1,2,3,,]
    ls:[    1,
    2;
    3    ,
    ],
    entao:{
        name:entao;
        addr={prov:shandong;city:jinan}
        sex:male
        a:b
        c:d
    }
    host= entao.dev\  ;
    port = 80 ;
    yang.en.tao = 999
    entao.name=yang 
    tao=
    a=
    
    b=123
    
""".trimIndent()
}