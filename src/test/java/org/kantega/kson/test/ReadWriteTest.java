package org.kantega.kson.test;

import org.junit.Assert;
import org.junit.Test;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.JsonObject;
import org.kantega.kson.json.JsonValue;
import org.kantega.kson.parser.JsonParser;

import static org.kantega.kson.json.JsonValues.jBool;

public class ReadWriteTest {

    @Test
    public void parseFalse() {
        String json = "{\"flag\":false}";

        JsonResult<JsonValue> value =
          JsonParser.parse(json);

        JsonObject jObject =
          value.orThrow().asObject().orThrow();

        Assert.assertTrue(jObject.get("flag").some().equals(jBool(false)));
    }


}
