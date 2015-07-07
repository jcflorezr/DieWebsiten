package com.famisanar.ctc.demo;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


public class XGCalConverter {
    
    public static class Serializer implements JsonSerializer {
        
        public Serializer() {
            super();
        }

        public JsonElement serialize(Object t, Type type, JsonSerializationContext jsonSerializationContext) {
            XMLGregorianCalendar xgcal = (XMLGregorianCalendar)t;
            return new JsonPrimitive(xgcal.toXMLFormat());
        }
    }

    public static class Deserializer implements JsonDeserializer {

        public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
            try {
                return DatatypeFactory.newInstance().newXMLGregorianCalendar(jsonElement.getAsString());
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }
}
