package org.kantega.kson.codec;

import fj.F;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.JsonValue;

public interface JsonDecoder<A> extends F<JsonValue, JsonResult<A>> {

    default JsonResult<A> f(JsonValue v) {
        return decode(v);
    }

    JsonResult<A> decode(JsonValue v);

    default <B> JsonDecoder<B> map(F<A, B> f) {
        return v -> this.decode(v).map(f);
    }

    default <B> JsonDecoder<B> bind(F<A,JsonDecoder<B>> f){
        return v->this.decode(v).bind(a->f.f(a).decode(v));
    }

    default JsonDecoder<A> ensure(F<A, Boolean> pred) {
        return ensure(pred, pred.toString());
    }

    default JsonDecoder<A> ensure(F<A, Boolean> pred, String msg) {
        return v -> this.decode(v).bind(decoded -> pred.f(decoded) ? JsonResult.success(decoded) : JsonResult.fail("The value " + decoded + " did not satisfy the constraint " + msg));
    }
}
