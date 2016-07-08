package org.kantega.kson.parser;

import fj.P;
import fj.P2;
import fj.data.List;
import fj.data.Stream;
import fj.parser.Parser;
import org.kantega.kson.json.JsonArray;
import org.kantega.kson.json.JsonNumber;
import org.kantega.kson.json.JsonString;
import org.kantega.kson.json.JsonValue;

import java.math.BigDecimal;

import static fj.parser.Parser.CharsParser.character;
import static fj.parser.Parser.CharsParser.characters;
import static org.kantega.kson.json.JsonObject.JsonObject;

public class JsonParser {

  static Exception f(String msg) {
    return new Exception(msg);
  }

  static final Exception missingInput = f("Missing input");

  static final Parser<Stream<Character>, Stream<Character>, Exception> comma =
      singleChar(',');

  static final Parser<Stream<Character>, Stream<Character>, Exception> lCurly =
      singleChar('{');

  static final Parser<Stream<Character>, Stream<Character>, Exception> rCurly =
      singleChar('}');

  static final Parser<Stream<Character>, Stream<Character>, Exception> lBrace =
      singleChar('[');

  static final Parser<Stream<Character>, Stream<Character>, Exception> rBrace =
      singleChar(']');

  static final Parser<Stream<Character>, Stream<Character>, Exception> dash =
      singleChar('-');

  static final Parser<Stream<Character>, Stream<Character>, Exception> quot =
      singleChar('"');

  static final Parser<Stream<Character>, Stream<Character>, Exception> point =
      singleChar('.');

  static final Parser<Stream<Character>, Stream<Character>, Exception> colon =
      singleChar(':');

  static final Parser<Stream<Character>, Stream<Character>, Exception> e =
      singleChar('e').or(singleChar('E')).or(sequence("e+")).or(sequence("e-")).or(sequence("E+")).or(sequence("E-"));

  static final Parser<Stream<Character>, Character, Exception> digit =
      Parser.StreamParser.satisfy(missingInput, (i) -> f(i + " is not a digit"), Character::isDigit);

  static final Parser<Stream<Character>, Stream<Character>, Exception> digits =
      digit.repeat1();

  static final Parser<Stream<Character>, Stream<Character>, Exception> exp =
      and(e, digits);

  static final Parser<Stream<Character>, Stream<Character>, Exception> frac =
      and(point, digits);

  static final Parser<Stream<Character>, Stream<Character>, Exception> intt =
      and(digit.map(Stream::single), digits)
          .or(and(dash, digits));

  static final Parser<Stream<Character>, JsonValue, Exception> numberValue =
      intt
          .or(and(intt, frac))
          .or(and(intt, exp))
          .or(and(intt, frac, exp))
          .map(cs -> new JsonNumber(new BigDecimal(Stream.asString(cs))));

  static final Parser<Stream<Character>, Character, Exception> charr =
      Parser.StreamParser.satisfy(missingInput, (i) -> f(i + " is not a letter"), x -> x != '"' && x != '\\');

  static final Parser<Stream<Character>, Stream<Character>, Exception> chars =
      charr.repeat1();

  static final Parser<Stream<Character>, String, Exception> string =
      quot.bind(chars, quot, q1 -> cs -> q2 -> Stream.asString(cs));

  static final Parser<Stream<Character>, JsonValue, Exception> stringValue =
      string.map(JsonString::new);


  static Parser<Stream<Character>, JsonValue, Exception> value() {
    return numberValue.or(stringValue).or(arrayValue()).or(numberValue).or(object());
  }

  static Parser<Stream<Character>, JsonValue, Exception> arrayValue() {
    return
        lBrace.bind(value(), comma.sequence(value()).repeat(), rBrace,
            lb -> first -> rest -> rb -> new JsonArray(rest.cons(first).toList()));
  }

  static final Parser<Stream<Character>, P2<String, JsonValue>, Exception> pair(){
    return string.bind(colon, value(), str -> c -> val -> P.p(str, val));
  }


  static Parser<Stream<Character>, JsonValue, Exception> object() {
    return
        lCurly.bind(pair(),comma.sequence(pair()).repeat(),rCurly,lc->first->rest->rc-> JsonObject(rest.cons(first).toList()));
  }


  static Parser<Stream<Character>, Stream<Character>, Exception> singleChar(Character c) {
    return character(missingInput, (i) -> f(i + " is not a '" + c.toString() + "'"), c).map(Stream::single);
  }

  static Parser<Stream<Character>, Stream<Character>, Exception> sequence(String chars) {
    return characters(missingInput, (i) -> f(i + " is not a '" + chars + "'"), Stream.fromString(chars));
  }


  static Parser<Stream<Character>, Stream<Character>, Exception> and(
      Parser<Stream<Character>, Stream<Character>, Exception> one,
      Parser<Stream<Character>, Stream<Character>, Exception>... rest) {
    return List.arrayList(rest).foldLeft((o, next) -> o.bind(val1 -> next.map(val1::append)), one);

  }


}
