package org.kantega.kson.navigator;

import fj.data.Option;
import org.kantega.kson.JsonResult;
import org.kantega.kson.json.JsonValue;
import org.kantega.kson.lens.JsonLenses;
import org.kantega.kson.lens.JsonValueLens;

import static fj.data.Option.none;
import static fj.data.Option.some;


public interface Navigator {

    Navigator down(String field);

    Navigator up();

    JsonResult<JsonValue> get();

    JsonResult<String> asText();

    static Navigator value(JsonResult<JsonValue> value){
        return value.fold(FailedNavigator::new, Navigator::value);
    }

    static Navigator value(JsonValue value) {
        return new ObjectNavigator(value, JsonLenses.selfLens(), none());
    }

    class FailedNavigator implements Navigator {
        final String fail;

        public FailedNavigator(String fail) {
            this.fail = fail;
        }

        @Override
        public Navigator down(String field) {
            return this;
        }

        @Override
        public Navigator up() {
            return this;
        }

        @Override
        public JsonResult<JsonValue> get() {
            return JsonResult.fail(fail);
        }

        @Override
        public JsonResult<String> asText() {
            return JsonResult.fail(fail);
        }
    }

    class ObjectNavigator implements Navigator {

        final JsonValue         jsonValue;
        final JsonValueLens     lens;
        final Option<Navigator> prev;


        private ObjectNavigator(JsonValue jsonValue, JsonValueLens lens, Option<Navigator> prev) {
            this.jsonValue = jsonValue;
            this.lens = lens;
            this.prev = prev;
        }


        public Navigator down(String field) {
            return new ObjectNavigator(jsonValue, lens.then(JsonLenses.select(field)), some(this));
        }

        public Navigator up() {
            return prev.orSome(this);
        }

        public JsonResult<JsonValue> get() {
            return lens.get().f(jsonValue);
        }

        public JsonResult<String> asText() {
            return get().bind(JsonValue::asText);
        }
    }
}




