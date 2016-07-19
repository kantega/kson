package org.kantega.kson.parser;

import fj.P;
import fj.P2;
import fj.data.List;
import fj.data.Stream;
import fj.data.Validation;
import fj.parser.Parser;
import fj.parser.Result;
import org.kantega.kson.json.*;

import java.math.BigDecimal;
import java.util.function.Supplier;

import static fj.parser.Parser.CharsParser.character;
import static fj.parser.Parser.CharsParser.characters;
import static org.kantega.kson.json.JsonObject.JsonObject;

public class JsonParser {

  static Exception f(String msg) {
    return new Exception(msg);
  }

  private static final Exception missingInput =
      f("Missing input");

  // *** Tokens ***

  private static final Parser<Stream<Character>, Stream<Character>, Exception> space =
      singleChar(' ');

  private static final Parser<Stream<Character>, Stream<Character>, Exception> nl =
      sequence("\\n");

  private static final Parser<Stream<Character>, Stream<Character>, Exception> tab =
      on("\\t", "\t");

  private static final Parser<Stream<Character>, Stream<Character>, Exception> quote =
      on("\\\"", "\"");

  private static final Parser<Stream<Character>, Stream<Character>, Exception> formfeed =
      on("\\f", "\f");

  private static final Parser<Stream<Character>, Stream<Character>, Exception> reverseSolidus =
      on("\\\\", "\\");

  private static final Parser<Stream<Character>, Stream<Character>, Exception> solidus =
      on("\\/", "/");

  private static final Parser<Stream<Character>, Character, Exception> hex =
      Parser.StreamParser.satisfy(missingInput, (i) -> f(i + " is not a hexadecimal character"), ch -> Character.isDigit(ch) || "abcde".contains(ch.toString().toLowerCase()));

  private static final Parser<Stream<Character>, Stream<Character>, Exception> control =
      singleChar('u').bind(u -> hex.bind(h1 -> hex.bind(h2 -> hex.bind(h3 -> hex.map(h4 -> u.append(Stream.arrayStream(h1, h2, h3, h4)))))));

  private static final Parser<Stream<Character>, Stream<Character>, Exception> special =
      tab.or(nl).or(quote).or(formfeed).or(reverseSolidus).or(solidus).or(control);

  private static final Parser<Stream<Character>, Stream<Character>, Exception> whitespaces =
      space.or(singleChar('\n')).or(singleChar('\t'));

  private static final Parser<Stream<Character>, Stream<Character>, Exception> comma =
      singleChar(',');

  private static final Parser<Stream<Character>, Stream<Character>, Exception> lCurly =
      singleChar('{');

  private static final Parser<Stream<Character>, Stream<Character>, Exception> rCurly =
      singleChar('}');

  private static final Parser<Stream<Character>, Stream<Character>, Exception> lBracket =
      singleChar('[');

  private static final Parser<Stream<Character>, Stream<Character>, Exception> rBracket =
      singleChar(']');

  private static final Parser<Stream<Character>, Stream<Character>, Exception> dash =
      singleChar('-');

  private static final Parser<Stream<Character>, Stream<Character>, Exception> quot =
      singleChar('"');

  private static final Parser<Stream<Character>, Stream<Character>, Exception> point =
      singleChar('.');

  private static final Parser<Stream<Character>, Stream<Character>, Exception> colon =
      singleChar(':');

  private static final Parser<Stream<Character>, Stream<Character>, Exception> e =
      singleChar('e').or(singleChar('E')).or(sequence("e+")).or(sequence("e-")).or(sequence("E+")).or(sequence("E-"));

  private static final Parser<Stream<Character>, Character, Exception> digit =
      Parser.StreamParser.satisfy(missingInput, (i) -> f(i + " is not a digit"), Character::isDigit);

  private static final Parser<Stream<Character>, Stream<Character>, Exception> digits =
      digit.repeat1();

  private static final Parser<Stream<Character>, Stream<Character>, Exception> exp =
      and(e, digits);

  private static final Parser<Stream<Character>, Stream<Character>, Exception> frac =
      and(point, digits);

  private static final Parser<Stream<Character>, Stream<Character>, Exception> intt =
      digits
          .or(and(dash, digits));


  private static final Parser<Stream<Character>, Stream<Character>, Exception> number =
      intt
          .or(and(intt, frac))
          .or(and(intt, exp))
          .or(and(intt, frac, exp));


  private static final Parser<Stream<Character>, Character, Exception> charr =
      Parser.StreamParser.satisfy(missingInput, (i) -> f(i + " is not a letter"), x -> x != '"' && x != '\\');

  private static final Parser<Stream<Character>, Stream<Character>, Exception> chars =
      flatten(charr.map(Stream::single).or(special).repeat());

  private static final Parser<Stream<Character>, String, Exception> string =
      quot.bind(chars, quot, q1 -> cs -> q2 -> Stream.asString(cs));

  // *** Parser ***

  public static final Parser<Stream<Character>, JsonValue, Exception> numberValue =
      number
          .map(cs -> new JsonNumber(new BigDecimal(Stream.asString(cs))));

  private static final Parser<Stream<Character>, JsonValue, Exception> stringValue =
      string.map(JsonString::new);

  private static final Parser<Stream<Character>, JsonValue, Exception> boolValue =
      string("true").map(s -> (JsonValue) new JsonBool(true)).or(string("false").map(s -> new JsonBool(false)));

  private static final Parser<Stream<Character>, JsonValue, Exception> nullValue =
      string("null").map(s -> (JsonValue) new JsonNull());

  private static Parser<Stream<Character>, JsonValue, Exception> value() {
    return lazy(() -> nullValue.or(boolValue).or(numberValue).or(stringValue).or(arrayValue()).or(object()));
  }

  private static Parser<Stream<Character>, JsonValue, Exception> emptyArray() {
    return and(trim(lBracket), trim(rBracket)).map(s -> (JsonValue) new JsonArray(List.nil()));
  }

  private static Parser<Stream<Character>, JsonValue, Exception> nonEmptyArray() {
    return trim(lBracket).bind(value(), trim(comma).sequence(value()).repeat(), trim(rBracket),
        lb -> first -> rest -> rb -> new JsonArray(rest.cons(first).toList()));
  }

  private static Parser<Stream<Character>, JsonValue, Exception> arrayValue() {
    return emptyArray().or(nonEmptyArray());
  }

  private static Parser<Stream<Character>, P2<String, JsonValue>, Exception> pair() {
    return string.bind(trim(colon), value(), str -> c -> val -> P.<String, JsonValue>p(str, val));
  }

  private static Parser<Stream<Character>, JsonValue, Exception> object() {
    return
        trim(lCurly).bind(pair(), trim(comma).sequence(pair()).repeat(), trim(rCurly), lc -> first -> rest -> rc -> JsonObject(rest.cons(first).toList()));
  }

  // *** Helpers ***

  private static Parser<Stream<Character>, Stream<Character>, Exception> singleChar(Character c) {
    return character(missingInput, (i) -> f("'" + i + "' is not a '" + c.toString() + "'"), c).map(Stream::single);
  }

  private static Parser<Stream<Character>, Stream<Character>, Exception> sequence(String chars) {
    return characters(missingInput, (i) -> f("'" + i + "' is not a '" + chars + "'"), Stream.fromString(chars));
  }

  private static Parser<Stream<Character>, String, Exception> string(String chars) {
    return sequence(chars).map(Stream::asString);
  }

  @SafeVarargs
  private static Parser<Stream<Character>, Stream<Character>, Exception> and(
      Parser<Stream<Character>, Stream<Character>, Exception> one,
      Parser<Stream<Character>, Stream<Character>, Exception>... rest) {
    return List.arrayList(rest).foldLeft((o, next) -> o.bind(val1 -> next.map(val1::append)), one);
  }

  private static <I, A, E> Parser<I, A, E> lazy(Supplier<Parser<I, A, E>> lazyParser) {
    return Parser.parser(i -> lazyParser.get().parse(i));
  }

  private static Parser<Stream<Character>, Stream<Character>, Exception> on(String check, String output) {
    return string(check).map(s -> Stream.fromString(output));
  }

  private static Parser<Stream<Character>, Stream<Character>, Exception> flatten(Parser<Stream<Character>, Stream<Stream<Character>>, Exception> input){
    return input.map(s->s.bind(i->i));
  }

  private static Parser<Stream<Character>, Stream<Character>, Exception> trim(Parser<Stream<Character>, Stream<Character>, Exception> parser){
    return whitespaces.repeat().bind(parser,whitespaces.repeat(),w1->value->w2->value);
  }

  // *** API ***

  /**
   * Parses a stream of charcters to a JsonValue
   *
   * @param json the stream
   * @return the Success(JsonValue) or Fail(String)
   */
  public static Validation<String, JsonValue> parse(Stream<Character> json) {
    return value().parse(json).f().map(Throwable::getMessage).map(Result::value);
  }

  public static Validation<String, JsonValue> parse(String json) {
    return parse(Stream.fromString(json));
  }

}
