/*
 * CompilationTarget.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.reflection.emit;

import java.util.HashMap;
import java.util.Map;

/**
 * @author strobelm
 */
enum CompilationTarget {
    JDK1_1("1.1", 45, 3),
    JDK1_2("1.2", 46, 0),
    JDK1_3("1.3", 47, 0),

    /** J2SE1.4 = Merlin. */
    JDK1_4("1.4", 48, 0),

    /** Support for the JSR14 prototype compiler (targeting 1.4 VMs
     *  augmented with a few support classes).  This is a transitional
     *  option that will not be supported in the product.  */
    JSR14("jsr14", 48, 0),

    /** The following are undocumented transitional targets that we
     *  had used to test VM fixes in update releases.  We do not
     *  promise to retain support for them.  */
    JDK1_4_1("1.4.1", 48, 0),
    JDK1_4_2("1.4.2", 48, 0),

    /** Tiger. */
    JDK1_5("1.5", 49, 0),

    /** JDK 6. */
    JDK1_6("1.6", 50, 0),

    /** JDK 7. */
    JDK1_7("1.7", 51, 0);

    private static CompilationTarget MIN;
    public static CompilationTarget MIN() { return MIN; }

    private static final CompilationTarget MAX;
    public static CompilationTarget MAX() { return MAX; }

    private static final Map<String,CompilationTarget> tab = new HashMap<String,CompilationTarget>();
    static {
        CompilationTarget max = null;
        for (final CompilationTarget t : values()) {
            if (MIN == null) MIN = t;
            max = t;
            tab.put(t.name, t);
        }
        MAX = max;
        tab.put("5", JDK1_5);
        tab.put("6", JDK1_6);
        tab.put("7", JDK1_7);
    }

    public final String name;
    public final int majorVersion;
    public final int minorVersion;

    private CompilationTarget(final String name, final int majorVersion, final int minorVersion) {
        this.name = name;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    public static final CompilationTarget DEFAULT = JDK1_7;

    public static CompilationTarget lookup(final String name) {
        return tab.get(name);
    }

    /** In -target 1.1 and earlier, the compiler is required to emit
     *  synthetic method definitions in abstract classes for interface
     *  methods that are not overridden.  We call them "Miranda" methods.
     */
    public boolean requiresIproxy() {
        return compareTo(JDK1_1) <= 0;
    }

    /** Beginning in 1.4, we take advantage of the possibility of emitting
     *  code to initialize fields before calling the superclass constructor.
     *  This is allowed by the VM spec, but the verifier refused to allow
     *  it until 1.4.  This is necesary to translate some code involving
     *  inner classes.  See, for example, 4030374.
     */
    public boolean initializeFieldsBeforeSuper() {
        return compareTo(JDK1_4) >= 0;
    }

    /** Beginning with -target 1.2 we obey the JLS rules for binary
     *  compatibility, emitting as the qualifying type of a reference
     *  to a method or field the type of the qualifier.  In earlier
     *  targets we use as the qualifying type the class in which the
     *  member was found.  The following methods named
     *  *binaryCompatibility() indicate places where we vary from this
     *  general rule. */
    public boolean obeyBinaryCompatibility() {
        return compareTo(JDK1_2) >= 0;
    }

    /** Starting in 1.5, the compiler uses an array type as
     *  the qualifier for method calls (such as clone) where required by
     *  the language and VM spec.  Earlier versions of the compiler
     *  qualified them by Object.
     */
    public boolean arrayBinaryCompatibility() {
        return compareTo(JDK1_5) >= 0;
    }

    /** Beginning after 1.2, we follow the binary compatibility rules for
     *  interface fields.  The 1.2 VMs had bugs handling interface fields
     *  when compiled using binary compatibility (see 4400598), so this is
     *  an accommodation to them.
     */
    public boolean interfaceFieldsBinaryCompatibility() {
        return compareTo(JDK1_2) > 0;
    }

    /** Beginning in -target 1.5, we follow the binary compatibility
     *  rules for interface methods that redefine Object methods.
     *  Earlier VMs had bugs handling such methods compiled using binary
     *  compatibility (see 4392595, 4398791, 4392595, 4400415).
     *  The VMs were fixed during or soon after 1.4.  See 4392595.
     */
    public boolean interfaceObjectOverridesBinaryCompatibility() {
        return compareTo(JDK1_5) >= 0;
    }

    /** Beginning in -target 1.4.2, we make synthetic variables
     *  package-private instead of private.  This is to prevent the
     *  necessity of access methods, which effectively relax the
     *  protection of the field but bloat the class files and affect
     *  execution.
     */
    public boolean usePrivateSyntheticFields() {
        return compareTo(JDK1_4_2) < 0;
    }

    /** Sometimes we need to create a field to cache a value like a
     *  class literal of the assertions flag.  In -target 1.4.2 and
     *  later we create a new synthetic class for this instead of
     *  using the outermost class.  See 4401576.
     */
    public boolean useInnerCacheClass() {
        return compareTo(JDK1_4_2) >= 0;
    }

    /** Return true if cldc-style stack maps need to be generated. */
    public boolean generateCLDCStackmap() {
        return false;
    }

    /** Beginning in -target 6, we generate stackmap attribute in
     *  compact format. */
    public boolean generateStackMapTable() {
        return compareTo(JDK1_6) >= 0;
    }

    /** Beginning in -target 6, package-info classes are marked synthetic.
     */
    public boolean isPackageInfoSynthetic() {
        return compareTo(JDK1_6) >= 0;
    }

    /** Do we generate "empty" stackmap slots after double and long?
     */
    public boolean generateEmptyAfterBig() {
        return false;
    }

    /** Beginning in 1.5, we have an unsynchronized version of
     *  StringBuffer called StringBuilder that can be used by the
     *  compiler for string concatenation.
     */
    public boolean useStringBuilder() {
        return compareTo(JDK1_5) >= 0;
    }

    /** Beginning in 1.5, we have flag bits we can use instead of
     *  marker attributes.
     */
    public boolean useSyntheticFlag() {
        return compareTo(JDK1_5) >= 0;
    }
    public boolean useEnumFlag() {
        return compareTo(JDK1_5) >= 0;
    }
    public boolean useAnnotationFlag() {
        return compareTo(JDK1_5) >= 0;
    }
    public boolean useVarargsFlag() {
        return compareTo(JDK1_5) >= 0;
    }
    public boolean useBridgeFlag() {
        return compareTo(JDK1_5) >= 0;
    }

    /** Return the character to be used in constructing synthetic
     *  identifiers, where not specified by the JLS.
     */
    public char syntheticNameChar() {
        return '$';
    }

    /** Does the VM have direct support for class literals?
     */
    public boolean hasClassLiterals() {
        return compareTo(JDK1_5) >= 0;
    }

    /** Does the VM support an invokedynamic instruction?
     */
    public boolean hasInvokedynamic() {
        return compareTo(JDK1_7) >= 0;
    }

    /** Does the VM support polymorphic method handle invocation?
     *  Affects the linkage information output to the classfile.
     *  An alias for {@code hasInvokedynamic}, since all the JSR 292 features appear together.
     */
    public boolean hasMethodHandles() {
        return hasInvokedynamic();
    }

    /** Although we may not have support for class literals, should we
     *  avoid initializing the class that the literal refers to?
     *  See 4468823
     */
    public boolean classLiteralsNoInit() {
        return compareTo(JDK1_4_2) >= 0;
    }

    /** Although we may not have support for class literals, when we
     *  throw a NoClassDefFoundError, should we initialize its cause?
     */
    public boolean hasInitCause() {
        return compareTo(JDK1_4) >= 0;
    }

    /** For bootstrapping, we use J2SE1.4's wrapper class constructors
     *  to implement boxing.
     */
    public boolean boxWithConstructors() {
        return compareTo(JDK1_5) < 0;
    }

    /** For bootstrapping, we use J2SE1.4's java.util.Collection
     *  instead of java.lang.Iterable.
     */
    public boolean hasIterable() {
        return compareTo(JDK1_5) >= 0;
    }

    /** In J2SE1.5.0, we introduced the "EnclosingMethod" attribute
     *  for improved reflection support.
     */
    public boolean hasEnclosingMethodAttribute() {
        return compareTo(JDK1_5) >= 0 || this == JSR14;
    }
}
