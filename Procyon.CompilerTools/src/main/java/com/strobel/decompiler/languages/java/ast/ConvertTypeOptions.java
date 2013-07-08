/*
 * ConvertTypeOptions.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is based on Mono.Cecil from Jb Evain, Copyright (c) Jb Evain;
 * and ILSpy/ICSharpCode from SharpDevelop, Copyright (c) AlphaSierraPapa.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.decompiler.languages.java.ast;

public final class ConvertTypeOptions {
    private boolean _includePackage/* = true*/;
    private boolean _includeTypeParameterDefinitions = true;

    public ConvertTypeOptions() {
    }

    public ConvertTypeOptions(final boolean includePackage, final boolean includeTypeParameterDefinitions) {
        _includePackage = includePackage;
        _includeTypeParameterDefinitions = includeTypeParameterDefinitions;
    }

    public boolean getIncludePackage() {
        return _includePackage;
    }

    public void setIncludePackage(final boolean value) {
        _includePackage = value;
    }

    public boolean getIncludeTypeParameterDefinitions() {
        return _includeTypeParameterDefinitions;
    }

    public void setIncludeTypeParameterDefinitions(final boolean value) {
        _includeTypeParameterDefinitions = value;
    }
}
