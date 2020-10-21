package dev.feedforward.markdownto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DownParserTest {

    @Test
    fun `test header`() = assertParsing("# Header", "*Header*")

    @Test
    fun `test header 2`() = assertParsing("## Header", "*Header*")

    @Test
    fun `test header 3`() = assertParsing("### Header", "*Header*")

    @Test
    fun `test header and text`() = assertParsing("## Header\nSome Text", "*Header*\nSome Text")

    @Test
    fun `test strong`() = assertParsing("**strong**", "*strong*")

    @Test
    fun `test emph`() = assertParsing("*strong*", "_strong_")

    @Test
    fun `test strong 2`() = assertParsing("__strong__", "*strong*")

    @Test
    fun `test emph 2`() = assertParsing("_strong_", "_strong_")

    @Test
    fun `strike out`() = assertParsing("~~word~~", "~word~")

    @Test
    fun `unordered list`() = assertParsing("* One\n* Two", "• One\n• Two")

    @Test
    fun `inline link`() = assertParsing("[ya](ya.ru)", "<ya.ru|ya>")

    @Test
    fun `empty inline link`() = assertParsing("[](ya.ru)", "<ya.ru>")

    @Test
    fun `complicated list`() = assertParsing("""
        * Item1
        * **Item2**
        * [Item3](ya.ru)
    """.trimIndent(), """
        • Item1
        • *Item2*
        • <ya.ru|Item3>
    """.trimIndent())

    @Test
    fun `code block`() = assertParsing(
        """
        |    hello
        |    this is code
    """.trimMargin(), """
        |```
        |hello
        |this is code
        |```
    """.trimMargin()
    )

    private fun assertParsing(md: String, slack: String) {
        val result = DownParser(md).toSlack()
        assertEquals(slack, result)
    }
}
