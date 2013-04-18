package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.FieldReference;
import com.strobel.assembler.metadata.MemberReference;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.ParameterDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.Predicates;
import com.strobel.core.StringUtilities;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.ast.Variable;
import com.strobel.decompiler.languages.java.ast.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.strobel.core.CollectionUtilities.*;

public class IntroduceOuterClassReferencesTransform extends ContextTrackingVisitor<Void> {
    private final List<AstNode> _nodesToRemove;
    private final Set<ParameterDefinition> _parametersToRemove;
    private final Map<String, MethodDeclaration> _accessMethodDeclarations;

    public IntroduceOuterClassReferencesTransform(final DecompilerContext context) {
        super(context);

        _nodesToRemove = new ArrayList<>();
        _parametersToRemove = new HashSet<>();
        _accessMethodDeclarations = new HashMap<>();
    }

    @Override
    public void run(final AstNode compilationUnit) {
        //
        // First run through and locate any outer class member access methods.
        //
        new PhaseOneVisitor().run(compilationUnit);

        super.run(compilationUnit);

        for (final AstNode node : _nodesToRemove) {
            node.remove();
        }
    }

    private static String makeMethodKey(final MethodReference method) {
        return method.getFullName() + ":" + method.getErasedSignature();
    }

    @Override
    public Void visitInvocationExpression(final InvocationExpression node, final Void data) {
        super.visitInvocationExpression(node, data);

        final Expression target = node.getTarget();
        final AstNodeCollection<Expression> arguments = node.getArguments();

        if (target instanceof MemberReferenceExpression && arguments.size() == 1) {
            final MemberReferenceExpression memberReference = (MemberReferenceExpression) target;

            MemberReference reference = memberReference.getUserData(Keys.MEMBER_REFERENCE);

            if (reference == null) {
                reference = node.getUserData(Keys.MEMBER_REFERENCE);
            }

            if (reference instanceof MethodReference) {
                final MethodReference method = (MethodReference) reference;
                final MethodDefinition resolvedMethod = method.resolve();

                if (resolvedMethod != null && resolvedMethod.isConstructor()) {
                    final TypeDefinition declaringType = resolvedMethod.getDeclaringType();

                    if (declaringType.isInnerClass() || declaringType.isAnonymous()) {
                        for (final ParameterDefinition p : resolvedMethod.getParameters()) {
                            if (_parametersToRemove.contains(p)) {
                                final int parameterIndex = p.getPosition();
                                final Expression argumentToRemove = getOrDefault(arguments, parameterIndex);

                                if (argumentToRemove != null) {
                                    _nodesToRemove.add(argumentToRemove);
                                }
                            }
                        }
                    }
                }

                final String key = makeMethodKey(resolvedMethod != null ? resolvedMethod : method);
                final MethodDeclaration declaration = _accessMethodDeclarations.get(key);

                if (declaration != null) {
                    final MethodDefinition definition = declaration.getUserData(Keys.METHOD_DEFINITION);

                    if (definition != null && definition.getParameters().size() == 1) {
                        final AstNode inlinedBody = InliningHelper.inlineMethod(
                            declaration,
                            Collections.singletonMap(
                                definition.getParameters().get(0),
                                arguments.firstOrNullObject()
                            )
                        );

                        if (inlinedBody instanceof Expression) {
                            node.replaceWith(inlinedBody);
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Void visitMemberReferenceExpression(final MemberReferenceExpression node, final Void data) {
        tryIntroduceOuterClassReference(node, node.getTarget() instanceof ThisReferenceExpression);
        return super.visitMemberReferenceExpression(node, data);
    }

    private boolean tryIntroduceOuterClassReference(final MemberReferenceExpression node, final boolean hasThisOnLeft) {
        if (!hasThisOnLeft || !context.getCurrentType().isInnerClass() || context.getCurrentType().isStatic()) {
            return false;
        }

        if (node.getParent() instanceof AssignmentExpression &&
            node.getRole() == AssignmentExpression.LEFT_ROLE) {

            return false;
        }

        final String memberName = node.getMemberName();

        if (!StringUtilities.startsWith(memberName, "this$")) {
            return false;
        }

        final MemberReference reference = node.getUserData(Keys.MEMBER_REFERENCE);

        if (!(reference instanceof FieldReference)) {
            return false;
        }

        final FieldReference field = (FieldReference) reference;
        final FieldDefinition resolvedField = field.resolve();

        if (resolvedField == null || !resolvedField.isSynthetic()) {
            return false;
        }

        if (node.getParent() instanceof MemberReferenceExpression &&
            tryIntroduceOuterClassReference((MemberReferenceExpression) node.getParent(), hasThisOnLeft)) {

            return true;
        }

        final SimpleType outerType;

        final TypeReference outerTypeReference = field.getFieldType();
        final TypeDefinition resolvedOuterType = outerTypeReference.resolve();

        if (resolvedOuterType != null && resolvedOuterType.isAnonymous()) {
            if (resolvedOuterType.getExplicitInterfaces().isEmpty()) {
                outerType = new SimpleType(resolvedOuterType.getBaseType().getSimpleName());
            }
            else {
                outerType = new SimpleType(resolvedOuterType.getExplicitInterfaces().get(0).getSimpleName());
            }
        }
        else {
            if (resolvedOuterType != null) {
                outerType = new SimpleType(resolvedOuterType.getSimpleName());
            }
            else {
                outerType = new SimpleType(outerTypeReference.getSimpleName());
            }
        }

        outerType.putUserData(Keys.TYPE_REFERENCE, outerTypeReference);

        final OuterTypeReferenceExpression replacement = new OuterTypeReferenceExpression(outerType);

        node.replaceWith(replacement);

        return true;
    }

    // <editor-fold defaultstate="collapsed" desc="PhaseOneVisitor Class">

    private class PhaseOneVisitor extends ContextTrackingVisitor<Void> {
        private boolean _baseConstructorCalled;

        private PhaseOneVisitor() {
            super(IntroduceOuterClassReferencesTransform.this.context);
        }

        @Override
        public Void visitSuperReferenceExpression(final SuperReferenceExpression node, final Void _) {
            super.visitSuperReferenceExpression(node, _);

            if (context.getCurrentMethod() != null &&
                context.getCurrentMethod().isConstructor() &&
                node.getParent() instanceof InvocationExpression) {

                _baseConstructorCalled = true;
            }

            return null;
        }

        @Override
        public Void visitMethodDeclaration(final MethodDeclaration node, final Void _) {
            final boolean wasDone = _baseConstructorCalled;

            _baseConstructorCalled = false;

            try {
                final MethodDefinition method = node.getUserData(Keys.METHOD_DEFINITION);

                if (method != null &&
                    method.isSynthetic() &&
                    StringUtilities.startsWith(method.getName(), "access$") &&
                    node.getBody().getStatements().size() == 1) {

                    final Statement firstStatement = node.getBody().getStatements().firstOrNullObject();

                    if (firstStatement instanceof ReturnStatement &&
                        ((ReturnStatement) firstStatement).getExpression() instanceof MemberReferenceExpression) {

                        if (method.isSynthetic() && method.isStatic()) {
                            final List<ParameterDefinition> p = method.getParameters();

                            if (p.size() == 1) {
                                _accessMethodDeclarations.put(makeMethodKey(method), node);
                            }
                        }
                    }
                }

                return super.visitMethodDeclaration(node, _);
            }
            finally {
                _baseConstructorCalled = wasDone;
            }
        }

        @Override
        public Void visitAssignmentExpression(final AssignmentExpression node, final Void _) {
            super.visitAssignmentExpression(node, _);

            if (context.getSettings().getShowSyntheticMembers() ||
                context.getCurrentMethod() == null ||
                !context.getCurrentMethod().isConstructor() ||
                !context.getCurrentType().isInnerClass() && !context.getCurrentType().isAnonymous()) {

                return null;
            }

            final Expression left = node.getLeft();
            final Expression right = node.getRight();

            if (left instanceof MemberReferenceExpression) {
                if (right instanceof IdentifierExpression) {
                    final Variable variable = right.getUserData(Keys.VARIABLE);

                    if (variable == null || !variable.isParameter()) {
                        return null;
                    }

                    final MemberReferenceExpression memberReference = (MemberReferenceExpression) left;
                    final MemberReference member = memberReference.getUserData(Keys.MEMBER_REFERENCE);

                    if (member instanceof FieldReference &&
                        memberReference.getTarget() instanceof ThisReferenceExpression) {

                        final FieldDefinition resolvedField = ((FieldReference) member).resolve();

                        if (resolvedField != null &&
                            resolvedField.isSynthetic() &&
                            resolvedField.getName().startsWith("this$")) {

                            final ParameterDefinition parameter = variable.getOriginalParameter();

                            _parametersToRemove.add(parameter);

                            final ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) firstOrDefault(
                                node.getAncestorsAndSelf(),
                                Predicates.<AstNode>instanceOf(ConstructorDeclaration.class)
                            );

                            if (constructorDeclaration != null && !constructorDeclaration.isNull()) {
                                final ParameterDeclaration parameterToRemove = getOrDefault(
                                    constructorDeclaration.getParameters(),
                                    parameter.getPosition()
                                );

                                if (parameterToRemove != null) {
                                    _nodesToRemove.add(parameterToRemove);
                                }
                            };

                            if (node.getParent() instanceof ExpressionStatement) {
                                _nodesToRemove.add(node.getParent());
                            }
                            else {
                                right.replaceWith(
                                    new OuterTypeReferenceExpression(
                                        new SimpleType(resolvedField.getFieldType().getSimpleName())
                                    )
                                );
                            }
                        }
                    }
                }
            }

            return null;
        }
    }

    // </editor-fold>
}
