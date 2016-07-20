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
        JsonLenses.select("address").select("zip").asString();


    JsonValue userjson =
        jObj(
            field("name", jString("Ole Normann")),
            field("address", jObj(
                field("street", jString("Kongensgate 3")),
                field("zip", jString("1234")) //Observe the presence of this field
            ))
        );

    JsonValue invalidUserJson =
        jObj(
            field("name", jString("Kari Normann")),
            field("address", jObj(
                field("street", jString("Kongensgate 3"))
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
        JsonLenses.select("model");

    JsonValueLens leaderLens =
        JsonLenses.select("leader");

    JsonValueLens usersLens =
        JsonLenses.select("users");

    //Replace leader
    JsonValue updatedLeaderModel =
        modelLens.then(leaderLens).setF(userModel, invalidUserJson);


    //Update zipcode of last user in array
    JsonValue modelWithZips =
        modelLens.then(usersLens).asArray().modF(updatedLeaderModel, list -> list.map(zipLens.setF("1234")));

    System.out.println(JsonWriter.writePretty(modelWithZips));
  }
}
