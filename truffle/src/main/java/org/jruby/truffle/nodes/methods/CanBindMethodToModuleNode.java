/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.methods;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;

import org.jruby.truffle.nodes.RubyNode;
import org.jruby.truffle.runtime.ModuleOperations;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyBasicObject;
import org.jruby.truffle.runtime.methods.InternalMethod;

/**
 * Caches {@link ModuleOperations#canBindMethodTo} for a method.
 */
@NodeChildren({
        @NodeChild("method"),
        @NodeChild("module")
})
public abstract class CanBindMethodToModuleNode extends RubyNode {

    public CanBindMethodToModuleNode(RubyContext context, SourceSection sourceSection) {
        super(context, sourceSection);
    }

    public abstract boolean executeCanBindMethodToModule(InternalMethod method, RubyBasicObject module);

    @Specialization(
            guards = { "isRubyModule(module)", "method.getDeclaringModule() == declaringModule", "module == cachedModule" },
            limit = "getCacheLimit()")
    protected boolean canBindMethodToCached(InternalMethod method, RubyBasicObject module,
            @Cached("method.getDeclaringModule()") RubyBasicObject declaringModule,
            @Cached("module") RubyBasicObject cachedModule,
            @Cached("canBindMethodTo(declaringModule, cachedModule)") boolean canBindMethodTo) {
        return canBindMethodTo;
    }

    @Specialization(guards = "isRubyModule(module)")
    protected boolean canBindMethodToUncached(InternalMethod method, RubyBasicObject module) {
        final RubyBasicObject declaringModule = method.getDeclaringModule();
        return canBindMethodTo(declaringModule, module);
    }

    protected boolean canBindMethodTo(RubyBasicObject declaringModule, RubyBasicObject module) {
        return ModuleOperations.canBindMethodTo(declaringModule, module);
    }

}
