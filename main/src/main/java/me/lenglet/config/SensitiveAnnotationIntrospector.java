package me.lenglet.config;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import me.lenglet.Sensitive;

public class SensitiveAnnotationIntrospector extends JacksonAnnotationIntrospector {
    @Override
    public Object findDeserializer(Annotated a) {
        if (a.hasAnnotation(Sensitive.class)) {
            return new SensitiveStringDeserializer();
        }
        return super.findDeserializer(a);
    }

    @Override
    public Object findSerializer(Annotated a) {
        if (a.hasAnnotation(Sensitive.class)) {
            return new SensitiveStringSerializer();
        }
        return super.findSerializer(a);
    }
}
