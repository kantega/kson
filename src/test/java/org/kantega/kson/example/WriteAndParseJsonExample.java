package org.kantega.kson.example;

import com.oracle.javafx.jmx.json.JSONReader;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.JsonValue;
import org.kantega.kson.parser.JsonParser;
import org.kantega.kson.parser.JsonWriter;

import static org.kantega.kson.json.JsonValues.field;
import static org.kantega.kson.json.JsonValues.jArray;
import static org.kantega.kson.json.JsonValues.jObj;

public class WriteAndParseJsonExample {


    public static void main(String[] args) {

        //Constructing
        JsonValue userjson =
          jObj(
            field("name", "Ole Normann"),
            field("address", jObj(
              field("street", "Kongensgate 3"),
              field("zip", "1234") //Observe the presence of this field
            ))
          );

        JsonValue userWithoutZipcode =
          jObj(
            field("name", "Kari Normann"),
            field("address", jObj(
              field("street", "Kongensgate 3")
              //Behold the missing zipcode field
            ))
          );

        //Values are reusable and can be nested.
        JsonValue team =
          jObj(
            field("team",
              jObj(
                field("members", jArray(userjson, userWithoutZipcode)),
                field("name", "A-team")
              )));


        //Write to a string
        String jsonAsString =
          JsonWriter.writePretty(team);

        //Parse a string
        JsonResult<JsonValue> json =
          JsonParser.parse(jsonAsString);


        //Find the zipcode of the first user
        String zip1 =
          json.field("team").field("members").index(0).field("address").fieldAsString("zip", "unknown");
        //Zip 1 is "1234"

        //If a field is not present, or a conversion fails, the navigations aborts, and the default value is used.
        String zip2 =
          json.field("team").field("members").index(1).field("address").fieldAsString("zip", "unknown");
        //zip2 is "unknown" since the field is not present.


        //You can also keep the JsonResult directly
        JsonResult<JsonValue> failedResult =
          json.field("temas"); //Yields a failed JsonResult

        JsonResult<JsonValue> teamsResult =
          json.field("team"); //Yields a failed JsonResult


    }
}
