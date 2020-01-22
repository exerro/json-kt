import astify.Token
import astify.monadic.*
import astify.monadic.util.*

typealias JSONParser<Value> = P<ListParserState<Token>, String, Value>
typealias JP<Value> = JSONParser<Value>

internal val jsonValueParser: JP<JSONValue> = p { oneOf(
        stringValue map (::JSONString),
        lazy { numeric },
        symbol("-") keepRight lazy { numeric } map { JSONNumber(-it.value) },
        keyword("true") map { JSONBoolean(true) },
        keyword("false") map { JSONBoolean(false) },
        keyword("null") map { JSONNull },
        lazy { jsonObjectParser },
        lazy { jsonArrayParser }
) }

internal val numeric: JP<JSONNumber> = p {
    numberValue map(::JSONNumber) or (integerValue map { JSONNumber(it.toFloat()) })
}

internal val jsonObjectMemberParser: JP<Pair<String, JSONValue>> = p {
    stringValue keepLeft symbol(":") and jsonValueParser
}

internal val jsonObjectParser: JP<JSONValue> = p {
    wrapDelimitedSymbols(jsonObjectMemberParser, "{", "}") map { JSONObject(it.toMap()) }
}

internal val jsonArrayParser: JP<JSONValue> = p {
    wrapDelimitedSymbols(jsonValueParser, "[", "]") map { JSONArray(it) } }

internal val jsonLexer = untilEOF(tokenParser(setOf("true", "false", "null")))
