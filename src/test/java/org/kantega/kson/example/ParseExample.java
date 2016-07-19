package org.kantega.kson.example;

import fj.F;
import fj.data.Validation;
import org.kantega.kson.json.JsonObject;
import org.kantega.kson.json.JsonValue;
import org.kantega.kson.parser.JsonParser;
import org.kantega.kson.parser.JsonWriter;

import static org.kantega.kson.json.JsonValues.*;

public class ParseExample {
  public static void main(String[] args) {

    final JsonObject json =
        jObj(
            field("name", jString("Ola Nordmann")),
            field("age", jNum(28)),
            field("favourites", jArray(jString("red"), jString("blue"), jString("purple")))
        );

    final String jsonString =
        JsonWriter.writePretty(json);

    System.out.println(jsonString);

    final Validation<String, JsonValue> parsedJsonV =
        JsonParser.parse(jsonString);

    final F<JsonValue, Validation<String, String>> getNameAndAge =
        obj ->
            getFieldAsText(obj, "name").bind(name -> getFieldAsText(obj, "age").map(age -> name + ", " + age)).toValidation("'name' or 'age' is missing");

    final String output =
        parsedJsonV.validation(
            failmsg -> failmsg,
            parsedJson -> getNameAndAge.f(parsedJson).validation(failmsg2 -> failmsg2, nameAndAge -> nameAndAge));

  }
}
