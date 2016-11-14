package com.fasterxml.jackson.datatype.eclipselink;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.Module;


public class EclipselinkModule extends Module {
    protected final static int DEFAULT_FEATURES = Feature.collectDefaults();
    /**
     * Bit flag composed of bits that indicate which
     * {@link Feature}s
     * are enabled.
     */
    protected int _moduleFeatures = DEFAULT_FEATURES;

    @Override
    public String getModuleName() {
        return "jackson-datatype-eclipselink";
    }



    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    @Override
    public Version version() {
        return ModuleVersion.instance.version();
    }

    @Override
    public void setupModule(SetupContext context) {
        /* First, append annotation introspector (no need to override, esp.
         * as we just implement couple of methods)
         */
        // Then add serializers we need
        AnnotationIntrospector ai = annotationIntrospector();
        if (ai != null) {
            context.appendAnnotationIntrospector(ai);
        }
        context.addSerializers(new EclipselinkSerializers(_moduleFeatures));
        context.addBeanSerializerModifier(new EclipselinkSerializerModifier(_moduleFeatures));
    }

    /**
     * Method called during {@link #setupModule}, to create {@link com.fasterxml.jackson.databind.AnnotationIntrospector}
     * to register along with module. If null is returned, no introspector is added.
     */
    protected AnnotationIntrospector annotationIntrospector() {
        EclipselinkAnnotationIntrospector ai = new EclipselinkAnnotationIntrospector();
        ai.setUseTransient(isEnabled(Feature.USE_TRANSIENT_ANNOTATION));
        return ai;
    }

    public EclipselinkModule enable(Feature f) {
        _moduleFeatures |= f.getMask();
        return this;
    }
    
    /*
    /**********************************************************************
    /* Extended API, configuration
    /**********************************************************************
     */

    public EclipselinkModule disable(Feature f) {
        _moduleFeatures &= ~f.getMask();
        return this;
    }

    public final boolean isEnabled(Feature f) {
        return (_moduleFeatures & f.getMask()) != 0;
    }

    public EclipselinkModule configure(Feature f, boolean state) {
        if (state) {
            enable(f);
        } else {
            disable(f);
        }
        return this;
    }

    /**
     * Enumeration that defines all toggleable features this module
     */
    public enum Feature {
        /**
         * Whether lazy-loaded object should be forced to be loaded and then serialized
         * (true); or serialized as nulls (false).
         * <p>
         * Default value is false.
         */
        FORCE_LAZY_LOADING(false),

        /**
         * Whether {@link javax.persistence.Transient} annotation should be checked or not;
         * if true, will consider @Transient to mean that property is to be ignored;
         * if false annotation will have no effect.
         * <p>
         * Default value is true.
         */
        USE_TRANSIENT_ANNOTATION(true),

        /**
         * If FORCE_LAZY_LOADING is false, this feature serializes uninitialized lazy loading proxies as
         * <code>{"identifierName":"identifierValue"}</code> rather than <code>null</code>.
         * <p>
         * Default value is false.
         * <p>
         * Note that the name of the identifier property can only be determined if
         * <ul>
         * <li>the {@link Mapping} is provided to the EclipselinkModule, or </li>
         * <li>the persistence context that loaded the proxy has not yet been closed, or</li>
         * <li>the id property is mapped with property access (for instance because the {@code @Id}
         * annotation is applied to a method rather than a field)</li>
         * </ul>
         * Otherwise, the entity name will be used instead.
         */
        SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS(false),

        /**
         * This feature determines how {@link org.eclipselink.collection.spi.PersistentCollection}s properties
         * for which no annotation is found are handled with respect to
         * lazy-loading: if true, lazy-loading is only assumed if annotation
         * is used to indicate that; if false, lazy-loading is assumed to be
         * the default.
         * Note that {@link #FORCE_LAZY_LOADING} has priority over this Feature;
         * meaning that if it is defined as true, setting of this Feature has no
         * effect.
         * <p>
         * Default value is false, meaning that laziness is considered to be the
         * default value.
         *
         * @since 2.4
         */
        REQUIRE_EXPLICIT_LAZY_LOADING_MARKER(false),

        /**
         * Feature that may be enabled to force
         * replacement <code>org.eclipselink.collection.spi.PersistentCollection</code>,
         * <code>List</code>, <code>Set</code>, <code>Map</code> subclasses
         * during serialization as standard JDK {@lin java.util.List},
         * {@link java.util.Set} and {@link java.util.Map}.
         * This is usually done to prevent issues with polymorphic handling, so
         * that type id is generated for standard containers and NOT for Hibernate
         * variants.
         * <p>
         * Default setting is false, so that no replacement occurs.
         *
         * @since 2.8.2
         */
        REPLACE_PERSISTENT_COLLECTIONS(false);

        final boolean _defaultState;
        final int _mask;

        Feature(boolean defaultState) {
            _defaultState = defaultState;
            _mask = (1 << ordinal());
        }

        /**
         * Method that calculates bit set (flags) of all features that
         * are enabled by default.
         */
        public static int collectDefaults() {
            int flags = 0;
            for (Feature f : values()) {
                if (f.enabledByDefault()) {
                    flags |= f.getMask();
                }
            }
            return flags;
        }

        public boolean enabledIn(int flags) {
            return (flags & _mask) != 0;
        }

        public boolean disabledIn(int flags) {
            return (flags & _mask) == 0;
        }

        public boolean enabledByDefault() {
            return _defaultState;
        }

        public int getMask() {
            return _mask;
        }
    }

}