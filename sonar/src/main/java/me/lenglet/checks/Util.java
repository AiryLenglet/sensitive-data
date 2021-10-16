package me.lenglet.checks;

import me.lenglet.Sensitive;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;

import java.util.Optional;

class Util {

    private static final String FULLY_QUALIFIED_ANNOTATION = Sensitive.class.getTypeName();

    static boolean isSensitive(Symbol.VariableSymbol field) {
        return field.metadata().isAnnotatedWith(FULLY_QUALIFIED_ANNOTATION);
    }

    static boolean isSensitive(Optional<Symbol.VariableSymbol> field) {
        return field.map(Util::isSensitive)
                .orElse(false);
    }

    static Optional<Symbol.VariableSymbol> getField(MethodInvocationTree methodInvocationTree) {
        String methodName = getMethodName(methodInvocationTree);

        final Symbol.TypeSymbol methodClass = (Symbol.TypeSymbol) methodInvocationTree.symbol().owner();
        return methodClass.lookupSymbols(methodName).stream()
                .filter(Symbol::isVariableSymbol)
                .map(s -> (Symbol.VariableSymbol) s)
                .findFirst();
    }

    static String getMethodName(MethodInvocationTree methodInvocationTree) {
        String methodName = methodInvocationTree.symbol().name();
        if (methodName.startsWith("get") || methodName.startsWith("set")) {
            char[] c = methodName.substring(3).toCharArray();
            c[0] = Character.toLowerCase(c[0]);
            methodName = new String(c);
        }
        return methodName;
    }
}
