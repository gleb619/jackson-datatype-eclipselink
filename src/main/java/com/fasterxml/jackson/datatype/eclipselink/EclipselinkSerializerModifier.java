package com.fasterxml.jackson.datatype.eclipselink;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;

public class EclipselinkSerializerModifier extends BeanSerializerModifier {

    protected final int _features;


    public EclipselinkSerializerModifier(int features) {
        _features = features;
    }

    @Override
    public JsonSerializer<?> modifyCollectionSerializer(SerializationConfig config,
                                                        CollectionType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        return new PersistentCollectionSerializer(valueType, serializer, _features);
    }

    @Override
    public JsonSerializer<?> modifyMapSerializer(SerializationConfig config,
                                                 MapType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        return new PersistentCollectionSerializer(valueType, serializer, _features);
    }
}
