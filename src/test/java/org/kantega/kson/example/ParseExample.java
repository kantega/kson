package org.kantega.kson.example;

import fj.F;
import fj.P;
import fj.P2;
import fj.data.Validation;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.JsonObject;
import org.kantega.kson.json.JsonValue;
import org.kantega.kson.parser.JsonParser;
import org.kantega.kson.parser.JsonWriter;

import java.math.BigDecimal;

import static org.kantega.kson.json.JsonValues.*;

public class ParseExample {
  public static void main(String[] args) {

    final JsonValue json =
        jObj(
            field("name", jString("Ola Nordmann")),
            field("age", jNum(28)),
            field("favourites", jArray(jString("red"), jString("blue"), jString("purple")))
        );

    final String jsonString =
        JsonWriter.writePretty(json);

    final JsonResult<JsonValue> parsedJsonV =
        JsonParser.parse(jsonString);

    final F<JsonValue, JsonResult<P2<String,BigDecimal>>> getNameAndAge =
        obj ->
            obj.getFieldAsText("name").bind(name -> obj.getFieldAsNumber("age").map(age -> P.p(name, age))).fold(f->JsonResult.fail("'name' or 'age' is missing"), JsonResult::success);

    final String output =
        parsedJsonV.fold(
            failmsg -> failmsg,
            parsedJson -> getNameAndAge.f(parsedJson).fold(failmsg2 -> failmsg2, nameAndAge -> nameAndAge._1()+","+nameAndAge._2()));

    System.out.println(output);

  }
}
