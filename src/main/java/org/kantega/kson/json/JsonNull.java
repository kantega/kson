package org.kantega.kson.json;

import fj.Equal;
import fj.F0;
import fj.data.Option;

public class JsonNull extends JsonValue {

  public final static Equal<JsonNull> eq =
      Equal.equal(one -> other -> true);

  public <T> Option<T> onNull(F0<T> f) {
    return Option.some(f.f());
  }
}
