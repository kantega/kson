package org.kantega.kson.skinny;

import fj.*;
import fj.data.List;
import org.kantega.kson.JsonResult;
import org.kantega.kson.codec.JsonDecoder;
import org.kantega.kson.json.JsonValue;
import org.kantega.kson.util.Products;

public class SkinnyDecoders {


    public static <A, T> JsonDecoder<T> skinny(JsonDecoder<A> ad, F<A, T> convert) {
        return arrayDecoder(ad).map(convert);
    }

    public static <A, B, T> JsonDecoder<T> skinny(JsonDecoder<A> ad, JsonDecoder<B> bd, F2<A, B, T> convert) {
        return arrayDecoder(ad, bd).map(F2Functions.tuple(convert));
    }

    public static <A, B, C, T> JsonDecoder<T> skinny(
      JsonDecoder<A> ad,
      JsonDecoder<B> bd,
      JsonDecoder<C> cd,
      F3<A, B, C, T> convert) {
        return arrayDecoder(ad, bd, cd).map(t -> convert.f(t._1(), t._2(), t._3()));
    }

    public static <A, B, C, D, T> JsonDecoder<T> skinny(
      JsonDecoder<A> ad,
      JsonDecoder<B> bd,
      JsonDecoder<C> cd,
      JsonDecoder<D> dd,
      F4<A, B, C, D, T> convert) {
        return arrayDecoder(ad, bd, cd, dd).map(t -> convert.f(t._1(), t._2(), t._3(), t._4()));
    }

    public static <A, B, C, D, E, T> JsonDecoder<T> skinny(
      JsonDecoder<A> ad,
      JsonDecoder<B> bd,
      JsonDecoder<C> cd,
      JsonDecoder<D> dd,
      JsonDecoder<E> ed,
      F5<A, B, C, D, E, T> convert) {
        return arrayDecoder(ad, bd, cd, dd, ed).map(t -> convert.f(t._1(), t._2(), t._3(), t._4(), t._5()));
    }

    public static <A, B, C, D, E, FF, T> JsonDecoder<T> skinny(
      JsonDecoder<A> ad,
      JsonDecoder<B> bd,
      JsonDecoder<C> cd,
      JsonDecoder<D> dd,
      JsonDecoder<E> ed,
      JsonDecoder<FF> fd,
      F6<A, B, C, D, E, FF, T> convert) {
        return arrayDecoder(ad, bd, cd, dd, ed, fd).map(t -> convert.f(t._1(), t._2(), t._3(), t._4(), t._5(), t._6()));
    }


    public static <A> JsonDecoder<A> arrayDecoder(JsonDecoder<A> ad) {
        return v ->
          v.asArray()
            .bind(list -> expectedLength(list, 1))
            .bind(list -> ad.decode(list.head()));
    }

    public static <A, B> JsonDecoder<P2<A, B>> arrayDecoder(JsonDecoder<A> ad, JsonDecoder<B> bd) {
        return v ->
          v.asArray()
            .bind(list -> expectedLength(list, 2))
            .bind(list -> decode(list, ad, bd));
    }

    public static <A, B, C> JsonDecoder<P3<A, B, C>> arrayDecoder(
      JsonDecoder<A> ad,
      JsonDecoder<B> bd,
      JsonDecoder<C> cd) {
        return v ->
          arrayDecoder(ad, arrayDecoder(bd, cd)).decode(v)
            .map(Products::flatten3);
    }

    public static <A, B, C, D> JsonDecoder<P4<A, B, C, D>> arrayDecoder(
      JsonDecoder<A> ad,
      JsonDecoder<B> bd,
      JsonDecoder<C> cd,
      JsonDecoder<D> dd) {
        return v ->
          arrayDecoder(ad, arrayDecoder(bd, arrayDecoder(cd, dd))).decode(v)
            .map(Products::flatten4);
    }

    public static <A, B, C, D, E> JsonDecoder<P5<A, B, C, D, E>> arrayDecoder(
      JsonDecoder<A> ad,
      JsonDecoder<B> bd,
      JsonDecoder<C> cd,
      JsonDecoder<D> dd,
      JsonDecoder<E> ed) {
        return v ->
          arrayDecoder(ad, arrayDecoder(bd, arrayDecoder(cd, arrayDecoder(dd, ed)))).decode(v)
            .map(Products::flatten5);
    }

    public static <A, B, C, D, E, FF> JsonDecoder<P6<A, B, C, D, E, FF>> arrayDecoder(
      JsonDecoder<A> ad,
      JsonDecoder<B> bd,
      JsonDecoder<C> cd,
      JsonDecoder<D> dd,
      JsonDecoder<E> ed,
      JsonDecoder<FF> fd) {
        return v ->
          arrayDecoder(ad, arrayDecoder(bd, arrayDecoder(cd, arrayDecoder(dd, arrayDecoder(ed, fd))))).decode(v)
            .map(Products::flatten6);
    }


    public static <A, B> JsonResult<P2<A, B>> decode(List<JsonValue> list, JsonDecoder<A> ad, JsonDecoder<B> bd) {
        return ad.decode(list.head()).bind(a -> bd.decode(list.tail().head()).map(b -> P.p(a, b)));
    }

    public static JsonResult<List<JsonValue>> expectedLength(List<JsonValue> list, int expect) {
        return list.length() < expect ?
               JsonResult.fail("Tried to decode a list with length " + expect + ", but got a list with length " + list.length()) :
               JsonResult.success(list);
    }

    public static <A> JsonResult<A> fail(JsonValue v) {
        return JsonResult.fail("Expected a list, but got a " + v);
    }
}
