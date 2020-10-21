import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.CompositeASTNode
import org.intellij.markdown.ast.accept
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.ast.visitors.Visitor
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.html.TrimmingInlineHolderProvider
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkdownParser
import java.net.URI

class DownParser(private val content: String) {
    private val parser = MarkdownParser(GFMFlavourDescriptor())
    private val tree = parser.buildMarkdownTreeFromString(content)

    fun toSlack(): CharSequence {
        var result: CharSequence = ""

//        toHtml()

        tree.accept(object : Visitor {
            override fun visitNode(node: ASTNode) {
                result = reg(node)
            }

            private fun reg(node: ASTNode): CharSequence {
                val processed = process(node)
                if (processed != null) return processed
                return if (node is CompositeASTNode) {
                    var content = ""
                    for (child in node.children) {
                        content += reg(child)
                    }
                    content
                } else node.getTextInNode(content).toString()
            }

            private fun process(node: ASTNode): CharSequence? {
                return when (node.type) {
                    MarkdownElementTypes.ATX_1 -> {
                        val children = node.children
                        if (children.size > 1) {
                            children[1].wrapWith("*", content) + "\n"
                        } else null
                    }
                    MarkdownElementTypes.ATX_2, MarkdownElementTypes.ATX_3 -> {
                        val children = node.children
                        if (children.size > 1) {
                            "*" + children[1].getTextInNode(content).drop(1) + "*\n"
                        } else null
                    }
                    MarkdownElementTypes.STRONG -> node.children.findText()?.wrapWith("*", content)
                    MarkdownElementTypes.EMPH -> node.children.findText()?.wrapWith("_", content)
                    GFMElementTypes.STRIKETHROUGH -> node.children.findText()?.wrapWith("~", content)
                    MarkdownTokenTypes.LIST_BULLET -> "• "
                    MarkdownElementTypes.INLINE_LINK -> {
                        val url = node.children.find { it.type == MarkdownElementTypes.LINK_DESTINATION }
                            ?.getTextInNode(content) ?: return null
                        val text = node.children.find { it.type == MarkdownElementTypes.LINK_TEXT }
                            ?.children?.find { it.type == MarkdownTokenTypes.TEXT }
                            ?.getTextInNode(content)
                        if (text != null) "<$url|$text>" else "<$url>"
                    }
                    MarkdownElementTypes.CODE_BLOCK -> {
                        val code = node.children.joinToString(separator = "") { reg(it) }
                        "```\n$code\n```"
                    }
                    MarkdownTokenTypes.CODE_LINE -> node.getTextInNode(content).drop(4)
                    else -> null
                }
            }
        })

        return result
    }

    private fun List<ASTNode>.findText(): ASTNode? = this.singleOrNull { it.type == MarkdownTokenTypes.TEXT }

    private fun ASTNode.wrapWith(wrapper: String, content: String): String {
        return wrapper + this.getTextInNode(content) + wrapper
    }

    private fun toHtml() {
        val flavourDescriptor = ChangelogFlavourDescriptor()
        val generateHtml = HtmlGenerator(
            content,
            MarkdownParser(flavourDescriptor).buildMarkdownTreeFromString(content),
            flavourDescriptor,
            false
        ).generateHtml()
        println(generateHtml)
    }
}

class ChangelogFlavourDescriptor : GFMFlavourDescriptor() {

    override fun createHtmlGeneratingProviders(linkMap: LinkMap, baseURI: URI?) =
        super.createHtmlGeneratingProviders(linkMap, baseURI) + hashMapOf(
            MarkdownElementTypes.MARKDOWN_FILE to TrimmingInlineHolderProvider()
        )
}
