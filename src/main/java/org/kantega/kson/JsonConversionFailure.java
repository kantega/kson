package org.kantega.kson;

public class JsonConversionFailure extends RuntimeException {

    public JsonConversionFailure(String msg){
        super(msg);
    }

}
