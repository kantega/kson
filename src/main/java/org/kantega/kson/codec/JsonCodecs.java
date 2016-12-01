package org.kantega.kson.codec;

import fj.*;
import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;

import java.math.BigDecimal;

import static org.kantega.kson.codec.JsonCodec.*;
import static org.kantega.kson.codec.JsonDecoders.*;
import static org.kantega.kson.codec.JsonEncoders.*;
import static org.kantega.kson.codec.JsonEncoders.bigDecimalEncoder;

public class JsonCodecs {

    public static JsonCodec<String> stringCodec =
      JsonCodec(stringEncoder, stringDecoder);

    public static JsonCodec<BigDecimal> bigDecimalCodec =
      JsonCodec(bigDecimalEncoder, bigDecimalDecoder);

    public static JsonCodec<Integer> intCodec =
      bigDecimalCodec.xmap(BigDecimal::valueOf, BigDecimal::intValue);

    public static JsonCodec<Long> longCodec =
      bigDecimalCodec.xmap(BigDecimal::valueOf, BigDecimal::longValue);


    public static JsonCodec<Double> doubleCodec =
      bigDecimalCodec.xmap(BigDecimal::valueOf, BigDecimal::doubleValue);

    public static JsonCodec<Boolean> booleanCodec =
      JsonCodec(boolEncoder, boolDecoder);

    public static <A> JsonCodec<Option<A>> optionCodec(JsonCodec<A> a) {
        return JsonCodec(optionEncoder(a.encoder), optionDecoder(a.decoder));
    }

    public static <A> JsonCodec<List<A>> arrayCodec(JsonCodec<A> a) {
        return JsonCodec(arrayEncoder(a.encoder), arrayDecoder(a.decoder));
    }

    public static <A> JsonCodec<TreeMap<String, A>> fieldsCodec(JsonCodec<A> a) {
        return JsonCodec(fieldsEncoder(a.encoder), fieldsDecoder(a.decoder));
    }

    public static <A> FieldCodec<A> field(String name, JsonCodec<A> valueCodec) {
        return new FieldCodec<>(JsonEncoders.field(name, valueCodec.encoder), JsonDecoders.field(name, valueCodec.decoder));
    }

    public static <A, X> JsonCodec<X> objectCodec(
      FieldCodec<A> a,
      F<X, A> from,
      F<A, X> to
    ) {
        return JsonCodec(JsonEncoders.obj(a.encoder).contramap(from), JsonDecoders.obj(a.decoder).map(to));
    }

    public static <A, B, X> JsonCodec<X> objectCodec(
      FieldCodec<A> a,
      FieldCodec<B> b,
      F<X, P2<A, B>> from,
      F2<A, B, X> to
    ) {
        return JsonCodec(
          JsonEncoders.obj(a.encoder, b.encoder).contramap(from),
          JsonDecoders.obj(a.decoder, b.decoder).map(t -> to.f(t._1(), t._2())));
    }

    public static <A, B, C, X> JsonCodec<X> objectCodec(
      FieldCodec<A> a,
      FieldCodec<B> b,
      FieldCodec<C> c,
      F<X, P3<A, B, C>> from,
      F3<A, B, C, X> to
    ) {
        return JsonCodec(
          JsonEncoders.obj(a.encoder, b.encoder, c.encoder).contramap(from),
          JsonDecoders.obj(a.decoder, b.decoder, c.decoder).map(t -> to.f(t._1(), t._2(), t._3())));
    }

    public static <A, B, C, D, X> JsonCodec<X> objectCodec(
      FieldCodec<A> a,
      FieldCodec<B> b,
      FieldCodec<C> c,
      FieldCodec<D> d,
      F<X, P4<A, B, C, D>> from,
      F4<A, B, C, D, X> to
    ) {
        return JsonCodec(
          JsonEncoders.obj(a.encoder, b.encoder, c.encoder, d.encoder).contramap(from),
          JsonDecoders.obj(a.decoder, b.decoder, c.decoder, d.decoder).map(t -> to.f(t._1(), t._2(), t._3(), t._4())));
    }

    public static <A, B, C, D, E, X> JsonCodec<X> objectCodec(
      FieldCodec<A> a,
      FieldCodec<B> b,
      FieldCodec<C> c,
      FieldCodec<D> d,
      FieldCodec<E> e,
      F<X, P5<A, B, C, D, E>> from,
      F5<A, B, C, D, E, X> to
    ) {
        return JsonCodec(
          JsonEncoders.obj(a.encoder, b.encoder, c.encoder, d.encoder, e.encoder).contramap(from),
          JsonDecoders.obj(a.decoder, b.decoder, c.decoder, d.decoder, e.decoder).map(t -> to.f(t._1(), t._2(), t._3(), t._4(), t._5())));
    }

    public static <A, B, C, D, E, FF, X> JsonCodec<X> objectCodec(
      FieldCodec<A> a,
      FieldCodec<B> b,
      FieldCodec<C> c,
      FieldCodec<D> d,
      FieldCodec<E> e,
      FieldCodec<FF> ff,
      F<X, P6<A, B, C, D, E, FF>> from,
      F6<A, B, C, D, E, FF, X> to
    ) {
        return JsonCodec(
          JsonEncoders.obj(a.encoder, b.encoder, c.encoder, d.encoder, e.encoder, ff.encoder).contramap(from),
          JsonDecoders.obj(a.decoder, b.decoder, c.decoder, d.decoder, e.decoder, ff.decoder).map(t -> to.f(t._1(), t._2(), t._3(), t._4(), t._5(), t._6())));
    }

    public static <A, B, C, D, E, FF, G, X> JsonCodec<X> objectCodec(
      FieldCodec<A> a,
      FieldCodec<B> b,
      FieldCodec<C> c,
      FieldCodec<D> d,
      FieldCodec<E> e,
      FieldCodec<FF> ff,
      FieldCodec<G> g,
      F<X, P7<A, B, C, D, E, FF, G>> from,
      F7<A, B, C, D, E, FF, G, X> to
    ) {
        return JsonCodec(
          JsonEncoders.obj(a.encoder, b.encoder, c.encoder, d.encoder, e.encoder, ff.encoder, g.encoder).contramap(from),
          JsonDecoders.obj(a.decoder, b.decoder, c.decoder, d.decoder, e.decoder, ff.decoder, g.decoder).map(t -> to.f(t._1(), t._2(), t._3(), t._4(), t._5(), t._6(), t._7())));
    }

    public static <A, B, C, D, E, FF, G, H, X> JsonCodec<X> objectCodec(
      FieldCodec<A> a,
      FieldCodec<B> b,
      FieldCodec<C> c,
      FieldCodec<D> d,
      FieldCodec<E> e,
      FieldCodec<FF> ff,
      FieldCodec<G> g,
      FieldCodec<H> h,
      F<X, P8<A, B, C, D, E, FF, G, H>> from,
      F8<A, B, C, D, E, FF, G, H, X> to
    ) {
        return JsonCodec(
          JsonEncoders.obj(a.encoder, b.encoder, c.encoder, d.encoder, e.encoder, ff.encoder, g.encoder, h.encoder).contramap(from),
          JsonDecoders.obj(a.decoder, b.decoder, c.decoder, d.decoder, e.decoder, ff.decoder, g.decoder, h.decoder).map(t -> to.f(t._1(), t._2(), t._3(), t._4(), t._5(), t._6(), t._7(), t._8())));
    }

    static class FieldCodec<A> {
        final FieldEncoder<A> encoder;
        final FieldDecoder<A> decoder;

        FieldCodec(FieldEncoder<A> encoder, FieldDecoder<A> decoder) {
            this.encoder = encoder;
            this.decoder = decoder;
        }
    }

}
