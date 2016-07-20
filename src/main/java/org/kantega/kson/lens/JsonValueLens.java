package org.kantega.kson.lens;

import fj.F;
import fj.F2;
import fj.data.List;
import fj.data.Option;
import org.kantega.kson.json.*;

import java.math.BigDecimal;

import static fj.data.Option.some;
import static org.kantega.kson.lens.LensResult.fail;
import static org.kantega.kson.lens.LensResult.success;

public class JsonValueLens extends JsonLens<JsonValue, JsonValue> {


  public JsonValueLens(
      F<JsonValue, LensResult<JsonValue>> get,
      F2<JsonValue, JsonValue, LensResult<JsonValue>> set) {
    super(get, set);
  }

  /**
   * Compose two lenses together
   *
   * @param other
   * @param <B>
   * @return
   */
  public <B> JsonValueLens then(JsonValueLens other) {
    return new JsonValueLens(
        s -> get(s).bind(other::get),
        (b, s) -> mod(s, a -> other.set(a, b))
    );
  }

  public JsonValueLens select(String fieldName) {
    return then(JsonLenses.select(fieldName));
  }

  public JsonLens<JsonValue, String> asString() {
    return
        xmap(
            a -> a.onString(LensResult::success).orElse(fail("Not a string")),
            str -> success(new JsonString(str))
        );
  }

  public JsonLens<JsonValue, BigDecimal> asNumber() {
    return
        xmap(
            a -> a.onNumber(LensResult::success).orElse(fail("Not a number")),
            n -> success(new JsonNumber(n))
        );
  }

  public JsonLens<JsonValue, Boolean> asBool() {
    return
        xmap(
            a -> a.onBool(LensResult::success).orElse(fail("Not a number")),
            n -> success(new JsonBool(n))
        );
  }

  public JsonLens<JsonValue, Option<JsonValue>> asNullable() {
    return
        xmap(
            a -> a.onNull(() -> success(Option.<JsonValue>none())).orElse(success(some(a))),
            n -> n.option(success(JsonValues.jNull()), LensResult::success)
        );
  }

  public JsonLens<JsonValue, JsonObject> asObject() {
    return
        xmap(
            a -> a.onObject(map -> LensResult.success(new JsonObject(map))).orElse(fail("Not an object")),
            LensResult::success
        );
  }

  public JsonLens<JsonValue, List<JsonValue>> asArray() {
    return
        xmap(
            a -> a.onArray(list -> LensResult.success(list)).orElse(fail("Not an object")),
            list->LensResult.success(JsonValues.jArray(list))
        );
  }

}
