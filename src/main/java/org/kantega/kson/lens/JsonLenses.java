package org.kantega.kson.lens;

import fj.data.Validation;
import org.kantega.kson.json.JsonObject;
import org.kantega.kson.json.JsonValue;

import static fj.data.Validation.*;

public class JsonLenses {

  public static JsonLens<JsonValue, JsonValue> field(String fieldName) {
    return new JsonLens<>(
        jVal ->
            jVal.fold(
                JsonValue.Fold
                    .foldWith(Validation.<String, JsonValue>fail("Not an object"))
                    .onObject(map ->
                        map.get(fieldName)
                            .option(
                                fail("No field with name " + fieldName),
                                Validation::success))),
        (a, origin) ->
            origin.fold(JsonValue.Fold.foldWith(Validation.<String, JsonValue>fail("Not an object"))
                .onObject(map -> success(new JsonObject(map.set(fieldName, a))))
            )
    );
  }
}
