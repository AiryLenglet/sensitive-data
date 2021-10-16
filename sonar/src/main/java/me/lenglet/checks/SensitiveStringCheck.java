package me.lenglet.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.*;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.lenglet.checks.Util.getField;
import static me.lenglet.checks.Util.isSensitive;
import static org.sonar.plugins.java.api.tree.Tree.Kind.*;

@Rule(key = "L-1")
public class SensitiveStringCheck extends IssuableSubscriptionVisitor {

    private static final String FULLY_QUALIFIED_TARGET = "me.lenglet.SensitiveString";

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
                if (field.isPresent() && !isSensitive(field.get())) {
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
}
