/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.services.models.placeholders;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.concurrent.Immutable;

/**
 * A unique pipeline element signifying deletion of the entire string containing the pipeline.
 */
@Immutable
final class PipelineElementDeleted implements PipelineElement {

    static final PipelineElement INSTANCE = new PipelineElementDeleted();

    private PipelineElementDeleted() {
        // no-op
    }

    @Override
    public Type getType() {
        return Type.DELETED;
    }

    @Override
    public PipelineElement onResolved(final Function<String, PipelineElement> stringProcessor) {
        return this;
    }

    @Override
    public PipelineElement onUnresolved(final Supplier<PipelineElement> nextPipelineElement) {
        return this;
    }

    @Override
    public PipelineElement onDeleted(final Supplier<PipelineElement> nextPipelineElement) {
        return nextPipelineElement.get();
    }

    @Override
    public <T> T accept(final PipelineElementVisitor<T> visitor) {
        return visitor.deleted();
    }

    @Override
    public Iterator<String> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
