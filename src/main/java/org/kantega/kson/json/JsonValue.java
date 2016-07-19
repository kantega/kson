package org.kantega.kson.json;

import fj.Equal;
import fj.F;
import fj.F0;
import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;

import java.math.BigDecimal;

import static org.kantega.kson.json.JsonValue.Folder.*;

public abstract class JsonValue {

  public static Equal<JsonValue> eq() {
    return Equal.equal(one -> other ->
        one
            .fold(
                foldWith(false)
                    .onString(str1 -> other.fold(foldWith(false).onString(str1::equals)))
                    .onBool(bool1 -> other.fold(foldWith(false).onBool(bool1::equals)))
                    .onNumber(num1 -> other.fold(foldWith(false).onNumber(num1::equals)))
                    .onNull(() -> other.fold(foldWith(false).onNull(() -> true)))
                    .onObject(obj1 -> other.fold(foldWith(false).onObject(Equal.treeMapEqual(Equal.stringEqual, eq()).eq(obj1))))
                    .onArray(arr -> other.fold(foldWith(false).onArray(Equal.listEqual(eq()).eq(arr))))));
  }

  public <T> T fold(Fold<T> f){
    return f.on(this);
  }

  interface Fold<T> {
    <A extends JsonValue> T on(A value);
  }

  public static class Folder<T> implements Fold<T> {
    final T                          def;
    final List<F<Object, Option<T>>> funcs;

    Folder(T def, List<F<Object, Option<T>>> funcs) {
      this.def = def;
      this.funcs = funcs;
    }

    public static <T> Folder<T> foldWith(T t) {
      return new Folder<T>(t, List.nil());
    }

    <A extends JsonValue> Folder<T> match(Class<A> c, F<A, T> f) {
      return new Folder<>(def, funcs.cons(obj -> c.isInstance(obj)
          ? Option.some(f.f(c.cast(obj)))
          : Option.none()));
    }

    public Folder<T> onString(F<String, T> f) {
      return match(JsonString.class, jstr -> f.f(jstr.value));
    }

    public Folder<T> onNumber(F<BigDecimal, T> f) {
      return match(JsonNumber.class, jstr -> f.f(jstr.value));
    }

    public Folder<T> onArray(F<List<JsonValue>, T> f) {
      return match(JsonArray.class, jarr -> f.f(jarr.values));
    }

    public Folder<T> onObject(F<TreeMap<String, JsonValue>, T> f) {
      return match(JsonObject.class, jobj -> f.f(jobj.pairs));
    }

    public Folder<T> onNull(F0<T> f) {
      return match(JsonObject.class, jobj -> f.f());
    }

    public Folder<T> onBool(F<Boolean, T> f) {
      return match(JsonBool.class, jbool -> f.f(jbool.value));
    }

    @Override
    public <A extends JsonValue> T on(A value) {
      return funcs.foldLeft((maybeT, f) -> maybeT.orElse(f.f(value)), Option.<T>none()).orSome(def);
    }
  }


}
