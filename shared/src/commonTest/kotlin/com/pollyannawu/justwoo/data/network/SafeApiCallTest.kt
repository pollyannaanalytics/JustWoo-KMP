package com.pollyannawu.justwoo.data.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SafeApiCallTest {

    @Test
    fun `parseServerErrorMessage extracts error field from ktor exception message`() {
        val raw = """Client request(POST http://host/houses/1/settlements) invalid: 400 Bad Request. Text: "{"error":"Amount must be greater than zero"}""""
        assertEquals("Amount must be greater than zero", parseServerErrorMessage(raw))
    }

    @Test
    fun `parseServerErrorMessage extracts invalid currency error`() {
        val raw = """Client request(POST http://host/houses/1/settlements) invalid: 400 Bad Request. Text: "{"error":"Unknown or unsupported currency code: TWD"}""""
        assertEquals("Unknown or unsupported currency code: TWD", parseServerErrorMessage(raw))
    }

    @Test
    fun `parseServerErrorMessage returns null when no Text section`() {
        assertNull(parseServerErrorMessage("Connection refused"))
    }

    @Test
    fun `parseServerErrorMessage returns null when body has no error field`() {
        val raw = """Client request(GET http://host/path) invalid: 404 Not Found. Text: "Not Found""""
        assertNull(parseServerErrorMessage(raw))
    }

    @Test
    fun `withServerMessage returns exception with clean message when error field present`() {
        val raw = """Client request(POST http://host/path) invalid: 400 Bad Request. Text: "{"error":"House not found"}""""
        val original = Exception(raw)
        val result = original.withServerMessage()
        assertEquals("House not found", result.message)
        assertEquals(original, result.cause)
    }

    @Test
    fun `withServerMessage returns this when no error field`() {
        val original = Exception("Connection refused")
        val result = original.withServerMessage()
        assertEquals(original, result)
    }
}
