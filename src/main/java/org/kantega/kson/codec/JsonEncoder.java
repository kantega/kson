package org.kantega.kson.codec;

import fj.F;
import org.kantega.kson.json.JsonValue;

/**
 * I JsonEncoder converts from objects of type A to a JsonValue. JsonEncoders can be adapted
 * to your domain model by using contramap(). You supply a lambda that adapts your domain
 * value to the value de encoder knows how to encode:
 *
 * i.e. To encode a userId as a string: JsonDecoders.stringDecoder.contramap(userId -&rarr userId.stringValue)
 * @param <A>
 */
public interface JsonEncoder<A> extends F<A, JsonValue> {

    JsonValue encode(A t);

    /**
     * Convert an object of type A to a JsonValue
     * @param a The value to convert
     * @return The value repesented in json.
     */
    @Override default JsonValue f(A a) {
        return encode(a);
    }

    /**
     * Adapt the type of the input so it fits into this codec.
     * @param f The lambda that adapts your domain value into a value this encoder can encode
     * @param <B> The type of the value you are adapting
     * @return an encoder that can encode your domain values.
     */
    default <B> JsonEncoder<B> contramap(F<B, A> f) {
        return b -> this.encode(f.f(b));
    }

}
