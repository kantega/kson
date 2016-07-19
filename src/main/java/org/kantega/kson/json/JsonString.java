package org.kantega.kson.json;

public class JsonString extends JsonValue{

  public final String value;

  public JsonString(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("JsonString{");
    sb.append("value='").append(value).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
