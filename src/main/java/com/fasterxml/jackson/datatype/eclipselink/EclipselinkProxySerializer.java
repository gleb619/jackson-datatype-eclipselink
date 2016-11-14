package com.fasterxml.jackson.datatype.eclipselink;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import org.eclipse.persistence.indirection.IndirectCollection;

import java.io.IOException;
import java.util.Objects;

/**
 * Serializer to use for values proxied using {@link org.eclipselink.proxy.IndirectCollection}.
 * <p>
 * TODO: should try to make this work more like Jackson
 * <code>BeanPropertyWriter</code>, possibly sub-classing
 * it -- it handles much of functionality we need, and has
 * access to more information than value serializers (like
 * this one) have.
 */
public class EclipselinkProxySerializer extends JsonSerializer<IndirectCollection> implements ContextualSerializer {
    /**
     * Property that has proxy value to handle
     */
    protected final BeanProperty _property;

    protected final boolean _forceLazyLoading;
    protected final boolean _serializeIdentifier;

    /**
     * For efficient serializer lookup, let's use this; most
     * of the time, there's just one type and one serializer.
     */
    protected PropertySerializerMap _dynamicSerializers;

    /*
     * Life cycle
     */
    public EclipselinkProxySerializer(boolean forceLazyLoading) {
        this(forceLazyLoading, false, null);
    }

    public EclipselinkProxySerializer(boolean forceLazyLoading, boolean serializeIdentifier) {
        this(forceLazyLoading, serializeIdentifier, null);
    }

    public EclipselinkProxySerializer(boolean forceLazyLoading, boolean serializeIdentifier, BeanProperty property) {
        _forceLazyLoading = forceLazyLoading;
        _serializeIdentifier = serializeIdentifier;
        _dynamicSerializers = PropertySerializerMap.emptyForProperties();
        _property = property;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        return new EclipselinkProxySerializer(this._forceLazyLoading, _serializeIdentifier, property);
    }    

    /*
    /**********************************************************************
    /* JsonSerializer impl
    /**********************************************************************
     */

    @Override
    public boolean isEmpty(SerializerProvider provider, IndirectCollection value) {
        return (value == null) || (findProxied(value) == null);
    }

    @Override
    public void serialize(IndirectCollection value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        Object proxiedValue = findProxied(value);
        // TODO: figure out how to suppress nulls, if necessary? (too late for that here)
        if (proxiedValue == null) {
            provider.defaultSerializeNull(jgen);
            return;
        }
        findSerializer(provider, proxiedValue).serialize(proxiedValue, jgen, provider);
    }

    @Override
    public void serializeWithType(IndirectCollection value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        Object proxiedValue = findProxied(value);
        if (proxiedValue == null) {
            provider.defaultSerializeNull(jgen);
            return;
        }
        /* This isn't exactly right, since type serializer really refers to proxy
         * object, not value. And we really don't either know static type (necessary
         * to know how to apply additional type info) or other things;
         * so it's not going to work well. But... we'll do out best.
         */
        findSerializer(provider, proxiedValue).serializeWithType(proxiedValue, jgen, provider, typeSer);
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        SerializerProvider prov = visitor.getProvider();
        if ((prov == null) || (_property == null)) {
            super.acceptJsonFormatVisitor(visitor, typeHint);
        } else {
            JavaType type = _property.getType();
            prov.findPrimaryPropertySerializer(type, _property)
                    .acceptJsonFormatVisitor(visitor, type);
        }
    }

    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */

    protected JsonSerializer<Object> findSerializer(SerializerProvider provider, Object value) throws IOException {
        /* TODO: if Hibernate did use generics, or we wanted to allow use of Jackson
         *  annotations to indicate type, should take that into account.
         */
        Class<?> type = value.getClass();
        /* we will use a map to contain serializers found so far, keyed by type:
         * this avoids potentially costly lookup from global caches and/or construction
         * of new serializers
         */
        /* 18-Oct-2013, tatu: Whether this is for the primary property or secondary is
         *   really anyone's guess at this point; proxies can exist at any level?
         */
        PropertySerializerMap.SerializerAndMapResult result =
                _dynamicSerializers.findAndAddPrimarySerializer(type, provider, _property);
        if (_dynamicSerializers != result.map) {
            _dynamicSerializers = result.map;
        }
        return result.serializer;
    }

    /**
     * Helper method for finding value being proxied, if it is available
     * or if it is to be forced to be loaded.
     */
    protected Object findProxied(IndirectCollection proxy) {
        if (Objects.nonNull(proxy) && Objects.nonNull(proxy.getDelegateObject())) {
            proxy.getDelegateObject();
        }

        return null;
    }

}
