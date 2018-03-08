package org.kantega.kson.codec;

import fj.F;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.JsonValue;

/**
 * A JsonDecoder can convert a JsonValue to some domain object.
 * The result is wrapped in a JsonResult, since conversion from an untyped datastructure
 * is error-prone.
 * One need a mechanism to capture possible outcomes of the conversion.
 *
 * @param <A> The type of the value the decoder creates
 */
public interface JsonDecoder<A> extends F<JsonValue, JsonResult<A>> {

    default JsonResult<A> f(JsonValue v) {
        return decode(v);
    }

    /**
     * Decodes the value into a domain object.
     *
     * @param v The jsonvalue to convert
     * @return The conversion result.
     */
    JsonResult<A> decode(JsonValue v);

    /**
     * Adapt the output of this codec into a value in your domain.
     *
     * @param f   The lambda that adapts the original output of this decoder.
     * @param <B> The tyoe of the value your are adapting to
     * @return A decoder that decodes the jsonvalue into your value.
     */
    default <B> JsonDecoder<B> map(F<A, B> f) {
        return v -> this.decode(v).map(f);
    }

    /**
     * When converting from json to your domain model, every conversion might fail.
     * To short circuit the decodingprocess when a decoder fails you can bind them together using
     * this method. The lambda you insert as an argument will receive the result of the first
     * decoder if it succeeds, and then you can supply the second decoder.
     * If the first one fails, the second one is never called.
     * <p>
     * This method is frequently called 'andThen' because it runs the first decoder andThen the
     * second decoder id the first one succeeds.
     *
     * @param f   The lambda that supplies the second decoder
     * @param <B> the type the second decoder converts to
     * @return a new decoder that binds this decoder with the second decoder.
     */
    default <B> JsonDecoder<B> bind(F<A, JsonDecoder<B>> f) {
        return v -> this.decode(v).bind(a -> f.f(a).decode(v));
    }

    /**
     * When decoding values it is sometimes useful to add constraints to
     * the values, forcing the decoder to fail if some requirements arent met.
     *
     * @param pred Test the decoded value.
     * @return A decoder that fails if the predicate is not met.
     */
    default JsonDecoder<A> ensure(F<A, Boolean> pred) {
        return ensure(pred, pred.toString());
    }

    /**
     * As ensure(pred), but with a simple message.
     *
     * @param pred The predicate that must hold
     * @param msg Message to output if fail
     * @return A new decoder that fails if the predicate does not hold
     */
    default JsonDecoder<A> ensure(F<A, Boolean> pred, String msg) {
        return ensure(pred,decoded -> "The value " + decoded + " did not satisfy the constraint " + msg);
    }

    /**
     * As ensure(pred), but you can supply the error message yourself
     *
     * @param pred The predicate that must yield true
     * @param msg The message to outputif the predicate does not hold
     * @return A new decoder that fails if the predicate does not hold.
     */
    default JsonDecoder<A> ensure(F<A, Boolean> pred, F<A, String> msg) {
        return v ->
            this.decode(v).bind(decoded ->
                pred.f(decoded) ?
                JsonResult.success(decoded) :
                JsonResult.fail(msg.f(decoded)));
    }
}
