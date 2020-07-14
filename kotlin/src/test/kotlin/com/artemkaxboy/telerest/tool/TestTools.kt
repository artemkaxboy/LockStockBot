package com.artemkaxboy.telerest.tool

import java.io.ByteArrayOutputStream
import java.io.PrintStream

fun <T : Any?> getOutput(
    block: () -> T
): Triple<T, String, String> {

    val out = ByteArrayOutputStream()
    val originalOut = System.out
    System.setOut(PrintStream(out))

    val err = ByteArrayOutputStream()
    val originalErr = System.err
    System.setErr(PrintStream(err))

    try {
        return Triple(block(), out.toString(), err.toString())
    } finally {
        System.setOut(originalOut)
        System.setErr(originalErr)
    }
}
