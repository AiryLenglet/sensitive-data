package me.lenglet.checks;

import me.lenglet.Sensitive;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.*;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.sonar.plugins.java.api.tree.Tree.Kind.*;

@Rule(key = "L-1")
public class SensitiveStringCheck extends IssuableSubscriptionVisitor {

    private static final String FULLY_QUALIFIED_TARGET = "me.lenglet.SensitiveString";
    private static final String FULLY_QUALIFIED_ANNOTATION = Sensitive.class.getTypeName();

    @Override
    public List<Kind> nodesToVisit() {
        List<Kind> result = new ArrayList<>();
        result.add(NEW_CLASS);
        result.add(METHOD_INVOCATION);
        return result;
    }

    @Override
    public void visitNode(Tree tree) {
        if (tree.is(NEW_CLASS)) {
            visitNewClassTree(tree);
        } else if (tree.is(METHOD_INVOCATION)) {
            visitMethodInvocation(tree);
        }
    }

    private boolean isSensitive(Symbol.VariableSymbol field) {
        return field.metadata().isAnnotatedWith(FULLY_QUALIFIED_ANNOTATION);
    }

    private boolean isSensitive(Optional<Symbol.VariableSymbol> field) {
        return field.map(this::isSensitive)
                .orElse(false);
    }

    private boolean hasTreeSensitive(Tree tree) {
        if (tree.is(METHOD_INVOCATION)) {
            MethodInvocationTree methodInvocationTree = (MethodInvocationTree) tree;
            return isSensitive(getField(methodInvocationTree));
        }
        return false;
    }

    private boolean isParentSensitive(Tree tree) {
        if (tree.is(Kind.ARGUMENTS)) {
            Arguments arguments = (Arguments) tree;
        }
        return false;
    }

    private void visitMethodInvocation(Tree tree) {
        MethodInvocationTree methodInvocationTree = (MethodInvocationTree) tree;
        if (methodInvocationTree.symbol().isMethodSymbol()) {
            final Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) methodInvocationTree.symbol();
            if (methodSymbol.returnType().type().is(FULLY_QUALIFIED_TARGET) &&
                    methodInvocationTree.arguments().stream().noneMatch(this::hasTreeSensitive)) {
                this.reportIssue(methodInvocationTree, "parameters are not sensitive");
            } else if (methodSymbol.parameterTypes().stream().anyMatch(t -> t.is(FULLY_QUALIFIED_TARGET)) &&
                    isParentSensitive(methodInvocationTree.parent())) {
                this.reportIssue(tree, "parent is not sensitive");
            }
        }

        if (methodInvocationTree.parent() != null
                && methodInvocationTree.parent().kind() == ARGUMENTS) {
            final Arguments arguments = (Arguments) methodInvocationTree.parent();
            if (arguments.parent() != null && arguments.parent().is(METHOD_INVOCATION)) {
                final Optional<Symbol.VariableSymbol> field = getField((MethodInvocationTree) arguments.parent());
                if (!field.isPresent()) {
                    return;
                }
                if (isSensitive(field) != hasTreeSensitive(methodInvocationTree)) {
                    this.reportIssue(tree, "parent is not sensitive");
                }
            }
        }
    }

    private static Optional<Symbol.VariableSymbol> getField(MethodInvocationTree methodInvocationTree) {
        String methodName = getMethodName(methodInvocationTree);

        final Symbol.TypeSymbol methodClass = (Symbol.TypeSymbol) methodInvocationTree.symbol().owner();
        return methodClass.lookupSymbols(methodName).stream()
                .filter(Symbol::isVariableSymbol)
                .map(s -> (Symbol.VariableSymbol) s)
                .findFirst();
    }

    private void visitNewClassTree(Tree tree) {

        NewClassTree newClassTree = (NewClassTree) tree;
        if (!newClassTree.identifier().symbolType().is(FULLY_QUALIFIED_TARGET)) {
            return;
        }
        if (newClassTree.arguments().size() == 1) {

            final ExpressionTree expressionTree = newClassTree.arguments().get(0);
            if (expressionTree.is(METHOD_INVOCATION)) {
                MethodInvocationTree methodInvocationTree = (MethodInvocationTree) expressionTree;
                final Optional<Symbol.VariableSymbol> field = getField(methodInvocationTree);
                if (field.isPresent() && !field.get().metadata().isAnnotatedWith(FULLY_QUALIFIED_ANNOTATION)) {
                    reportIssue(newClassTree, "SensitiveString created from non sensitive data");
                }

            } else if (expressionTree.is(Kind.STRING_LITERAL)) {
                reportIssue(newClassTree, "SensitiveString created from non sensitive data");
            } else if (expressionTree.is(Kind.IDENTIFIER)) {
                if (((VariableTree) ((IdentifierTree) expressionTree).symbol().declaration()).symbol().isVariableSymbol()) {
                    final Symbol.VariableSymbol variable = (Symbol.VariableSymbol) ((VariableTree) ((IdentifierTree) expressionTree).symbol().declaration()).symbol();
                    if (variable.declaration().initializer() != null && variable.declaration().initializer().is(Kind.STRING_LITERAL)) {
                        reportIssue(newClassTree, "SensitiveString created from non sensitive data");
                    }
                }
            }
        }
    }

    private static String getMethodName(MethodInvocationTree methodInvocationTree) {
        String methodName = methodInvocationTree.symbol().name();
        if (methodName.startsWith("get") || methodName.startsWith("set")) {
            char[] c = methodName.substring(3).toCharArray();
            c[0] = Character.toLowerCase(c[0]);
            methodName = new String(c);
        }
        return methodName;
    }
}
