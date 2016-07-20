package org.kantega.kson.codec;

import fj.F;
import org.kantega.kson.json.JsonValue;

public interface JsonEncoder<A> extends F<A, JsonValue> {

  @Override
  default JsonValue f(A a) {
    return encode(a);
  }

  JsonValue encode(A t);

  default <B> JsonEncoder<B> contramap(F<B, A> f) {
    return b -> this.encode(f.f(b));
  }

}
