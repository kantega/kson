package org.kantega.kson.example;

import org.kantega.kson.json.JsonValue;
import org.kantega.kson.lens.JsonLens;
import org.kantega.kson.lens.JsonLenses;
import org.kantega.kson.lens.JsonValueLens;
import org.kantega.kson.parser.JsonWriter;

import static org.kantega.kson.json.JsonValues.*;

public class LensExample {

  public static void main(String[] args) {

    JsonLens<JsonValue, String> zipLens =
        JsonLenses.field("address").select("zip").asString();


    JsonValue userjson =
        jObj(
            field("name", "Ole Normann"),
            field("address", jObj(
                field("street", "Kongensgate 3"),
                field("zip", "1234") //Observe the presence of this field
            ))
        );

    JsonValue invalidUserJson =
        jObj(
            field("name", "Kari Normann"),
            field("address", jObj(
                field("street", "Kongensgate 3")
                //Behold the missing zipcode field
            ))
        );

    JsonValue userModel =
        jObj(
            field("model",
                jObj(
                    field("users", jArray(userjson, invalidUserJson)),
                    field("leader", userjson)
                )));


    JsonValueLens modelLens =
        JsonLenses.field("model");

    JsonValueLens leaderLens =
        JsonLenses.field("leader");

    JsonValueLens usersLens =
        JsonLenses.field("users");

    //Replace leader
    JsonValue updatedLeaderModel =
        modelLens.then(leaderLens).setF(userModel, invalidUserJson);


    //Update zipcode of last user in array
    JsonValue modelWithZips =
        modelLens.then(usersLens).asArray().modF(updatedLeaderModel, list -> list.map(zipLens.setF("1234")));

    System.out.println(JsonWriter.writePretty(modelWithZips));
  }
}
