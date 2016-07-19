package org.kantega.kson.json;

import fj.Equal;

import java.math.BigDecimal;

public class JsonNumber extends JsonValue {

  public final static Equal<JsonNumber> eq =
      Equal.bigdecimalEqual.contramap(num -> num.value);

  public final BigDecimal value;

  public JsonNumber(BigDecimal value) {
    this.value = value;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("JsonNumber{");
    sb.append(value);
    sb.append('}');
    return sb.toString();
  }
}
