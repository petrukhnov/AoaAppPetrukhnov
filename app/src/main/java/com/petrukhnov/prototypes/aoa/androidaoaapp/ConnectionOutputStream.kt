package com.petrukhnov.prototypes.aoa.androidaoaapp

import java.io.IOException

interface ConnectionOutputStream {
    @Throws(IOException::class)
    fun write(txBuffer: ByteArray?)

    @Throws(IOException::class)
    fun close()
}