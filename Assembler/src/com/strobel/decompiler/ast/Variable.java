/*
 * Variable.java
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

package com.strobel.decompiler.ast;

import com.strobel.assembler.metadata.ParameterDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.assembler.metadata.VariableDefinition;

public final class Variable {
    private String _name;
    private boolean _isGenerated;
    private TypeReference _type;
    private VariableDefinition _originalVariable;
    private ParameterDefinition _originalParameter;

    public final String getName() {
        return _name;
    }

    public final void setName(final String name) {
        _name = name;
    }

    public final boolean isGenerated() {
        return _isGenerated;
    }

    public final void setGenerated(final boolean generated) {
        _isGenerated = generated;
    }

    public final TypeReference getType() {
        return _type;
    }

    public final void setType(final TypeReference type) {
        _type = type;
    }

    public final VariableDefinition getOriginalVariable() {
        return _originalVariable;
    }

    public final void setOriginalVariable(final VariableDefinition originalVariable) {
        _originalVariable = originalVariable;
    }

    public final ParameterDefinition getOriginalParameter() {
        return _originalParameter;
    }

    public final void setOriginalParameter(final ParameterDefinition originalParameter) {
        _originalParameter = originalParameter;
    }
}