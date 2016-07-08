package org.kantega.kson.json;

import fj.Equal;

public class JsonNull extends JsonValue {

  public final static Equal<JsonNull> eq =
      Equal.equal(one -> other -> true);

}
