package org.kantega.kson.skinny;

import org.kantega.kson.codec.JsonEncoder;

import static fj.P.p;
import static org.kantega.kson.json.JsonValues.jObj;

public class SkinnyMux {

    public static <A> JsonEncoder<A> mux(String msgName, JsonEncoder<A> msgEncoder) {
        return a -> jObj(p(msgName, msgEncoder.encode(a)));
    }

}
