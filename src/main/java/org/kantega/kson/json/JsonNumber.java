package org.kantega.kson.json;

import fj.Equal;

import java.math.BigDecimal;

public class JsonNumber extends JsonValue {

  public final Equal<JsonNumber> eq =
      Equal.bigdecimalEqual.contramap(num -> num.value);

  public final BigDecimal value;

  public JsonNumber(BigDecimal value) {
    this.value = value;
  }

}
