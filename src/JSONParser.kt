
internal val jsonValueParser: P<JSONValue> = parser { p { branch(
        string to (token map { JSONString(deString(it.text)) }),
        oneOf(number, integer) to (token map { JSONNumber(it.text.toFloat()) }),
        keyword("true") to (token map { JSONBoolean(true) }),
        keyword("false") to (token map { JSONBoolean(false) }),
        keyword("null") to (token map { JSONNull }),
        symbol("{") to jsonObjectParser,
        symbol("[") to jsonArrayParser
) } }

internal val jsonObjectMemberParser = parser {
    text(string) map ::deString followedBy symbol(":") andThen jsonValueParser }

internal val jsonObjectParser = parser {
    wrappedCommaSeparated("{", "}", jsonObjectMemberParser) map { JSONObject(it.toMap()) } }

internal val jsonArrayParser = parser {
    wrappedCommaSeparated("[", "]", jsonValueParser) map { JSONArray(it) } }

internal fun jsonLexer(s: TextStream)
        = Lexer(s, LexerTools.keywords(listOf("true", "false", "null")) lexUnion LexerTools.defaults)

//////////////////////////////////////////////////////////////////////////////////////////

private fun <T> wrappedCommaSeparated(s: String, e: String, term: P<T>) = parser.sequence {
    p { symbol(s) }

    if (p { optional(symbol(e)) } == null) {
        val items = p { term sepBy symbol(",") }
        p { symbol(e) }
        items
    }
    else listOf()
}

private fun deString(s: String)
        = unescapeSpecialCharacters(s.substring(1, s.length - 1))

private fun unescapeSpecialCharacters(str: String)
        = str.replace(Regex("\\\\(.)")) { it.groupValues[1] }
