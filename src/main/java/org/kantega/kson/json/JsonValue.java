package org.kantega.kson.json;

import fj.Equal;
import fj.F;
import fj.F0;
import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;
import org.kantega.kson.JsonResult;

import java.math.BigDecimal;

import static fj.data.List.nil;
import static fj.data.Option.none;
import static org.kantega.kson.JsonResult.fail;

public abstract class JsonValue {

  public static Equal<JsonValue> eq() {
    return Equal.equal(one -> other ->
        one
            .onString(str1 -> other.onString(str1::equals).orElse(false))
            .onBool(bool1 -> other.onBool(bool1::equals).orElse(false))
            .onNumber(num1 -> other.onNumber(num1::equals).orElse(false))
            .onNull(() -> other.onNull(() -> true).orElse(false))
            .onObject(obj1 -> other.onObject(Equal.treeMapEqual(Equal.stringEqual, eq()).eq(obj1)).orElse(false))
            .onArray(arr -> other.onArray(Equal.listEqual(eq()).eq(arr)).orElse(false))
            .orElse(false)
    );
  }

  public Option<String> asTextO() {
    return onString(Option::some).orElse(none());
  }

  public JsonResult<String> asText() {
    return onString(JsonResult::success).orElse(fail("Not a string"));
  }

  public Option<BigDecimal> asNumberO() {
    return onNumber(Option::some).orElse(none());
  }

  public JsonResult<BigDecimal> asNumber() {
    return onNumber(JsonResult::success).orElse(fail("Not a number"));
  }

  public Option<Boolean> asBoolO() {
    return onBool(Option::some).orElse(none());
  }

  public JsonResult<Boolean> asBool() {
    return onBool(JsonResult::success).orElse(fail("Not a bool"));
  }

  public Option<JsonValue> getField(String field) {
    return onObject(m -> m.get(field)).orElse(none());
  }

  public Option<JsonValue> setField(String name, JsonValue value) {
    return onObject(map -> Option.some(JsonValues.jObj(map.set(name, value)))).orElse(none());
  }

  public Option<String> getFieldAsText(String field) {
    return getField(field).bind(JsonValue::asTextO);
  }

  public Option<BigDecimal> getFieldAsNumber(String field) {
    return getField(field).bind(JsonValue::asNumberO);
  }

  public Option<Boolean> getFieldAsBool(String field) {
    return getField(field).bind(JsonValue::asBoolO);
  }

  public <T> Fold<T> onString(F<String, T> f) {
    return Fold.<T>newFold(this).onString(f);
  }

  public <T> Fold<T> onNumber(F<BigDecimal, T> f) {
    return Fold.<T>newFold(this).onNumber(f);
  }

  public <T> Fold<T> onArray(F<List<JsonValue>, T> f) {
    return Fold.<T>newFold(this).onArray(f);
  }

  public <T> Fold<T> onObject(F<TreeMap<String, JsonValue>, T> f) {
    return Fold.<T>newFold(this).onObject(f);
  }

  public <T> Fold<T> onNull(F0<T> f) {
    return Fold.<T>newFold(this).onNull(f);
  }

  public <T> Fold<T> onBool(F<Boolean, T> f) {
    return Fold.<T>newFold(this).onBool(f);
  }


  public static class Fold<T> {
    final JsonValue                  value;
    final List<F<Object, Option<T>>> funcs;

    Fold(JsonValue value, List<F<Object, Option<T>>> funcs) {
      this.value = value;
      this.funcs = funcs;
    }

    static <T> Fold<T> newFold(JsonValue value) {
      return new Fold<>(value, nil());
    }

    <A extends JsonValue> Fold<T> match(Class<A> c, F<A, T> f) {
      return new Fold<>(value, funcs.cons(obj -> c.isInstance(obj)
          ? Option.some(f.f(c.cast(obj)))
          : Option.none()));
    }

    public Fold<T> onString(F<String, T> f) {
      return match(JsonString.class, jstr -> f.f(jstr.value));
    }

    public Fold<T> onNumber(F<BigDecimal, T> f) {
      return match(JsonNumber.class, jstr -> f.f(jstr.value));
    }

    public Fold<T> onArray(F<List<JsonValue>, T> f) {
      return match(JsonArray.class, jarr -> f.f(jarr.values));
    }

    public Fold<T> onObject(F<TreeMap<String, JsonValue>, T> f) {
      return match(JsonObject.class, jobj -> f.f(jobj.pairs));
    }

    public Fold<T> onNull(F0<T> f) {
      return match(JsonObject.class, jobj -> f.f());
    }

    public Fold<T> onBool(F<Boolean, T> f) {
      return match(JsonBool.class, jbool -> f.f(jbool.value));
    }

    public T orElse(T defaultValue) {
      return funcs.foldLeft((maybeT, f) -> maybeT.orElse(f.f(value)), Option.<T>none()).orSome(defaultValue);
    }
  }


}
