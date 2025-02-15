/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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

import java.util.Optional;

/**
 * Definition of a placeholder expression in the format {@code prefix:name}.
 *
 * @param <T> the type which is required to resolve a placeholder
 */
public interface Placeholder<T> extends Expression {

    /**
     * Resolves the placeholder variable by name.
     *
     * @param placeholderSource the source from which to the placeholder is resolved, e.g. a Thing id.
     * @param name the placeholder variable name (i. e., the part after ':').
     * @return value of the placeholder variable if the placeholder name is supported, or an empty optional otherwise.
     */
    Optional<String> resolve(T placeholderSource, String name);
}
