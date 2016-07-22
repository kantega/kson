package org.kantega.kson.codec;

import fj.F;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.JsonValue;

public class JsonCodec<A> {

  public final JsonEncoder<A> encoder;
  public final JsonDecoder<A> decoder;

  private JsonCodec(JsonEncoder<A> encoder, JsonDecoder<A> decoder) {
    this.encoder = encoder;
    this.decoder = decoder;
  }

  public static <A> JsonCodec<A> JsonCodec(JsonEncoder<A> encoder, JsonDecoder<A> decoder) {
    return new JsonCodec<A>(encoder, decoder);
  }

  public JsonValue encode(A t) {
    return encoder.encode(t);
  }

  public JsonResult<A> decode(JsonValue val) {
    return decoder.decode(val);
  }

  public <B> JsonCodec<B> xmap(F<B, A> f, F<A, B> g) {
    return new JsonCodec<>(encoder.contramap(f), decoder.map(g));
  }

}
