package io.github.yangentao.config

import io.github.yangentao.charcode.CharCode
import io.github.yangentao.charcode.TextScanner

internal class ConfigParser(json: String) {
    val ts = TextScanner(json)

    fun skipWhites() {
        while (!ts.isEnd) {
            var n = ts.skipWhites().size
            if (!ts.isEnd && ts.nowChar == CharCode.SHARP) n += skipComment()
            if (n == 0) break
        }
    }

    fun parse(): Any? {
        skipWhites()
        if (ts.isEnd) return null
        val ch = ts.nowChar
        val value = when (ch) {
            CharCode.LCUB -> parseObject()
            CharCode.LSQB -> parseArray()
            else -> parseObject(needCube = false)
        }
        skipWhites()
        if (!ts.isEnd) raise()
        return value
    }

    private fun skipComment(): Int {
        var n = 0
        while (!ts.isEnd && ts.nowChar != CharCode.CR && ts.nowChar != CharCode.LF) {
            n += 1
            ts.skip(size = 1)
        }
        while (!ts.isEnd && (ts.nowChar == CharCode.CR || ts.nowChar == CharCode.LF)) {
            n += 1
            ts.skip(size = 1)
        }
        return n
    }

    private fun parseValue(): Any {
        skipWhites()
        if (ts.isEnd) raise()
        val ch = ts.nowChar
        return when (ch) {
            CharCode.LCUB -> parseObject()
            CharCode.LSQB -> parseArray()
            else -> parseString()
        }
    }

    private fun parseObject(needCube: Boolean = true): ConfigMap {
        skipWhites()
        val map = ConfigMap()
        if (needCube) ts.expectChar(CharCode.LCUB)
        while (!ts.isEnd && ts.nowChar != CharCode.RCUB) {
            skipWhites()
            val key = parseKey()
            skipWhites()
            ts.expectAnyChar(ASSIGN)
            val v = parseValue()
            map.put(key, v)
            val trails = ts.skipChars(TRAIL)
            if (!ts.isEnd && ts.nowChar != CharCode.RCUB) {
                if (trails.intersect(SEP).isEmpty()) raise()
            }
        }
        if (needCube) ts.expectChar(CharCode.RCUB)
        return map
    }

    private fun parseArray(): ConfigList {
        skipWhites()
        val list = ConfigList()
        ts.expectChar(CharCode.LSQB)
        skipWhites()
        while (ts.nowChar != CharCode.RSQB) {
            skipWhites()
            val v = parseValue()
            list.add(v)
            val trails = ts.skipChars(TRAIL)
            if (ts.nowChar != CharCode.RSQB) {
                if (trails.intersect(SEP).isEmpty()) raise()
            }
        }
        ts.expectChar(CharCode.RSQB)
        return list
    }

    private fun parseKey(): String {
        val charList = ts.moveNext(terminator = { it in ASSIGN && ts.preChar != CharCode.BSLASH })
        val s = codesToString(charList)
        return s.trim()
    }

    private fun parseString(): String {
        val charList = ts.moveNext(terminator = { (it in SEP || it == CharCode.SHARP || it == CharCode.RCUB || it == CharCode.RSQB) && ts.preChar != CharCode.BSLASH })
        val s = codesToString(charList)
        return s.trim()
    }

    private fun raise(msg: String = "Json parse error"): Nothing {
        error("$msg. ${ts.position}, ${ts.leftText}")
    }

}

private val ASSIGN: Set<Char> = setOf(CharCode.COLON, CharCode.EQUAL)
private val SEP: Set<Char> = setOf(CharCode.COMMA, CharCode.SEMI, CharCode.CR, CharCode.LF)
private val TRAIL: Set<Char> = setOf(CharCode.SP, CharCode.HTAB, CharCode.CR, CharCode.LF, CharCode.COMMA, CharCode.SEMI)

private fun codesToString(charList: List<Char>): String {
    val buf = ArrayList<Char>()
    var escaping = false
    var i = 0
    while (i < charList.size) {
        val ch = charList[i]
        if (!escaping) {
            if (ch == CharCode.BSLASH) {
                escaping = true;
            } else {
                buf.add(ch);
            }
        } else {
            escaping = false;
            when (ch) {
                CharCode.SQUOTE, CharCode.BSLASH, CharCode.SLASH -> {
                    buf.add(ch);
                }

                CharCode.b -> buf.add(CharCode.BS)
                CharCode.f -> buf.add(CharCode.FF)
                CharCode.n -> buf.add(CharCode.LF)
                CharCode.r -> buf.add(CharCode.CR)
                CharCode.t -> buf.add(CharCode.HTAB)
                CharCode.u, CharCode.U -> {
                    val uls = ArrayList<Char>()
                    i += 1;
                    if (i < charList.size && charList[i] == CharCode.PLUS) {
                        i += 1;
                    }
                    while (i < charList.size && uls.size < 4 && CharCode.isHex(charList[i])) {
                        uls.add(charList[i]);
                        i += 1;
                    }
                    if (uls.size != 4) error("Convert to string failed: ${String(charList.toCharArray())}.");
                    val s = String(uls.toCharArray())
                    val n = s.toInt(16)
                    val charArr = Character.toChars(n)
                    for (c in charArr) buf.add(c)
                    i -= 1;
                }

                else -> buf.add(ch)
            }
        }
        i += 1;
    }
    return String(buf.toCharArray())
}

internal fun encodeString(s: String): String {
    val chars: CharArray = s.toCharArray()
    val buf: ArrayList<Char> = ArrayList()
    var i: Int = 0
    while (i < chars.size) {
        val ch = chars[i]
        if (ch < CharCode.SP) {
            when (ch) {
                CharCode.BS -> {
                    buf.add(CharCode.BSLASH)
                    buf.add(CharCode.b)
                }

                CharCode.FF -> {
                    buf.add(CharCode.BSLASH)
                    buf.add(CharCode.f)
                }

                CharCode.LF -> {
                    buf.add(CharCode.BSLASH)
                    buf.add(CharCode.n)
                }

                CharCode.CR -> {
                    buf.add(CharCode.BSLASH)
                    buf.add(CharCode.r)
                }

                CharCode.HTAB -> {
                    buf.add(CharCode.BSLASH)
                    buf.add(CharCode.t)
                }

                else -> {
                    val x: Int = ch.code
                    buf.add(CharCode.BSLASH)
                    buf.add(CharCode.u)
                    buf.add(CharCode.NUM0)
                    buf.add(CharCode.NUM0)
                    buf.add(lastHex(x shr 4))
                    buf.add(lastHex(x))
                }
            }
        } else if (CharCode.isUnicodeLead(ch) && (i + 1 < chars.size) && CharCode.isUnicodeTrail(chars[i + 1])) {
            val x: Int = ch.code
            buf.add(CharCode.BSLASH)
            buf.add(CharCode.u)
            buf.add(CharCode.d)
            buf.add(lastHex(x shr 8))
            buf.add(lastHex(x shr 4))
            buf.add(lastHex(x))

            val y = chars[i + 1].code
            buf.add(CharCode.BSLASH)
            buf.add(CharCode.u)
            buf.add(CharCode.d)
            buf.add(lastHex(y shr 8))
            buf.add(lastHex(y shr 4))
            buf.add(lastHex(y))
            i += 1
        } else {
            when (ch) {
                CharCode.SQUOTE -> {
                    buf.add(CharCode.BSLASH);
                    buf.add(CharCode.SQUOTE);
                }

                CharCode.BSLASH -> {
                    buf.add(CharCode.BSLASH);
                    buf.add(CharCode.BSLASH);
                }

                CharCode.SLASH -> {
                    buf.add(CharCode.BSLASH);
                    buf.add(CharCode.SLASH);
                }

                else -> {
                    buf.add(ch)
                }
            }

        }
        i += 1
    }
    return String(buf.toCharArray())
}

// '0' + x  or  'a' + x - 10
private fun hex4(n: Int): Char = Char(if (n < 10) 48 + n else 87 + n)
private fun lastHex(n: Int): Char = hex4(n and 0x0F)

private fun isUTF16(a: Char, b: Char): Boolean {
    return CharCode.isUnicodeLead(a) && CharCode.isUnicodeTrail(b)
}