package org.kantega.kson.skinny;

import fj.*;
import org.kantega.kson.codec.JsonEncoder;

import static org.kantega.kson.json.JsonValues.jArray;

public class SkinnyEncoders {


    public static <A, T> JsonEncoder<T> skinnyEncoder(JsonEncoder<A> ae, F<T, A> extractor) {
        return t -> jArray(ae.encode(extractor.f(t)));
    }

    public static <A, B, T> JsonEncoder<T> skinnyEncoder(
      JsonEncoder<A> ae,
      JsonEncoder<B> be,
      F<T, P2<A, B>> extractor) {
        return t -> {
            P2<A, B> p = extractor.f(t);
            return jArray(ae.encode(p._1()), be.encode(p._2()));
        };
    }

    public static <A, B, C, T> JsonEncoder<T> skinnyEncoder(
      JsonEncoder<A> ae,
      JsonEncoder<B> be,
      JsonEncoder<C> ce,
      F<T, P3<A, B, C>> extractor) {
        return t -> {
            P3<A, B, C> p = extractor.f(t);
            return jArray(ae.encode(p._1()), be.encode(p._2()), ce.encode(p._3()));
        };
    }

    public static <A, B, C, D, T> JsonEncoder<T> skinnyEncoder(
      JsonEncoder<A> ae,
      JsonEncoder<B> be,
      JsonEncoder<C> ce,
      JsonEncoder<D> de,
      F<T, P4<A, B, C, D>> extractor) {
        return t -> {
            P4<A, B, C, D> p = extractor.f(t);
            return jArray(ae.encode(p._1()), be.encode(p._2()), ce.encode(p._3()), de.encode(p._4()));
        };
    }

    public static <A, B, C, D, E, T> JsonEncoder<T> skinnyEncoder(
      JsonEncoder<A> ae,
      JsonEncoder<B> be,
      JsonEncoder<C> ce,
      JsonEncoder<D> de,
      JsonEncoder<E> ee,
      F<T, P5<A, B, C, D, E>> extractor) {
        return t -> {
            P5<A, B, C, D, E> p = extractor.f(t);
            return jArray(ae.encode(p._1()), be.encode(p._2()), ce.encode(p._3()), de.encode(p._4()), ee.encode(p._5()));
        };
    }

    public static <A, B, C, D, E, FF, T> JsonEncoder<T> skinnyEncoder(
      JsonEncoder<A> ae,
      JsonEncoder<B> be,
      JsonEncoder<C> ce,
      JsonEncoder<D> de,
      JsonEncoder<E> ee,
      JsonEncoder<FF> fe,
      F<T, P6<A, B, C, D, E, FF>> extractor) {
        return t -> {
            P6<A, B, C, D, E, FF> p = extractor.f(t);
            return jArray(ae.encode(p._1()), be.encode(p._2()), ce.encode(p._3()), de.encode(p._4()), ee.encode(p._5()), fe.encode(p._6()));
        };
    }

}
