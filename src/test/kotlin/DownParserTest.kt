import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DownParserTest {

    @Test
    fun `test header`() = assertParsing("# Header", "*Header*\n")

    @Test
    fun `test header 2`() = assertParsing("## Header", "*Header*\n")

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

    private fun assertParsing(md: String, slack: String) {
        val result = DownParser(md).toSlack()
        assertEquals(slack, result)
    }
}
