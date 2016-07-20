package org.kantega.kson.codec;

import fj.data.List;
import fj.data.Option;
import org.kantega.kson.json.JsonValues;

import java.math.BigDecimal;

public class JsonEncoders {

  public static final JsonEncoder<String> stringEncoder =
      JsonValues::jString;

  public static final JsonEncoder<BigDecimal> bigDecimalEncoder =
      JsonValues::jNum;

  public static final JsonEncoder<Boolean> boolEncoder =
      JsonValues::jBool;

  public static <A> JsonEncoder<Option<A>> optionEncoder(JsonEncoder<A> aEncoder) {
    return maybeValue -> maybeValue.option(JsonValues.jNull(), aEncoder::encode);
  }

  public static <A> JsonEncoder<List<A>> arrayEncoder(JsonEncoder<A> aEncoder) {
    return list -> JsonValues.jArray(list.map(aEncoder::encode));
  }

}
