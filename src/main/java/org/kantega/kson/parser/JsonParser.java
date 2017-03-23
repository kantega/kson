package org.kantega.kson.parser;

import fj.P;
import fj.P2;
import fj.data.List;
import fj.data.Stream;
import fj.data.Validation;
import fj.parser.Parser;
import fj.parser.Result;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.*;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static fj.parser.Parser.CharsParser.character;
import static fj.parser.Parser.CharsParser.characters;
import static org.kantega.kson.json.JsonObject.JsonObject;

public class JsonParser {

    static ParseFailure fail(Supplier<String> msg) {
        return new ParseFailure(msg);
    }

    private static final ParseFailure missingInput =
      fail(() -> "Missing input");

    // *** Tokens ***

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> space =
      singleChar(' ');

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> nl =
      sequence("\\n");

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> tab =
      on("\\t", "\t");

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> quote =
      on("\\\"", "\"");

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> formfeed =
      on("\\fail", "\f");

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> reverseSolidus =
      on("\\\\", "\\");

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> solidus =
      on("\\/", "/");

    private static final Parser<Stream<Character>, Character, ParseFailure> hex =
      Parser.StreamParser.satisfy(missingInput, (i) -> fail(() -> i + " is not a hexadecimal character"), ch -> Character.isDigit(ch) || "abcde".contains(ch.toString().toLowerCase()));

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> control =
      singleChar('u').bind(u -> hex.bind(h1 -> hex.bind(h2 -> hex.bind(h3 -> hex.map(h4 -> u.append(Stream.arrayStream(h1, h2, h3, h4)))))));

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> special =
      tab.or(nl).or(quote).or(formfeed).or(reverseSolidus).or(solidus).or(control);

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> whitespaces =
      space.or(singleChar('\n')).or(singleChar('\t'));

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> comma =
      singleChar(',');

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> lCurly =
      singleChar('{');

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> rCurly =
      singleChar('}');

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> lBracket =
      singleChar('[');

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> rBracket =
      singleChar(']');

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> dash =
      singleChar('-');

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> quot =
      singleChar('"');

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> point =
      singleChar('.');

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> colon =
      singleChar(':');

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> e =
      singleChar('e').or(singleChar('E')).or(sequence("e+")).or(sequence("e-")).or(sequence("E+")).or(sequence("E-"));

    private static final Parser<Stream<Character>, Character, ParseFailure> digit =
      Parser.StreamParser.satisfy(missingInput, (i) -> fail(() -> i + " is not a digit"), Character::isDigit);

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> digits =
      digit.repeat1();

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> exp =
      and(e, digits);

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> frac =
      and(point, digits);

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> intt =
      digits
        .or(and(dash, digits));


    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> number =
      intt
        .or(and(intt, frac))
        .or(and(intt, exp))
        .or(and(intt, frac, exp));

    private final static Validation<ParseFailure, Result<Stream<Character>, Stream<Character>>> failure =
      Validation.fail(new ParseFailure(() -> "end"));

    private final static Validation<ParseFailure, Result<Stream<Character>, Stream<Character>>> illegalslash =
      Validation.fail(new ParseFailure(() -> "\" or \\"));

    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> charr =
      Parser.parser(stream -> {
          if (stream.isEmpty())
              return
                failure;

          if (stream.head() == '"' || stream.head() == '\\')
              return
                illegalslash;

          StringBuilder mutableBuilder =
            new StringBuilder();

          Stream<Character> mutableTail =
            stream;

          while (!mutableTail.isEmpty() && mutableTail.head() != '"' && mutableTail.head() != '\\') {
              mutableBuilder.append(mutableTail.head());
              mutableTail = mutableTail.tail()._1();
          }
          return Validation.success(Result.result(mutableTail, Stream.fromString(mutableBuilder.toString())));
      });


    private static final Parser<Stream<Character>, Stream<Character>, ParseFailure> chars =
      flatten(charr.or(special).repeat());

    private static final Parser<Stream<Character>, String, ParseFailure> string =
      quot.bind(chars, quot, q1 -> cs -> q2 -> Stream.asString(cs));

    // *** Parser ***

    private static final Parser<Stream<Character>, JsonValue, ParseFailure> numberValue =
      number
        .map(cs -> new JsonNumber(new BigDecimal(Stream.asString(cs))));

    private static final Parser<Stream<Character>, JsonValue, ParseFailure> stringValue =
      string.map(JsonString::new);

    private static final Parser<Stream<Character>, JsonValue, ParseFailure> boolValue =
      string("true").map(s -> (JsonValue) new JsonBool(true)).or(string("false").map(s -> new JsonBool(false)));

    private static final Parser<Stream<Character>, JsonValue, ParseFailure> nullValue =
      string("null").map(s -> (JsonValue) new JsonNull());

    private static Parser<Stream<Character>, JsonValue, ParseFailure> value() {
        return lazy(() -> object().or(arrayValue()).or(nullValue).or(boolValue).or(numberValue).or(stringValue));
    }

    private static Parser<Stream<Character>, JsonValue, ParseFailure> emptyArray() {
        return and(trim(lBracket), trim(rBracket)).map(s -> (JsonValue) new JsonArray(List.nil()));
    }

    private static Parser<Stream<Character>, JsonValue, ParseFailure> nonEmptyArray() {
        return trim(lBracket).bind(value(), trim(comma).sequence(value()).repeat(), trim(rBracket),
          lb -> first -> rest -> rb -> new JsonArray(rest.cons(first).toList()));
    }

    private static Parser<Stream<Character>, JsonValue, ParseFailure> arrayValue() {
        return emptyArray().or(nonEmptyArray());
    }

    private static Parser<Stream<Character>, P2<String, JsonValue>, ParseFailure> pair() {
        return string.bind(trim(colon), value(), str -> c -> val -> P.<String, JsonValue>p(str, val));
    }

    private static Parser<Stream<Character>, JsonValue, ParseFailure> object() {
        return
          trim(lCurly).bind(pair(), trim(comma).sequence(pair()).repeat(), trim(rCurly), lc -> first -> rest -> rc -> JsonObject(rest.cons(first).toList()));
    }

    // *** Helpers ***

    private static Parser<Stream<Character>, Stream<Character>, ParseFailure> singleChar(Character c) {
        return character(missingInput, (i) -> fail(() -> "'" + i + "' is not a '" + c.toString() + "'"), c).map(Stream::single);
    }

    private static Parser<Stream<Character>, Stream<Character>, ParseFailure> sequence(String chars) {
        return characters(missingInput, (i) -> fail(() -> "'" + i + "' is not a '" + chars + "'"), Stream.fromString(chars));
    }

    private static Parser<Stream<Character>, String, ParseFailure> string(String chars) {
        return sequence(chars).map(Stream::asString);
    }

    @SafeVarargs
    private static Parser<Stream<Character>, Stream<Character>, ParseFailure> and(
      Parser<Stream<Character>, Stream<Character>, ParseFailure> one,
      Parser<Stream<Character>, Stream<Character>, ParseFailure>... rest) {
        return List.arrayList(rest).foldLeft((o, next) -> o.bind(val1 -> next.map(val1::append)), one);
    }

    private static <I, A, E> Parser<I, A, E> lazy(Supplier<Parser<I, A, E>> lazyParser) {
        return Parser.parser(i -> lazyParser.get().parse(i));
    }

    private static Parser<Stream<Character>, Stream<Character>, ParseFailure> on(String check, String output) {
        return string(check).map(s -> Stream.fromString(output));
    }

    private static Parser<Stream<Character>, Stream<Character>, ParseFailure> flatten(Parser<Stream<Character>, Stream<Stream<Character>>, ParseFailure> input) {
        return input.map(s -> s.bind(i -> i));
    }

    private static Parser<Stream<Character>, Stream<Character>, ParseFailure> trim(Parser<Stream<Character>, Stream<Character>, ParseFailure> parser) {
        return whitespaces.repeat().bind(parser, whitespaces.repeat(), w1 -> value -> w2 -> value);
    }


    // *** API ***

    /**
     * Parses a stream of charcters to a JsonValue
     *
     * @param json the stream
     * @return the Success(JsonValue) or Fail(String)
     */
    public static JsonResult<JsonValue> parse(Stream<Character> json) {
        return JsonResult.fromValidation(value().parse(json).f().map(ParseFailure::getMessage).map(Result::value));
    }

    public static JsonResult<JsonValue> parse(String json) {
        return parse(Stream.fromString(json));
    }

}
