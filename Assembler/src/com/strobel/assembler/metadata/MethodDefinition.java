/*
 * MethodDefinition.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.assembler.metadata;

import com.strobel.assembler.Collection;
import com.strobel.assembler.metadata.annotations.CustomAnnotation;

import java.util.Collections;
import java.util.List;

public class MethodDefinition extends MethodReference implements IMemberDefinition {
    private final GenericParameterCollection _genericParameters;
    private final ParameterDefinitionCollection _parameters;
    private final Collection<TypeReference> _thrownTypes;
    private final Collection<CustomAnnotation> _customAnnotations;
    private final List<GenericParameter> _genericParametersView;
    private final List<ParameterDefinition> _parametersView;
    private final List<TypeReference> _thrownTypesView;
    private final List<CustomAnnotation> _customAnnotationsView;

    private MethodBody _body;
    private String _name;
    private TypeReference _returnType;
    private TypeDefinition _declaringType;
    private long _flags;

    protected MethodDefinition() {
        _genericParameters = new GenericParameterCollection(this);
        _parameters = new ParameterDefinitionCollection(this);
        _thrownTypes = new Collection<>();
        _customAnnotations = new Collection<>();
        _genericParametersView = Collections.unmodifiableList(_genericParameters);
        _parametersView = Collections.unmodifiableList(_parameters);
        _thrownTypesView = Collections.unmodifiableList(_thrownTypes);
        _customAnnotationsView = Collections.unmodifiableList(_customAnnotations);
    }

    public final MethodBody getBody() {
        return _body;
    }

    protected final void setBody(final MethodBody body) {
        _body = body;
    }

    @Override
    public final boolean isDefinition() {
        return true;
    }

    @Override
    public final List<GenericParameter> getGenericParameters() {
        return _genericParametersView;
    }

    @Override
    public final List<TypeReference> getThrownTypes() {
        return _thrownTypesView;
    }

    @Override
    public final TypeDefinition getDeclaringType() {
        return _declaringType;
    }

    @Override
    public final List<CustomAnnotation> getAnnotations() {
        return _customAnnotationsView;
    }

    @Override
    public final String getName() {
        return _name;
    }

    @Override
    public final TypeReference getReturnType() {
        return _returnType;
    }

    @Override
    public final List<ParameterDefinition> getParameters() {
        return _parametersView;
    }

    protected final void setName(final String name) {
        _name = name;
    }

    protected final void setReturnType(final TypeReference returnType) {
        _returnType = returnType;
    }

    protected final void setDeclaringType(final TypeDefinition declaringType) {
        _declaringType = declaringType;
    }

    protected final void setFlags(final long flags) {
        _flags = flags;
    }

    protected final GenericParameterCollection getGenericParametersInternal() {
        return _genericParameters;
    }

    protected final ParameterDefinitionCollection getParametersInternal() {
        return _parameters;
    }

    protected final Collection<TypeReference> getThrownTypesInternal() {
        return _thrownTypes;
    }

    protected final Collection<CustomAnnotation> getAnnotationsInternal() {
        return _customAnnotations;
    }

    // <editor-fold defaultstate="collapsed" desc="Method Attributes">

    public final boolean isAbstract() {
        return Flags.testAny(getFlags(), Flags.ABSTRACT);
    }

    public final boolean isBridgeMethod() {
        return Flags.testAny(getFlags(), Flags.ACC_BRIDGE);
    }

    public final boolean isVarArgs() {
        return Flags.testAny(getFlags(), Flags.ACC_VARARGS);
    }

    // </editor-fold>==≠

    // <editor-fold defaultstate="collapsed" desc="Member Attributes">_

    @Override
    public final long getFlags() {
        return _flags;
    }

    @Override
    public final int getModifiers() {
        return Flags.toModifiers(getFlags());
    }

    @Override
    public final boolean isFinal() {
        return Flags.testAny(getFlags(), Flags.FINAL);
    }

    @Override
    public final boolean isNonPublic() {
        return !Flags.testAny(getFlags(), Flags.PUBLIC);
    }

    @Override
    public final boolean isPrivate() {
        return Flags.testAny(getFlags(), Flags.PRIVATE);
    }

    @Override
    public final boolean isProtected() {
        return Flags.testAny(getFlags(), Flags.PROTECTED);
    }

    @Override
    public final boolean isPublic() {
        return Flags.testAny(getFlags(), Flags.PUBLIC);
    }

    @Override
    public final boolean isStatic() {
        return Flags.testAny(getFlags(), Flags.STATIC);
    }

    @Override
    public final boolean isSynthetic() {
        return Flags.testAny(getFlags(), Flags.SYNTHETIC);
    }

    @Override
    public final boolean isDeprecated() {
        return Flags.testAny(getFlags(), Flags.DEPRECATED);
    }

    @Override
    public final boolean isPackagePrivate() {
        return !Flags.testAny(getFlags(), Flags.PUBLIC | Flags.PROTECTED | Flags.PRIVATE);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Name and Signature Formatting">

    /**
     * Human-readable brief description of a type or member, which does not include information super types, thrown exceptions, or modifiers other than
     * 'static'.
     */
    @Override
    public String getBriefDescription() {
        return appendBriefDescription(new StringBuilder()).toString();
    }

    /**
     * Human-readable full description of a type or member, which includes specification of super types (in brief format), thrown exceptions, and modifiers.
     */
    @Override
    public String getDescription() {
        return appendDescription(new StringBuilder()).toString();
    }

    /**
     * Human-readable erased description of a type or member.
     */
    @Override
    public String getErasedDescription() {
        return appendErasedDescription(new StringBuilder()).toString();
    }

    /**
     * Human-readable simple description of a type or member, which does not include information super type or fully-qualified type names.
     */
    @Override
    public String getSimpleDescription() {
        return appendSimpleDescription(new StringBuilder()).toString();
    }

    @Override
    protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
        if (fullName) {
            final TypeDefinition declaringType = getDeclaringType();

            if (declaringType != null) {
                return declaringType.appendName(sb, true, false).append(getName());
            }
        }

        return sb.append(_name);
    }

    public StringBuilder appendDescription(final StringBuilder sb) {
        StringBuilder s = new StringBuilder();

        for (final javax.lang.model.element.Modifier modifier : Flags.asModifierSet(getModifiers() & ~Flags.ACC_VARARGS)) {
            s.append(modifier.toString());
            s.append(' ');
        }

        final List<? extends TypeReference> typeArguments;

        if (this instanceof IGenericInstance) {
            typeArguments = ((IGenericInstance) this).getTypeArguments();
        }
        else if (hasGenericParameters()) {
            typeArguments = getGenericParameters();
        }
        else {
            typeArguments = Collections.emptyList();
        }

        if (!typeArguments.isEmpty()) {
            final int count = typeArguments.size();

            s.append('<');

            for (int i = 0; i < count; i++) {
                if (i != 0) {
                    s.append(", ");
                }
                s = typeArguments.get(i).appendSimpleDescription(s);
            }

            s.append('>');
            s.append(' ');
        }

        TypeReference returnType = getReturnType();

        while (returnType.isWildcardType()) {
            returnType = returnType.getExtendsBound();
        }

        if (returnType.isGenericParameter()) {
            s.append(returnType.getName());
        }
        else {
            s = returnType.appendSimpleDescription(s);
        }

        s.append(' ');
        s.append(getName());
        s.append('(');

        final List<ParameterDefinition> parameters = getParameters();

        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterDefinition p = parameters.get(i);

            if (i != 0) {
                s.append(", ");
            }

            TypeReference parameterType = p.getParameterType();

            while (parameterType.isWildcardType()) {
                parameterType = parameterType.getExtendsBound();
            }

            if (parameterType.isGenericParameter()) {
                s.append(parameterType.getName());
            }
            else {
                s = parameterType.appendSimpleDescription(s);
            }
        }

        s.append(')');

        final List<TypeReference> thrownTypes = getThrownTypes();

        if (!thrownTypes.isEmpty()) {
            s.append(" throws ");

            for (int i = 0, n = thrownTypes.size(); i < n; ++i) {
                final TypeReference t = thrownTypes.get(i);
                if (i != 0) {
                    s.append(", ");
                }
                s = t.appendBriefDescription(s);
            }
        }

        return s;
    }

    public StringBuilder appendSimpleDescription(final StringBuilder sb) {
        StringBuilder s = new StringBuilder();

        for (final javax.lang.model.element.Modifier modifier : Flags.asModifierSet(getModifiers() & ~Flags.ACC_VARARGS)) {
            s.append(modifier.toString());
            s.append(' ');
        }

        final List<? extends TypeReference> typeArguments;

        if (this instanceof IGenericInstance) {
            typeArguments = ((IGenericInstance) this).getTypeArguments();
        }
        else if (hasGenericParameters()) {
            typeArguments = getGenericParameters();
        }
        else {
            typeArguments = Collections.emptyList();
        }

        if (!typeArguments.isEmpty()) {
            s.append('<');
            for (int i = 0, n = typeArguments.size(); i < n; i++) {
                if (i != 0) {
                    s.append(", ");
                }
                s = typeArguments.get(i).appendSimpleDescription(s);
            }
            s.append('>');
            s.append(' ');
        }

        TypeReference returnType = getReturnType();

        while (returnType.isWildcardType()) {
            returnType = returnType.getExtendsBound();
        }

        if (returnType.isGenericParameter()) {
            s.append(returnType.getName());
        }
        else {
            s = returnType.appendSimpleDescription(s);
        }

        s.append(' ');
        s.append(getName());
        s.append('(');

        final List<ParameterDefinition> parameters = getParameters();

        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterDefinition p = parameters.get(i);

            if (i != 0) {
                s.append(", ");
            }

            TypeReference parameterType = p.getParameterType();

            while (parameterType.isWildcardType()) {
                parameterType = parameterType.getExtendsBound();
            }

            if (parameterType.isGenericParameter()) {
                s.append(parameterType.getName());
            }
            else {
                s = parameterType.appendSimpleDescription(s);
            }
        }

        s.append(')');

        final List<TypeReference> thrownTypes = getThrownTypes();

        if (!thrownTypes.isEmpty()) {
            s.append(" throws ");

            for (int i = 0, n = thrownTypes.size(); i < n; ++i) {
                final TypeReference t = thrownTypes.get(i);
                if (i != 0) {
                    s.append(", ");
                }
                s = t.appendSimpleDescription(s);
            }
        }

        return s;
    }

    public StringBuilder appendBriefDescription(final StringBuilder sb) {
        StringBuilder s = new StringBuilder();

        TypeReference returnType = getReturnType();

        while (returnType.isWildcardType()) {
            returnType = returnType.getExtendsBound();
        }

        if (returnType.isGenericParameter()) {
            s.append(returnType.getName());
        }
        else {
            s = returnType.appendBriefDescription(s);
        }

        s.append(' ');
        s.append(getName());
        s.append('(');

        final List<ParameterDefinition> parameters = getParameters();

        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterDefinition p = parameters.get(i);

            if (i != 0) {
                s.append(", ");
            }

            TypeReference parameterType = p.getParameterType();

            while (parameterType.isWildcardType()) {
                parameterType = parameterType.getExtendsBound();
            }

            if (parameterType.isGenericParameter()) {
                s.append(parameterType.getName());
            }
            else {
                s = parameterType.appendBriefDescription(s);
            }
        }

        s.append(')');

        return s;
    }

    public StringBuilder appendErasedDescription(final StringBuilder sb) {
        if (hasGenericParameters() && !isGenericDefinition()) {
            final MethodDefinition definition = resolve();
            if (definition != null) {
                return definition.appendErasedDescription(sb);
            }
        }

        for (final javax.lang.model.element.Modifier modifier : Flags.asModifierSet(getModifiers() & ~Flags.ACC_VARARGS)) {
            sb.append(modifier.toString());
            sb.append(' ');
        }

        final List<ParameterDefinition> parameterTypes = getParameters();

        StringBuilder s = getReturnType().appendErasedDescription(sb);

        s.append(' ');
        s.append(getName());
        s.append('(');

        for (int i = 0, n = parameterTypes.size(); i < n; ++i) {
            if (i != 0) {
                s.append(", ");
            }
            s = parameterTypes.get(i).getParameterType().appendErasedDescription(s);
        }

        s.append(')');
        return s;
    }

    @Override
    public String toString() {
        return getSimpleDescription();
    }

    // </editor-fold>
}
