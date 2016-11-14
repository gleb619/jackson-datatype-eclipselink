package com.fasterxml.jackson.datatype.eclipselink;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.Serializers;
import org.eclipse.persistence.indirection.IndirectCollection;

public class EclipselinkSerializers extends Serializers.Base {

    protected final boolean _forceLoading;
    protected final boolean _serializeIdentifiers;


    public EclipselinkSerializers(int features) {
        _forceLoading = EclipselinkModule.Feature.FORCE_LAZY_LOADING.enabledIn(features);
        _serializeIdentifiers = EclipselinkModule.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS.enabledIn(features);
    }

    @Override
    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
        Class<?> raw = type.getRawClass();
        if (IndirectCollection.class.isAssignableFrom(raw)) {
            return new EclipselinkProxySerializer(_forceLoading, _serializeIdentifiers);
        }

        return null;
    }
}
