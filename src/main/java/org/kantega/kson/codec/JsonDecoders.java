package org.kantega.kson.codec;

import fj.*;
import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.JsonValue;
import org.kantega.kson.util.Products;

import java.math.BigDecimal;

import static fj.P.p;
import static org.kantega.kson.JsonResult.*;

public class JsonDecoders {

  public static final JsonDecoder<String> stringDecoder =
      JsonValue::asText;

  public static final JsonDecoder<BigDecimal> bigDecimalEncoder =
      JsonValue::asNumber;

  public static final JsonDecoder<Boolean> boolDecoder =
      JsonValue::asBool;

  public static <A> JsonDecoder<Option<A>> optionDecoder(JsonDecoder<A> da) {
    return v ->
        v
            .onNull(() -> success(Option.<A>none()))
            .orElse(da.decode(v).map(Option::some));
  }

  public static <A> JsonDecoder<List<A>> arrayDecoder(JsonDecoder<A> ad) {
    return v ->
        v.onArray(list -> sequence(list.map(ad))).orElse(fail("Not an array"));
  }

  public static <A> FieldDecoder<A> field(String name, JsonDecoder<A> valueDecoder) {
    return obj ->
        obj.get(name)
            .option(
                fail("No field with name " + name),
                v ->
                    valueDecoder
                        .decode(v)
                        .mapFail(str -> "Failure while deciding field " + name + ": " + str));
  }

  public static <A> JsonDecoder<A> obj(FieldDecoder<A> ad) {
    return v ->
        v.onObject(ad::apply).orElse(fail("Not an object"));
  }

  public static <A, B> JsonDecoder<P2<A, B>> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b) {
    return v ->
        v.onObject(and(a, b)::apply).orElse(fail("Not an object"));
  }

  public static <A, B, C> JsonDecoder<P3<A, B, C>> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c) {
    return v ->
        v
            .onObject(obj -> and(a, and(b, c)).apply(obj).map(Products::flatten3))
            .orElse(fail("Not an object"));
  }

  public static <A, B, C, D> JsonDecoder<P4<A, B, C, D>> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c,
      FieldDecoder<D> d) {
    return v ->
        v
            .onObject(obj -> and(a, and(b, and(c, d))).apply(obj).map(Products::flatten4))
            .orElse(fail("Not an object"));
  }

  public static <A, B, C, D, E> JsonDecoder<P5<A, B, C, D, E>> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c,
      FieldDecoder<D> d,
      FieldDecoder<E> e) {
    return v ->
        v
            .onObject(obj -> and(a, and(b, and(c, and(d, e)))).apply(obj).map(Products::flatten5))
            .orElse(fail("Not an object"));
  }

  public static <A, B, C, D, E,FF> JsonDecoder<P6<A, B, C, D, E,FF>> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c,
      FieldDecoder<D> d,
      FieldDecoder<E> e,
      FieldDecoder<FF> f) {
    return v ->
        v
            .onObject(obj -> and(a, and(b, and(c, and(d, and(e,f))))).apply(obj).map(Products::flatten6))
            .orElse(fail("Not an object"));
  }

  public static <A, B, C, D, E,FF,G> JsonDecoder<P7<A, B, C, D, E,FF,G>> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c,
      FieldDecoder<D> d,
      FieldDecoder<E> e,
      FieldDecoder<FF> f,
      FieldDecoder<G> g) {
    return v ->
        v
            .onObject(obj -> and(a, and(b, and(c, and(d, and(e,and(f,g)))))).apply(obj).map(Products::flatten7))
            .orElse(fail("Not an object"));
  }

  public static <A, B, C, D, E,FF,G,H> JsonDecoder<P8<A, B, C, D, E,FF,G,H>> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c,
      FieldDecoder<D> d,
      FieldDecoder<E> e,
      FieldDecoder<FF> f,
      FieldDecoder<G> g,
      FieldDecoder<H> h) {
    return v ->
        v
            .onObject(obj -> and(a, and(b, and(c, and(d, and(e,and(f,and(g,h))))))).apply(obj).map(Products::flatten8))
            .orElse(fail("Not an object"));
  }

  interface FieldDecoder<A> {
    JsonResult<A> apply(TreeMap<String, JsonValue> fields);

    default <B> FieldDecoder<B> map(F<A, B> f) {
      return fields -> apply(fields).map(f);
    }
  }

  private static <A, B> FieldDecoder<P2<A, B>> and(FieldDecoder<A> ad, FieldDecoder<B> bd) {
    return fields -> ad.apply(fields).bind(a -> bd.apply(fields).map(b -> p(a, b)));
  }
}
