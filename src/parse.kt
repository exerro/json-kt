import astify.Position
import astify.TextStream
import astify.monadic.*
import astify.monadic.util.ListParserState
import astify.monadic.util.TextStreamParserState

fun jsonParse(value: String): JSONValue {
    val stream = TextStream.create(value)
    val tsps = TextStreamParserState.new(stream)
    val tokens = guard(
            stream,
            jsonLexer.parse(tsps)
    )
    val lps = ListParserState.new(tokens, stream)
    return guard(stream, thenEOF(jsonValueParser).parse(lps))
}

private fun <State: PositionedTokenParserState<State, *, Position>, Value> guard(
        stream: TextStream,
        value: ParseResult<State, String, Value>
): Value = when (value) {
    is ParseResult.Success -> value.value
    is ParseResult.Failure -> throw JSONDecodeError(formatErrorInternal(
            value, stream, true
    ))
}

private fun <State: PositionedTokenParserState<State, *, Position>> formatErrorInternal(
        fail: ParseResult.Failure<State, String>,
        str: TextStream,
        includePosition: Boolean
): String {
    val pos = fail.state.position
    val start = fail.error + if (includePosition) "\n" + pos.linePointer(str) else ""

    return if (fail.causes.isEmpty()) start else {
        val includeSubPosition = fail.causes.any { it.state.position != pos }
        start + ("\n caused by\n" + fail.causes.joinToString("\n") {
            "\t" + formatErrorInternal(it, str, includeSubPosition)
                    .replace("\n", "\n\t")
        })
    }
}
