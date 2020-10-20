import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DownParserTest {

    @Test
    fun `test header`() = assertParsing("# Header", "*Header*\n")

    @Test
    fun `test header 2`() = assertParsing("## Header", "*Header*\n")

    private fun assertParsing(md: String, slack: String) {
        val result = DownParser(md).toSlack()
        assertEquals(slack, result)
    }
}
