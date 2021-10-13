package me.lenglet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_16)
@SupportedAnnotationTypes("me.lenglet.Sensitive")
public class SensitiveAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        final var messager = this.processingEnv.getMessager();

        for (Element element : roundEnv.getElementsAnnotatedWith(Sensitive.class)) {

            if (!String.class.getName().equals(element.asType().toString())) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Field type must be java.lang.String", element);
                continue;
            }
        }

        return true;
    }
}
