import astify.*
import astify.P
import astify.Token
import astify.util.*

internal val jsonValueParser: TP<JSONValue> = tokenP { branch(
        string to (string map { JSONString(it.value) }),
        numeric to numeric,
        symbol("-") to (symbol("-") keepRight numeric map { JSONNumber(-it.value) }),
        keyword("true") to (keyword("true") map { JSONBoolean(true) }),
        keyword("false") to (keyword("false") map { JSONBoolean(false) }),
        keyword("null") to (keyword("null") map { JSONNull }),
        symbol("{") to lazy { jsonObjectParser },
        symbol("[") to lazy { jsonArrayParser }
) }

internal val TokenParser.numeric get()
= number map { JSONNumber(it.value) } or (integer map { JSONNumber(it.value.toFloat()) })

internal val jsonObjectMemberParser: TP<Pair<String, JSONValue>> = tokenP {
    string map { it.value } keepLeft symbol(":") and jsonValueParser
}

internal val jsonObjectParser: TP<JSONValue> = tokenP {
    wrappedCommaSeparated("{", "}", jsonObjectMemberParser) map { JSONObject(it.toMap()) }
}

internal val jsonArrayParser: TP<JSONValue> = tokenP {
    wrappedCommaSeparated("[", "]", jsonValueParser) map { JSONArray(it) } }

internal val jsonLexer = lexerParser(setOf("true", "false", "null"))

//////////////////////////////////////////////////////////////////////////////////////////

private fun <T> TokenParser.wrappedCommaSeparated(s: String, e: String, term: P<Token, T>)
        = symbol(s) keepRight symbol(e) map { listOf<T>() } or
          wrap(term sepBy symbol(","), symbol(s), symbol(e))

private fun unescapeSpecialCharacters(str: String)
        = str.replace(Regex("\\\\(.)")) { it.groupValues[1] }
