package me.lenglet.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.Arguments;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.lenglet.checks.Util.getField;
import static me.lenglet.checks.Util.isSensitive;
import static org.sonar.plugins.java.api.tree.Tree.Kind.ARGUMENTS;
import static org.sonar.plugins.java.api.tree.Tree.Kind.METHOD_INVOCATION;

@Rule(key = "L-2")
public class SensitiveStringValueCheck extends IssuableSubscriptionVisitor {

    private static final String FULLY_QUALIFIED_TARGET = "me.lenglet.SensitiveString";

    @Override
    public List<Kind> nodesToVisit() {
        List<Kind> result = new ArrayList<>();
        result.add(METHOD_INVOCATION);
        return result;
    }

    @Override
    public void visitNode(Tree tree) {
        MethodInvocationTree methodInvocationTree = (MethodInvocationTree) tree;
        if (methodInvocationTree.symbol().isMethodSymbol()) {
            final Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) methodInvocationTree.symbol();
            final Symbol.TypeSymbol methodClass = (Symbol.TypeSymbol) methodSymbol.owner();
            if (!methodClass.type().is(FULLY_QUALIFIED_TARGET) || !methodSymbol.name().equals("getValue")) {
                return;
            }
            if (methodInvocationTree.parent() != null
                    && methodInvocationTree.parent().kind() == ARGUMENTS) {
                final Arguments arguments = (Arguments) methodInvocationTree.parent();
                if (arguments.parent() != null && arguments.parent().is(METHOD_INVOCATION)) {
                    final Optional<Symbol.VariableSymbol> field = getField((MethodInvocationTree) arguments.parent());
                    if (!field.isPresent()) {
                        return;
                    }
                    if (!isSensitive(field)) {
                        this.reportIssue(tree, "parent is not sensitive");
                    }
                }
            }
        }
    }
}
