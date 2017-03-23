package org.kantega.kson.lens;

import fj.F;
import fj.F2;
import fj.data.List;
import fj.data.TreeMap;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.JsonValue;

import static org.kantega.kson.json.JsonValues.jArray;
import static org.kantega.kson.json.JsonValues.jObj;
import static org.kantega.kson.JsonResult.success;

public class JsonLenses {

  public static JsonValueLens selfLens(){
    return new JsonValueLens(
      JsonResult::success,
      (a, origin) -> JsonResult.success(a)
    );
  }

  public static JsonValueLens objLens(
      F<TreeMap<String, JsonValue>, JsonResult<JsonValue>> get,
      F2<JsonValue, TreeMap<String, JsonValue>, TreeMap<String, JsonValue>> set) {
    return new JsonValueLens(
        jVal ->
            jVal.onObject(get).orElse(fail("Not an object")),
        (a, origin) ->
            origin.onObject(map -> success(jObj(set.f(a, map)))).orElse(fail("Not an object"))
    );
  }

  public static JsonValueLens arrayLens(
      F<List<JsonValue>, JsonResult<JsonValue>> get,
      F2<JsonValue, List<JsonValue>, List<JsonValue>> set) {
    return new JsonValueLens(
        jVal ->
            jVal.onArray(get).orElse(fail("Not an array")),
        (a, origin) ->
            origin
                .onArray(list -> success(jArray(set.f(a, list))))
                .orElse(fail("Not an array"))

    );
  }

  public static JsonValueLens select(String fieldName) {
    return objLens(
        map -> map.get(fieldName).option(JsonResult.fail("No field with name " + fieldName + " in object"), JsonResult::success),
        (a, map) -> map.set(fieldName, a)
    );
  }


  private static <A> JsonResult<A> fail(String failmsg) {
    return JsonResult.fail(failmsg);
  }


}
