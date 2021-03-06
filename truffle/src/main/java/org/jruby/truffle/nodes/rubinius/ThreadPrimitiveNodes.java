/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.nodes.rubinius;

import static org.jruby.RubyThread.RUBY_MAX_THREAD_PRIORITY;
import static org.jruby.RubyThread.RUBY_MIN_THREAD_PRIORITY;
import static org.jruby.RubyThread.javaPriorityToRubyPriority;
import static org.jruby.RubyThread.rubyPriorityToJavaPriority;

import org.jruby.truffle.nodes.core.ThreadNodes;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.control.RaiseException;
import org.jruby.truffle.runtime.core.RubyBasicObject;
import org.jruby.truffle.runtime.subsystems.SafepointAction;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.SourceSection;

/**
 * Rubinius primitives associated with the Ruby {@code Thread} class.
 */
public class ThreadPrimitiveNodes {

    @RubiniusPrimitive(name = "thread_raise")
    public static abstract class ThreadRaisePrimitiveNode extends RubiniusPrimitiveNode {

        public ThreadRaisePrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization(guards = { "isRubyThread(thread)", "isRubyException(exception)" })
        public RubyBasicObject raise(RubyBasicObject thread, final RubyBasicObject exception) {
            getContext().getSafepointManager().pauseThreadAndExecuteLater(
                    ThreadNodes.getCurrentFiberJavaThread(thread),
                    this,
                    new SafepointAction() {
                        @Override
                        public void run(RubyBasicObject currentThread, Node currentNode) {
                            throw new RaiseException(exception);
                        }
                    });

            return nil();
        }

    }

    @RubiniusPrimitive(name = "thread_get_priority")
    public static abstract class ThreadGetPriorityPrimitiveNode extends RubiniusPrimitiveNode {
        public ThreadGetPriorityPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization(guards = "isRubyThread(thread)")
        public int getPriority(RubyBasicObject thread) {
            final Thread javaThread = ThreadNodes.getFields(thread).thread;
            if (javaThread != null) {
                int javaPriority = javaThread.getPriority();
                return javaPriorityToRubyPriority(javaPriority);
            } else {
                return ThreadNodes.getFields(thread).priority;
            }
        }
    }

    @RubiniusPrimitive(name = "thread_set_priority")
    public static abstract class ThreadSetPriorityPrimitiveNode extends RubiniusPrimitiveNode {
        public ThreadSetPriorityPrimitiveNode(RubyContext context, SourceSection sourceSection) {
            super(context, sourceSection);
        }

        @Specialization(guards = "isRubyThread(thread)")
        public int getPriority(RubyBasicObject thread, int rubyPriority) {
            if (rubyPriority < RUBY_MIN_THREAD_PRIORITY) {
                rubyPriority = RUBY_MIN_THREAD_PRIORITY;
            } else if (rubyPriority > RUBY_MAX_THREAD_PRIORITY) {
                rubyPriority = RUBY_MAX_THREAD_PRIORITY;
            }

            int javaPriority = rubyPriorityToJavaPriority(rubyPriority);
            final Thread javaThread = ThreadNodes.getFields(thread).thread;
            if (javaThread != null) {
                javaThread.setPriority(javaPriority);
            }
            ThreadNodes.getFields(thread).priority = rubyPriority;
            return rubyPriority;
        }
    }

}
