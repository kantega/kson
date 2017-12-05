package org.kantega.kson.example;

import fj.P;
import org.kantega.kson.JsonResult;
import org.kantega.kson.codec.JsonDecoder;
import org.kantega.kson.codec.JsonDecoders;
import org.kantega.kson.codec.JsonEncoder;
import org.kantega.kson.codec.JsonEncoders;
import org.kantega.kson.parser.JsonParser;
import org.kantega.kson.parser.JsonWriter;
import org.kantega.kson.skinny.SkinnyDecoders;
import org.kantega.kson.skinny.SkinnyDemux;
import org.kantega.kson.skinny.SkinnyEncoders;
import org.kantega.kson.skinny.SkinnyMux;

import static fj.P.p;
import static org.kantega.kson.codec.JsonDecoders.*;
import static org.kantega.kson.codec.JsonEncoders.*;

public class SkinnyExample {

    interface Msg {
    }

    static class UserId {
        final String value;

        UserId(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "UserId{" +
              "value='" + value + '\'' +
              '}';
        }
    }

    static class AddUser implements Msg {

        final UserId userId;
        final String name;

        AddUser(UserId userId, String name) {
            this.userId = userId;
            this.name = name;
        }

        @Override
        public String toString() {
            return "AddUser{" +
              "userId=" + userId +
              ", name='" + name + '\'' +
              '}';
        }
    }

    static class ItemId {
        final String value;

        ItemId(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "ItemId{" +
              "value='" + value + '\'' +
              '}';
        }
    }

    static class AddItem implements Msg {

        final ItemId itemId;
        final UserId userId;

        AddItem(ItemId itemId, UserId userId) {
            this.itemId = itemId;
            this.userId = userId;
        }

        @Override
        public String toString() {
            return "AddItem{" +
              "itemId=" + itemId +
              ", userId=" + userId +
              '}';
        }
    }

    static JsonEncoder<AddUser> addUserEnc =
      SkinnyEncoders.skinnyEncoder(stringEncoder, stringEncoder, addUser -> p(addUser.userId.value, addUser.name));

    static JsonEncoder<AddItem> addItemEnc =
      SkinnyEncoders.skinnyEncoder(stringEncoder, stringEncoder, addItem -> p(addItem.itemId.value, addItem.userId.value));

    static JsonDecoder<AddUser> addUserDec =
      SkinnyDecoders.skinny(stringDecoder, stringDecoder, (uid, name) -> new AddUser(new UserId(uid), name));

    static JsonDecoder<AddItem> addItemDec =
      SkinnyDecoders.skinny(stringDecoder, stringDecoder, (iid, uid) -> new AddItem(new ItemId(iid), new UserId(uid)));

    public static void main(String[] args) {

        AddUser addUserMsg = new AddUser(new UserId("a"), "Ola Normann");
        AddItem addItemMsg = new AddItem(new ItemId("123qweasd"), new UserId("a"));


        String addUserMsgAsString = JsonWriter.write(addUserEnc.encode(addUserMsg));
        String addItemMsgAsString = JsonWriter.write(addItemEnc.encode(addItemMsg));

        System.out.println(addUserMsgAsString);
        System.out.println(addItemMsgAsString);

        JsonResult<AddUser> addUserDecoded = JsonParser.parse(addUserMsgAsString).decode(addUserDec);
        JsonResult<AddItem> addItemDecoded = JsonParser.parse(addItemMsgAsString).decode(addItemDec);


        System.out.println(addUserDecoded);
        System.out.println(addItemDecoded);


        // *** Demuxing ***

        JsonDecoder<Msg> skinnyDemux =
          SkinnyDemux.<Msg>demuxer()
            .add("addUser", addUserDec)
            .add("addItem", addItemDec);

        JsonEncoder<Object> skinnyMux =
          SkinnyMux.muxer()
            .add(AddUser.class, "addUser", addUserEnc)
            .add(AddItem.class, "addItem", addItemEnc);

        String addUserAsMuxedString =
          JsonWriter.write(skinnyMux.encode(addUserMsg));

        String addItemAsMuxedString =
          JsonWriter.write(skinnyMux.encode(addItemMsg));

        System.out.println(addUserAsMuxedString);
        System.out.println(addItemAsMuxedString);

        JsonResult<Msg> addUserDemuxed =
          JsonParser.parse(addUserAsMuxedString).decode(skinnyDemux);

        JsonResult<Msg> addItemDemuxed =
          JsonParser.parse(addItemAsMuxedString).decode(skinnyDemux);

        System.out.println(addUserDemuxed);
        System.out.println(addItemDemuxed);
    }
}
