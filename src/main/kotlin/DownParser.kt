import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.CompositeASTNode
import org.intellij.markdown.ast.accept
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.ast.visitors.Visitor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.html.TrimmingInlineHolderProvider
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkdownParser
import java.net.URI

class DownParser(private val content: String) {
    private val parser = MarkdownParser(CommonMarkFlavourDescriptor())
    private val tree = parser.buildMarkdownTreeFromString(content)

    fun toSlack(): String {
        val builder = StringBuilder()

        val flavourDescriptor = ChangelogFlavourDescriptor()
        val generateHtml = HtmlGenerator(
            content,
            MarkdownParser(flavourDescriptor).buildMarkdownTreeFromString(content),
            flavourDescriptor,
            false
        ).generateHtml()
        println(generateHtml)

        tree.accept(object : Visitor {
            override fun visitNode(node: ASTNode) {
                println(reg(node))
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
                            "*" + children[1].getTextInNode(content) + "*\n"
                        } else null
                    }
                    MarkdownElementTypes.ATX_2, MarkdownElementTypes.ATX_3 -> {
                        val children = node.children
                        if (children.size > 1) {
                            "*" + children[1].getTextInNode(content).drop(1) + "*\n"
                        } else null
                    }
                    MarkdownElementTypes.STRONG -> {
                        node.children.singleOrNull { it.type == MarkdownTokenTypes.TEXT }
                            ?.let { "*" + it.getTextInNode(content) + "*" }
                    }
                    MarkdownElementTypes.EMPH -> {
                        node.children.singleOrNull { it.type == MarkdownTokenTypes.TEXT }
                            ?.let { "_" + it.getTextInNode(content) + "_" }
                    }
                    else -> null
                }
            }
        })

        return builder.toString()
    }
}

class ChangelogFlavourDescriptor : GFMFlavourDescriptor() {

    override fun createHtmlGeneratingProviders(linkMap: LinkMap, baseURI: URI?) =
        super.createHtmlGeneratingProviders(linkMap, baseURI) + hashMapOf(
            MarkdownElementTypes.MARKDOWN_FILE to TrimmingInlineHolderProvider()
        )
}
