package com.petrukhnov.prototypes.aoa.androidaoaapp

import java.io.IOException

interface ConnectionInputStream {
    @Throws(IOException::class)
    fun read(rxBuffer: ByteArray?): Int

    @Throws(IOException::class)
    fun close()
}