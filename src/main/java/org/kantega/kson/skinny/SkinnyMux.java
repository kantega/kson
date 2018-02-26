package org.kantega.kson.skinny;

import fj.F;
import fj.data.Option;
import org.kantega.kson.codec.JsonEncoder;
import org.kantega.kson.json.JsonValue;
import org.kantega.kson.json.JsonValues;

import static fj.P.p;
import static org.kantega.kson.json.JsonValues.jObj;

/**
 * A skinny muxer knows how to encode simple domain messages that are subclasses of A. You can use this to serialize a
 * message protocol using json.
 * @param <A>
 */
public class SkinnyMux<A> implements JsonEncoder<A> {

    private final F<A, Option<JsonValue>> encoders;

    public SkinnyMux(F<A, Option<JsonValue>> f) {
        this.encoders = f;
    }

    public static <A> SkinnyMux<A> muxer() {
        return new SkinnyMux<>(a -> Option.none());
    }

    public <T extends A> SkinnyMux<A> add(Class<T> c, String name, JsonEncoder<T> encoder) {
        F<A, Option<JsonValue>> f =
          a ->
            c.isInstance(a) ?
            Option.some(jObj(p(name, encoder.encode(c.cast(a))))) :
            Option.none();

        return new SkinnyMux<>(a -> encoders.f(a).orElse(f.f(a)));
    }

    @Override
    public JsonValue encode(A t) {
        return encoders.f(t).orSome(JsonValues.jNull());
    }


}
