package org.kantega.kson.codec;

import fj.*;
import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;
import org.kantega.kson.json.JsonObject;
import org.kantega.kson.json.JsonValues;
import org.kantega.kson.util.Products;

import java.math.BigDecimal;

import static org.kantega.kson.util.Products.*;

public class JsonEncoders {

    public static final JsonEncoder<String> stringEncoder =
      JsonValues::jString;

    public static final JsonEncoder<BigDecimal> bigDecimalEncoder =
      JsonValues::jNum;

    public static final JsonEncoder<Boolean> boolEncoder =
      JsonValues::jBool;

    public static final JsonEncoder<Integer> integerEncoder =
      bigDecimalEncoder.contramap(BigDecimal::valueOf);

    public static <A> JsonEncoder<Option<A>> optionEncoder(JsonEncoder<A> aEncoder) {
        return maybeValue -> maybeValue.option(JsonValues.jNull(), aEncoder::encode);
    }

    public static <A> JsonEncoder<List<A>> arrayEncoder(JsonEncoder<A> aEncoder) {
        return list -> JsonValues.jArray(list.map(aEncoder::encode));
    }

    public static <A> JsonEncoder<TreeMap<String, A>> fieldsEncoder(JsonEncoder<A> aEncoder) {
        return map -> JsonValues.jObj(map.map(aEncoder::encode));
    }

    public static <A> FieldEncoder<A> field(String name, JsonEncoder<A> a) {
        return (obj, va) -> obj.withField(name, a.encode(va));
    }

    public static <A> JsonEncoder<A> obj(FieldEncoder<A> fe) {
        return a -> fe.apply(JsonObject.empty, a);
    }

    public static <A, X> JsonEncoder<X> obj(FieldEncoder<A> fe, F<X, A> f) {
        return obj(fe).contramap(f);
    }

    public static <A, B> JsonEncoder<P2<A, B>> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b) {
        return p ->
          and(a, b).apply(JsonObject.empty, p);
    }

    public static <A, B, X> JsonEncoder<X> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b,
      F<X, P2<A, B>> f) {
        return
          obj(a, b).contramap(f);
    }

    public static <A, B, C> JsonEncoder<P3<A, B, C>> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b,
      FieldEncoder<C> c) {
        return t ->
          and(a, and(b, c)).apply(JsonObject.empty, expand(t));
    }

    public static <A, B, C, X> JsonEncoder<X> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b,
      FieldEncoder<C> c,
      F<X, P3<A, B, C>> f) {
        return
          obj(a, b, c).contramap(f);
    }

    public static <A, B, C, D> JsonEncoder<P4<A, B, C, D>> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b,
      FieldEncoder<C> c,
      FieldEncoder<D> d) {
        return t ->
          and(a, and(b, and(c, d)))
            .apply(JsonObject.empty, expand(t));
    }

    public static <A, B, C, D, X> JsonEncoder<X> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b,
      FieldEncoder<C> c,
      FieldEncoder<D> d,
      F<X, P4<A, B, C, D>> f) {
        return obj(a, b, c, d).contramap(f);
    }

    public static <A, B, C, D, E> JsonEncoder<P5<A, B, C, D, E>> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b,
      FieldEncoder<C> c,
      FieldEncoder<D> d,
      FieldEncoder<E> e) {
        return t ->
          and(a, and(b, and(c, and(d, e))))
            .apply(JsonObject.empty, expand(t));
    }

    public static <A, B, C, D, E, X> JsonEncoder<X> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b,
      FieldEncoder<C> c,
      FieldEncoder<D> d,
      FieldEncoder<E> e,
      F<X, P5<A, B, C, D, E>> f) {
        return
          obj(a, b, c, d, e).contramap(f);
    }

    public static <A, B, C, D, E, FF> JsonEncoder<P6<A, B, C, D, E, FF>> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b,
      FieldEncoder<C> c,
      FieldEncoder<D> d,
      FieldEncoder<E> e,
      FieldEncoder<FF> f) {
        return t ->
          and(a, and(b, and(c, and(d, and(e, f)))))
            .apply(JsonObject.empty, expand(t));
    }

    public static <A, B, C, D, E, FF, X> JsonEncoder<X> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b,
      FieldEncoder<C> c,
      FieldEncoder<D> d,
      FieldEncoder<E> e,
      FieldEncoder<FF> ff,
      F<X, P6<A, B, C, D, E, FF>> f) {
        return obj(a, b, c, d, e, ff).contramap(f);
    }

    public static <A, B, C, D, E, FF, G> JsonEncoder<P7<A, B, C, D, E, FF, G>> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b,
      FieldEncoder<C> c,
      FieldEncoder<D> d,
      FieldEncoder<E> e,
      FieldEncoder<FF> f,
      FieldEncoder<G> g) {
        return t ->
          and(a, and(b, and(c, and(d, and(e, and(f, g))))))
            .apply(JsonObject.empty, expand(t));
    }

    public static <A, B, C, D, E, FF, G, X> JsonEncoder<X> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b,
      FieldEncoder<C> c,
      FieldEncoder<D> d,
      FieldEncoder<E> e,
      FieldEncoder<FF> ff,
      FieldEncoder<G> g,
      F<X, P7<A, B, C, D, E, FF, G>> f) {
        return obj(a, b, c, d, e, ff, g).contramap(f);
    }

    public static <A, B, C, D, E, FF, G, H> JsonEncoder<P8<A, B, C, D, E, FF, G, H>> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b,
      FieldEncoder<C> c,
      FieldEncoder<D> d,
      FieldEncoder<E> e,
      FieldEncoder<FF> f,
      FieldEncoder<G> g,
      FieldEncoder<H> h) {
        return t ->
          and(a, and(b, and(c, and(d, and(e, and(f, and(g, h)))))))
            .apply(JsonObject.empty, expand(t));
    }

    public static <A, B, C, D, E, FF, G, H, X> JsonEncoder<X> obj(
      FieldEncoder<A> a,
      FieldEncoder<B> b,
      FieldEncoder<C> c,
      FieldEncoder<D> d,
      FieldEncoder<E> e,
      FieldEncoder<FF> ff,
      FieldEncoder<G> g,
      FieldEncoder<H> h,
      F<X, P8<A, B, C, D, E, FF, G, H>> f) {
        return obj(a, b, c, d, e, ff, g, h).contramap(f);
    }

    public interface FieldEncoder<A> {
        JsonObject apply(JsonObject obj, A a);
    }

    private static <A, B> FieldEncoder<P2<A, B>> and(FieldEncoder<A> fa, FieldEncoder<B> fb) {
        return (obj, t) -> fb.apply(fa.apply(obj, t._1()), t._2());
    }

}
