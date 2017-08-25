package org.kantega.kson.codec;

import fj.*;
import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;
import fj.function.Try1;
import org.kantega.kson.JsonConversionFailure;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.JsonObject;
import org.kantega.kson.json.JsonValue;
import org.kantega.kson.json.JsonValues;
import org.kantega.kson.util.Products;

import java.math.BigDecimal;

import static fj.P.p;
import static org.kantega.kson.JsonResult.*;

public class JsonDecoders {

    public static final JsonDecoder<String> stringDecoder =
      JsonValue::asText;

    public static final JsonDecoder<BigDecimal> bigDecimalDecoder =
      JsonValue::asNumber;

    public static final JsonDecoder<Boolean> boolDecoder =
      JsonValue::asBool;

    public static <A> JsonDecoder<Option<A>> optionDecoder(JsonDecoder<A> da) {
        return v ->
          v
            .onNull(() -> success(Option.<A>none()))
            .orSome(da.decode(v).map(Option::some));
    }

    public static <A> JsonDecoder<List<A>> arrayDecoder(JsonDecoder<A> ad) {
        return v ->
          v.onArray(list -> sequence(list.map(ad))).orSome(fail("Not an array"));
    }

    public static <A> JsonDecoder<A> arrayIndexDecoder(int i, JsonDecoder<A> ad) {
        return v ->
          v.onArray(list -> JsonResult.tried(() -> ad.decode(list.toArray().get(i))).bind(x -> x)).orSome(fail("Not an array"));
    }

    public static <A> JsonDecoder<TreeMap<String, A>> fieldsDecoder(JsonDecoder<A> aDecoder) {
        return v ->
          v.onObject(props ->
            sequence(
              props
                .toList()
                .map(p2 -> aDecoder.decode(p2._2()).map(a -> P.p(p2._1(), a)))
            ).map(list -> TreeMap.iterableTreeMap(Ord.stringOrd, list)))
            .orSome(fail(v + "is not an object"));
    }

    public static <A> FieldDecoder<A> field(String name, JsonDecoder<A> valueDecoder) {
        return obj ->
          obj.get(name)
            .option(
              fail("No field with name " + name),
              v ->
                valueDecoder
                  .decode(v)
                  .mapFail(str -> "Failure while decoding field " + name + ": " + str));
    }

    public static <A> FieldDecoder<Option<A>> optionalField(String name, JsonDecoder<A> valueDecoder) {
        return obj ->
          obj.get(name)
            .option(
              success(Option.<A>none()),
              v ->
                valueDecoder
                  .decode(v)
                  .map(Option::some)
                  .mapFail(str -> "Failure while decoding field " + name + ": " + str));
    }

    public static <A> JsonDecoder<A> obj(FieldDecoder<A> ad) {
        return v ->
          v.onObject(ad::apply).orSome(fail("Not an object"));
    }

    public static <A, B> JsonDecoder<P2<A, B>> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b) {
        return v ->
          v.onObject(pair(a, b)::apply).orSome(fail("Not an object"));
    }

    public static <A, B, C> JsonDecoder<C> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      F2<A, B, C> f) {
        return obj(a, b).map(t -> f.f(t._1(), t._2()));
    }

    public static <A, B, C> JsonDecoder<P3<A, B, C>> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c) {
        return v ->
          v
            .onObject(obj -> pair(a, pair(b, c)).apply(obj).map(Products::flatten3))
            .orSome(fail(v + "is not an object"));
    }

    public static <A, B, C, D> JsonDecoder<D> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c,
      F3<A, B, C, D> f) {
        return obj(a, b, c).map(t -> f.f(t._1(), t._2(), t._3()));
    }

    public static <A, B, C, D> JsonDecoder<P4<A, B, C, D>> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c,
      FieldDecoder<D> d) {
        return v ->
          v
            .onObject(obj -> pair(a, pair(b, pair(c, d))).apply(obj).map(Products::flatten4))
            .orSome(fail(v + "is not an object"));
    }

    public static <A, B, C, D, E> JsonDecoder<E> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c,
      FieldDecoder<D> d,
      F4<A, B, C, D, E> f) {
        return obj(a, b, c, d).map(t -> f.f(t._1(), t._2(), t._3(), t._4()));
    }

    public static <A, B, C, D, E> JsonDecoder<P5<A, B, C, D, E>> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c,
      FieldDecoder<D> d,
      FieldDecoder<E> e) {
        return v ->
          v
            .onObject(obj -> pair(a, pair(b, pair(c, pair(d, e)))).apply(obj).map(Products::flatten5))
            .orSome(fail(v + "is not an object"));
    }

    public static <A, B, C, D, E, FF> JsonDecoder<FF> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c,
      FieldDecoder<D> d,
      FieldDecoder<E> e,
      F5<A, B, C, D, E, FF> f) {
        return obj(a, b, c, d, e).map(t -> f.f(t._1(), t._2(), t._3(), t._4(), t._5()));
    }

    public static <A, B, C, D, E, FF> JsonDecoder<P6<A, B, C, D, E, FF>> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c,
      FieldDecoder<D> d,
      FieldDecoder<E> e,
      FieldDecoder<FF> f) {
        return v ->
          v
            .onObject(obj -> pair(a, pair(b, pair(c, pair(d, pair(e, f))))).apply(obj).map(Products::flatten6))
            .orSome(fail(v + "is not an object"));
    }

    public static <A, B, C, D, E, FF, G> JsonDecoder<G> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c,
      FieldDecoder<D> d,
      FieldDecoder<E> e,
      FieldDecoder<FF> ff,
      F6<A, B, C, D, E, FF, G> f) {
        return obj(a, b, c, d, e, ff).map(t -> f.f(t._1(), t._2(), t._3(), t._4(), t._5(), t._6()));
    }

    public static <A, B, C, D, E, FF, G> JsonDecoder<P7<A, B, C, D, E, FF, G>> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c,
      FieldDecoder<D> d,
      FieldDecoder<E> e,
      FieldDecoder<FF> f,
      FieldDecoder<G> g) {
        return v ->
          v
            .onObject(obj -> pair(a, pair(b, pair(c, pair(d, pair(e, pair(f, g)))))).apply(obj).map(Products::flatten7))
            .orSome(fail("Not an object"));
    }

    public static <A, B, C, D, E, FF, G, H> JsonDecoder<H> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c,
      FieldDecoder<D> d,
      FieldDecoder<E> e,
      FieldDecoder<FF> ff,
      FieldDecoder<G> g,
      F7<A, B, C, D, E, FF, G, H> f) {
        return obj(a, b, c, d, e, ff, g).map(t -> f.f(t._1(), t._2(), t._3(), t._4(), t._5(), t._6(), t._7()));
    }

    public static <A, B, C, D, E, FF, G, H> JsonDecoder<P8<A, B, C, D, E, FF, G, H>> obj(
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
            .onObject(obj -> pair(a, pair(b, pair(c, pair(d, pair(e, pair(f, pair(g, h))))))).apply(obj).map(Products::flatten8))
            .orSome(fail("Not an object"));
    }

    public static <A, B, C, D, E, FF, G, H, I> JsonDecoder<I> obj(
      FieldDecoder<A> a,
      FieldDecoder<B> b,
      FieldDecoder<C> c,
      FieldDecoder<D> d,
      FieldDecoder<E> e,
      FieldDecoder<FF> ff,
      FieldDecoder<G> g,
      FieldDecoder<H> h,
      F8<A, B, C, D, E, FF, G, H, I> f) {
        return obj(a, b, c, d, e, ff, g, h).map(t -> f.f(t._1(), t._2(), t._3(), t._4(), t._5(), t._6(), t._7(), t._8()));
    }

    public static <A> JsonDecoder<A> objE(Try1<JsonResult<JsonObject>,A,JsonConversionFailure> f){
        return value-> {
            JsonResult<JsonObject> jobj =
              value
                .onObject(map->JsonResult.success(new JsonObject(map)))
                .orSome(JsonResult.fail(value +" is not a json object"));
            try{
                return JsonResult.success(f.f(jobj));
            }catch (JsonConversionFailure e){
                return JsonResult.fail(e.getMessage());
            }
        };
    }

    public interface FieldDecoder<A> {
        JsonResult<A> apply(TreeMap<String, JsonValue> fields);

        default <B> FieldDecoder<B> map(F<A, B> f) {
            return fields -> apply(fields).map(f);
        }
    }

    private static <A, B> FieldDecoder<P2<A, B>> pair(FieldDecoder<A> ad, FieldDecoder<B> bd) {
        return fields -> ad.apply(fields).bind(a -> bd.apply(fields).map(b -> p(a, b)));
    }

    public static <A, B> JsonDecoder<P2<A, B>> and(JsonDecoder<A> aDecoder, JsonDecoder<B> bDecoder) {
        return v -> aDecoder.decode(v).bind(a -> bDecoder.decode(v).map(b -> p(a, b)));
    }

    public static <A, B, C> JsonDecoder<C> and(JsonDecoder<A> aDecoder, JsonDecoder<B> bDecoder, F2<A, B, C> join) {
        return and(aDecoder, bDecoder).map(t -> join.f(t._1(), t._2()));
    }
}
