package org.kantega.kson.lens;

import fj.F;
import fj.F2;
import fj.data.List;
import fj.data.Option;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.*;

import java.math.BigDecimal;

import static fj.data.Option.some;
import static org.kantega.kson.JsonResult.fail;
import static org.kantega.kson.JsonResult.success;

public class JsonValueLens extends JsonLens<JsonValue, JsonValue> {


  public JsonValueLens(
      F<JsonValue, JsonResult<JsonValue>> get,
      F2<JsonValue, JsonValue, JsonResult<JsonValue>> set) {
    super(get, set);
  }

  public <B> JsonValueLens then(JsonValueLens other) {
    return new JsonValueLens(
        s -> get(s).bind(other::get),
        (b, s) -> mod(s, a -> other.set(a, b))
    );
  }

  public JsonValueLens select(String fieldName) {
    return then(JsonLenses.field(fieldName));
  }

  public JsonLens<JsonValue, String> asString() {
    return
        xmap(
            JsonValue::asText,
            str -> success(new JsonString(str))
        );
  }

  public JsonLens<JsonValue, BigDecimal> asNumber() {
    return
        xmap(
            JsonValue::asNumber,
            n -> success(new JsonNumber(n.toString()))
        );
  }

  public JsonLens<JsonValue, Boolean> asBool() {
    return
        xmap(
            JsonValue::asBool,
            n -> success(new JsonBool(n))
        );
  }

  public JsonLens<JsonValue, Option<JsonValue>> asNullable() {
    return
        xmap(
            a -> a.onNull(() -> success(Option.<JsonValue>none())).orSome(success(some(a))),
            n -> n.option(success(JsonValues.jNull()), JsonResult::success)
        );
  }

  public JsonLens<JsonValue, JsonObject> asObject() {
    return
        xmap(
            a -> a.onObject(map -> JsonResult.success(new JsonObject(map))).orSome(fail("Not an object")),
            JsonResult::success
        );
  }

  public JsonLens<JsonValue, List<JsonValue>> asArray() {
    return
        xmap(
            a -> a.onArray(JsonResult::success).orSome(fail("Not an object")),
            list-> JsonResult.success(JsonValues.jArray(list))
        );
  }

}
