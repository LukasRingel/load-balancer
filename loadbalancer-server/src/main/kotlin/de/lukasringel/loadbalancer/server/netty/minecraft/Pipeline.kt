package de.lukasringel.loadbalancer.server.netty.minecraft

import io.netty.buffer.ByteBuf
import java.nio.charset.StandardCharsets

object Pipeline {

  fun writeString(s: String, buf: ByteBuf?) {
    if (s.length > Short.MAX_VALUE) {
      throw RuntimeException("Cannot send string longer than Short.MAX_VALUE (got " + s.length + " characters)")
    }
    val b: ByteArray = s.toByteArray(Charsets.UTF_8)
    writeVarInt(b.size, buf)
    buf?.writeBytes(b)
  }

  fun readString(buf: ByteBuf?, maxLen: Int): String {
    val len = readVarInt(buf)
    if (len > maxLen * 4) {
      throw RuntimeException("Cannot receive string longer than " + maxLen * 4 + " (got " + len + " bytes)")
    }
    val byteArray = ByteArray(len)
    buf?.readBytes(byteArray)
    val string = String(byteArray, StandardCharsets.UTF_8)
    if (string.length > maxLen) {
      throw RuntimeException("Cannot receive string longer than " + maxLen + " (got " + string + "/" + string.length + " characters)")
    }
    return string
  }

  fun writeVarInt(value: Int, output: ByteBuf?) {
    var value = value
    var part: Int
    while (true) {
      part = value and 0x7F
      value = value ushr 7
      if (value != 0) {
        part = part or 0x80
      }
      output?.writeByte(part)
      if (value == 0) {
        break
      }
    }
  }

  fun readVarInt(input: ByteBuf?): Int {
    return readVarInt(input, 5)
  }

  private fun readVarInt(input: ByteBuf?, maxBytes: Int): Int {
    var out = 0
    var bytes = 0
    var inputBytes: Byte
    while (true) {
      inputBytes = input?.readByte()!!
      out = out or (inputBytes.toInt() and 0x7F shl bytes++ * 7)
      if (bytes > maxBytes) {
        throw RuntimeException("VarInt too big")
      }
      if (inputBytes.toInt() and 0x80 != 0x80) {
        break
      }
    }
    return out
  }

}