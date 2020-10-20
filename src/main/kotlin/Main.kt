fun main() {
    DownParser("""
        # Header
        ### Second header
        hello **this is a good day** *this is a good day*
    """.trimIndent()).toSlack()
}