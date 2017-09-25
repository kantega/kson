package org.kantega.kson;

import fj.F;
import fj.F0;
import fj.data.List;
import fj.data.Option;
import fj.data.Validation;
import org.kantega.kson.codec.JsonDecoder;
import org.kantega.kson.json.JsonValue;

import java.math.BigDecimal;

public class JsonResult<A> {

    final Validation<String, A> validation;

    private JsonResult(Validation<String, A> validation) {
        this.validation = validation;
    }

    public static <A> JsonResult<A> fail(String msg) {
        return new JsonResult<>(Validation.fail(msg));
    }

    public static <A> JsonResult<A> success(A a) {
        return new JsonResult<>(Validation.success(a));
    }

    public static <A> JsonResult<A> tried(F0<A> a) {
        try {
            return new JsonResult<>(Validation.success(a.f()));
        } catch (Exception e) {
            return JsonResult.fail(e.getClass().getSimpleName() + ":" + e.getMessage());
        }
    }

    public <B> JsonResult<B> decode(JsonDecoder<B> decoder) {
        return onJsonValue(decoder);
    }

    public static <A> JsonResult<A> fromValidation(Validation<String, A> validation) {
        return new JsonResult<>(validation);
    }

    public JsonResult<JsonValue> field(String field) {
        return onJsonValue(json ->
          json.field(field));
    }

    public JsonResult<JsonValue> index(int i) {
        return asArray().bind(list -> JsonResult.tried(() -> (list.toArray().get(i))));
    }

    public ArrayResult<JsonValue> asArray() {
        return new ArrayResult<>(onJsonValue(jsonValue -> jsonValue.onArray(JsonResult::success).orSome(JsonResult.fail("Not an array"))).validation);
    }

    public ArrayResult<JsonValue> fieldAsArray(String field) {
        return field(field).asArray();
    }

    public JsonResult<List<String>> asStrings() {
        return asArray().bind(list -> sequence(list.map(JsonValue::asText)));
    }

    public JsonResult<List<String>> fieldAsStrings(String field) {
        return field(field).asStrings();
    }

    public JsonResult<List<BigDecimal>> asNumbers() {
        return asArray().bind(list -> sequence(list.map(JsonValue::asNumber)));
    }

    public JsonResult<List<BigDecimal>> fieldAsNumbers(String field) {
        return field(field).asNumbers();
    }

    public JsonResult<List<Boolean>> asBools() {
        return asArray().bind(list -> sequence(list.map(JsonValue::asBool)));
    }

    public JsonResult<List<Boolean>> fieldAsBools(String field) {
        return field(field).asBools();
    }

    public JsonResult<String> indexAsString(int i) {
        return index(i).asString();
    }

    public JsonResult<BigDecimal> indexAsNumber(int i) {
        return index(i).asNumber();
    }

    public JsonResult<Boolean> indexAsBool(int i) {
        return index(i).asBoolean();
    }

    public String indexAsString(int i, String defaultValue) {
        return indexAsString(i).orElse(() -> defaultValue);
    }

    public BigDecimal indexAsNumber(int i, BigDecimal defaultValue) {
        return indexAsNumber(i).orElse(() -> defaultValue);
    }

    public Boolean indexAsBool(int i, Boolean defaultValue) {
        return indexAsBool(i).orElse(() -> defaultValue);
    }

    public JsonResult<String> fieldAsString(String field) {
        return field(field).asString();
    }

    public JsonResult<BigDecimal> fieldAsNumber(String field) {
        return field(field).asNumber();
    }

    public JsonResult<Boolean> fieldAsBool(String field) {
        return field(field).asBoolean();
    }

    public String fieldAsString(String field, String defaultValue) {
        return field(field).asString().orElse(() -> defaultValue);
    }

    public JsonResult<String> asString() {
        return onJsonValue(JsonValue::asText);
    }

    public JsonResult<BigDecimal> asNumber() {
        return onJsonValue(JsonValue::asNumber);
    }

    public JsonResult<Boolean> asBoolean() {
        return onJsonValue(JsonValue::asBool);
    }

    public String asStringE() {
        return asString().orThrow(JsonConversionFailure::new);
    }

    public BigDecimal asNumberE() {
        return asNumber().orThrow(JsonConversionFailure::new);
    }

    public Boolean asBooleanE() {
        return asBoolean().orThrow(JsonConversionFailure::new);
    }


    public String asString(String defaultValue) {
        return asString().orElse(() -> defaultValue);
    }

    public BigDecimal asNumber(BigDecimal defaultNumber){
        return asNumber().orElse(()->defaultNumber);
    }

    public Boolean asBoolean(boolean defaultBoolean){
        return asBoolean().orElse(()->defaultBoolean);
    }


    public <A> JsonResult<A> onJsonValue(F<JsonValue, JsonResult<A>> f) {
        return validation.validation(
          fail -> JsonResult.fail(fail),
          s -> {
              if (s instanceof JsonValue) {
                  return f.f((JsonValue) s);
              } else
                  return JsonResult.fail("Not a json value");
          }
        );
    }

    /**
     * Returns a validation that either contains a value or a failmessage.
     * @return a validation.
     */
    public Validation<String, A> toValidation() {
        return validation;
    }

    /**
     * Returns Some(a) if the JsonResult contains a value, None otherwise.
     * @return An Option
     */
    public Option<A> toOption() {
        return toValidation().toOption();
    }

    public static <A> JsonResult<List<A>> sequence(List<JsonResult<A>> results) {
        return results.foldLeft(
          (memo, result) -> result.bind(a -> memo.map(list -> list.cons(a))),
          JsonResult.success(List.<A>nil())).map(List::reverse);
    }

    public <B> JsonResult<B> mod(F<Validation<String, A>, Validation<String, B>> f) {
        return new JsonResult<>(f.f(validation));
    }

    public <T> T fold(F<String, T> onError, F<A, T> onSuccess) {
        return validation.validation(onError, onSuccess);
    }

    public JsonResult<A> mapFail(F<String, String> f) {
        return mod(validation -> validation.f().map(f));
    }

    public <B> JsonResult<B> map(F<A, B> f) {
        return mod(v -> v.map(f));
    }

    public <B> JsonResult<B> bind(F<A, JsonResult<B>> f) {
        return mod(v -> v.map(f).bind(v2 -> v2.validation));
    }

    public A orElse(F0<A> a) {
        return validation.validation(f -> a.f(), aa -> aa);
    }

    public A orElse(F<String, A> a) {
        return validation.validation(a::f, aa -> aa);
    }

    public JsonResult<A> orResult(F0<JsonResult<A>> other) {
        return bind(u -> other.f());
    }

    public A orThrow() {
        return orThrow(RuntimeException::new);
    }

    public A orThrow(F<String, ? extends RuntimeException> supplier) {
        if (validation.isFail())
            throw supplier.f(validation.fail());
        else return validation.success();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("JsonResult{");
        sb.append(validation.validation(f -> f, Object::toString));
        sb.append('}');
        return sb.toString();
    }

    public static class ArrayResult<A> extends JsonResult<List<A>> {

        public ArrayResult(Validation<String, List<A>> validation) {
            super(validation);
        }

        public <B> ArrayResult<B> mapArray(F<A, B> f) {
            return new ArrayResult<B>(validation.map(list -> list.map(f)));
        }

        public <B> ArrayResult<B> mapFlattenArray(F<A, JsonResult<B>> f) {
            return new ArrayResult<B>(validation.bind(list -> JsonResult.sequence(list.map(f)).validation));

        }
    }
}
