package org.kantega.kson.example;

import fj.F;
import fj.P;
import fj.P2;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.JsonValue;
import org.kantega.kson.parser.JsonParser;
import org.kantega.kson.parser.JsonWriter;

import java.math.BigDecimal;

import static org.kantega.kson.json.JsonValues.*;

public class BindExample {
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

        //Binding two get operations together, yielding either a valid pair of values, or a failure
        //You will never have jsut one valid value when you bind: If one operation fails,
        //everything becomes a failure
        final F<JsonValue, JsonResult<P2<String, BigDecimal>>> getNameAndAge =
          obj ->
            obj.fieldAsText("name")
              .bind(name ->
                obj
                  .fieldAsNumber("age")
                  .map(age -> P.p(name, age)))
              .fold(f -> JsonResult.fail("'name' or 'age' is missing"), JsonResult::success);

        final String output =
          parsedJsonV.fold(
            failmsg -> failmsg,
            parsedJson -> getNameAndAge.f(parsedJson).fold(failmsg2 -> failmsg2, nameAndAge -> nameAndAge._1() + "," + nameAndAge._2()));

        System.out.println(output);

    }
}
