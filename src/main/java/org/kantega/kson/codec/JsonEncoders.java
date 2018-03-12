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

/**
 * Encoders for basic types
 */
public class JsonEncoders {

    /**
     * Encoder that encodes strings into json strings
     */
    public static final JsonEncoder<String> stringEncoder =
        JsonValues::jString;

    /**
     * Encoder that encodes bigdecimals into json numbers
     */
    public static final JsonEncoder<BigDecimal> bigDecimalEncoder =
        JsonValues::jNum;

    /**
     * Encoder that encodes boolean values
     */
    public static final JsonEncoder<Boolean> boolEncoder =
        JsonValues::jBool;

    /**
     * Encoder that encodes integers.
     */
    public static final JsonEncoder<Integer> integerEncoder =
        bigDecimalEncoder.contramap(BigDecimal::valueOf);

    /**
     * Encoder that encodes optional values.
     * @param aEncoder The encoder that can encode the contents of the optional value
     * @param <A> the type of the optional value
     * @return an encoder that can encode optional values.
     */
    public static <A> JsonEncoder<Option<A>> optionEncoder(JsonEncoder<A> aEncoder) {
        return maybeValue -> maybeValue.option(JsonValues.jNull(), aEncoder::encode);
    }

    /**
     * Encodes a fj.data.List into a json array
     *
     * @param aEncoder the encoder for the elements in the list
     * @param <A> the type of the elements
     * @return an encoder that encodes lists
     */
    public static <A> JsonEncoder<List<A>> arrayEncoder(JsonEncoder<A> aEncoder) {
        return list -> JsonValues.jArray(list.map(aEncoder::encode));
    }

    /**
     * Encodes a jf.data.TreeMap into a json object where the keys in the map are
     * represented as field names, and the values are encoded using the supplied encoder.
     *
     * @param aEncoder The encoder that encodes the values in the map
     * @param <A>      The type of the values in the map
     * @return an encoder that can encode maps
     */
    public static <A> JsonEncoder<TreeMap<String, A>> fieldsEncoder(JsonEncoder<A> aEncoder) {
        return map -> JsonValues.jObj(map.map(aEncoder::encode));
    }

    /**
     * A special encoder that is used when constructing objects. (see obj)
     *
     * @param name The name of the field
     * @param a    The encoder that encodes the value
     * @param <A>  The type of the value
     * @return a special encoder that help construct objects.
     */
    public static <A> FieldEncoder<A> field(String name, JsonEncoder<A> a) {
        return (obj, va) -> obj.withField(name, a.encode(va));
    }

    /**
     * Encodes an domain value into a json object.
     *
     * @param fe  the fieldencoder for that field.
     * @param <A> The type of the value that is encoded as the field value
     * @return an encoder that creates json objects
     */
    public static <A> JsonEncoder<A> obj(FieldEncoder<A> fe) {
        return a -> fe.apply(JsonObject.empty, a);
    }

    /**
     * En encoder that encodes domain values into a json object. You supply a field encoder that encodes a field, and
     * a lambda that converts your domain value into the type of the field converter.
     *
     * @param fe the encoder for the field
     * @param f unwraps the object
     * @param <A> the type of the field
     * @param <X> the type of the object
     * @return an encoder that encodes an object as a json obejct
     */
    public static <A, X> JsonEncoder<X> obj(FieldEncoder<A> fe, F<X, A> f) {
        return obj(fe).contramap(f);
    }

    /**
     * An encoder that encodes a tuple of domain values into a json object. You supply encoders for each field.
     *
     * @param a   The encoder that knows how to encode the first field value
     * @param b   The encoder that knows how to encode the second field value
     * @param <A> The type of the first field value
     * @param <B> The type of the second field value
     * @return an encoder that encodes tuples as a json object
     */
    public static <A, B> JsonEncoder<P2<A, B>> obj(
        FieldEncoder<A> a,
        FieldEncoder<B> b) {
        return p ->
            and(a, b).apply(JsonObject.empty, p);
    }

    /**
     * An encoder that encodes domain object into a json object. You supply encoders for each field, and a lambda that extracts the values that
     *
     * @param a   The encoder that knows how to encode the first field value
     * @param b   The encoder that knows how to encode the second field value
     * @param <A> The type of the first field value
     * @param <B> The type of the second field value
     * @param <X> The type of the domain object
     * @return An encoder that encodes domain objects of type X into a json object.
     */
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
