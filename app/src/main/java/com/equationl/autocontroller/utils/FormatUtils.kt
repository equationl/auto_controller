package com.equationl.autocontroller.utils

import java.util.*

object FormatUtils {

    /**
     * 将十六进制字符串转成 ByteArray
     * */
    fun hexStrToBytes(hexString: String): ByteArray {
        check(hexString.length % 2 == 0) { return ByteArray(0) }

        return hexString.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    /**
     * 将十六进制字符串转成 ByteArray
     * */
    fun String.toBytes(): ByteArray {
        return hexStrToBytes(this)
    }

    /**
     * 将 ByteArray 转成 十六进制字符串
     * */
    fun bytesToHexStr(byteArray: ByteArray) =
        with(StringBuilder()) {
            byteArray.forEach {
                val hex = it.toInt() and (0xFF)
                val hexStr = Integer.toHexString(hex)
                if (hexStr.length == 1) append("0").append(hexStr)
                else append(hexStr)
            }
            toString().uppercase(Locale.CHINA)
        }

    /**
     * 将字节数组转成十六进制字符串
     * */
    fun ByteArray.toHexStr(): String {
        return bytesToHexStr(this)
    }

    /**
     * 将字节数组解析成文本（ASCII）
     * */
    fun ByteArray.toText(): String {
        return String(this)
    }

    /**
     * 将 ByteArray 转为 bit 字符串
     * */
    fun ByteArray.toBitsStr(): String {
        if (this.isEmpty()) return ""
        val sb = java.lang.StringBuilder()
        for (aByte in this) {
            for (j in 7 downTo 0) {
                sb.append(if (aByte.toInt() shr j and 0x01 == 0) '0' else '1')
            }
        }
        return sb.toString()
    }

    /**
     *
     * 将十六进制字符串转成 ASCII 文本
     *
     * */
    fun String.toText(): String {
        val output = java.lang.StringBuilder()
        var i = 0
        while (i < this.length) {
            val str = this.substring(i, i + 2)
            output.append(str.toInt(16).toChar())
            i += 2
        }
        return output.toString()
    }

    /**
     * 将十六进制字符串转为带符号的 Int
     * */
    fun String.toNumber(): Int {
        return this.toInt(16).toShort().toInt()
    }

    /**
     * 将整数转成有符号十六进制字符串
     *
     * @param length 返回的十六进制总长度，不足会在前面补 0 ，超出会将前面多余的去除
     * */
    fun Int.toHex(length: Int = 4): String {
        val hex = Integer.toHexString(this).uppercase(Locale.CHINA)
        return hex.padStart(length, '0').drop((hex.length-length).coerceAtLeast(0))
    }

}