import astify.*
import astify.P
import astify.Token
import astify.util.*

internal val jsonValueParser: P<Token, JSONValue> = parser2 { branch(
        string to (string mapv { JSONString(it.value) }),
        numeric to numeric,
        symbol("-") to (numeric mapv { JSONNumber(-it.value) } preceededBy symbol("-")),
        keyword("true") to (keyword("true") mapv { JSONBoolean(true) }),
        keyword("false") to (keyword("false") mapv { JSONBoolean(false) }),
        keyword("null") to (keyword("null") mapv { JSONNull }),
        symbol("{") to lazy { jsonObjectParser },
        symbol("[") to lazy { jsonArrayParser }
) }

internal val ParserContext<Token>.numeric get()
= number mapv { JSONNumber(it.value) } or (integer mapv { JSONNumber(it.value.toFloat()) })

internal val jsonObjectMemberParser = parser2<Token, Pair<String, JSONValue>> {
    string mapv { it.value } proceededBy symbol(":") then jsonValueParser
}

internal val jsonObjectParser = parser2<Token, JSONValue> {
    wrappedCommaSeparated("{", "}", jsonObjectMemberParser) mapv { JSONObject(it.toMap()) }
}

internal val jsonArrayParser = parser2<Token, JSONValue> {
    wrappedCommaSeparated("[", "]", jsonValueParser) mapv { JSONArray(it) } }

internal val jsonLexer = lexerParser(setOf("true", "false", "null"))

//////////////////////////////////////////////////////////////////////////////////////////

private fun <T> ParserContext<Token>.wrappedCommaSeparated(s: String, e: String, term: P<Token, T>)
        = symbol(s) then symbol(e) mapv { listOf<T>() } or
        wrap(term sepBy symbol(","), symbol(s), symbol(e))

private fun deString(s: String)
        = unescapeSpecialCharacters(s.substring(1, s.length - 1))

private fun unescapeSpecialCharacters(str: String)
        = str.replace(Regex("\\\\(.)")) { it.groupValues[1] }
