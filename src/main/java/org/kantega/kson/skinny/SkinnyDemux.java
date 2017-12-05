package org.kantega.kson.skinny;

import fj.Ord;
import fj.data.Option;
import fj.data.TreeMap;
import org.kantega.kson.JsonResult;
import org.kantega.kson.codec.JsonDecoder;
import org.kantega.kson.json.JsonValue;


/**
 * Demuxing messages based on name of the field that contains the payload
 * For example
 * {"msg":["value1","value2"]}
 * Would loog for the decoder registered under "msg"
 *
 * @param <A>
 */
public class SkinnyDemux<A> implements JsonDecoder<A> {

    private final TreeMap<String, JsonDecoder<A>> decoders;

    private SkinnyDemux(TreeMap<String, JsonDecoder<A>> decoders) {
        this.decoders = decoders;
    }

    public static <A> SkinnyDemux<A> demuxer() {
        TreeMap<String, JsonDecoder<A>> decoders =
          TreeMap.empty(Ord.stringOrd);
        return new SkinnyDemux<>(decoders);
    }

    public SkinnyDemux<A> add(String name, JsonDecoder<? extends A> decoder) {
        return new SkinnyDemux<>(decoders.set(name, (JsonDecoder<A>) decoder));
    }

    public JsonResult<A> decode(JsonValue value) {
        return
          value.asObject().bind(
            obj ->
              obj.pairs.min().map(pair -> {
                  String                 name         = pair._1();
                  Option<JsonDecoder<A>> maybeDecoder = decoders.get(name);
                  return maybeDecoder.map(decoder -> decoder.<A>decode(pair._2()))
                    .orSome(JsonResult.fail("No decoder registered with name " + name + " in demuxer"));
              }).orSome(JsonResult.fail("Found an empty object when trying to demux a message"))
          );
    }


}
