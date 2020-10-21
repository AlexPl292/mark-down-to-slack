fun main() {
    DownParser(
        """
        ~~this is a good day~~
    """.trimIndent()
    ).toSlack()
}
