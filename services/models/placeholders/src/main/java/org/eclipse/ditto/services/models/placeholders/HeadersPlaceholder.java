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

import java.util.Map;

/**
 * A {@link Placeholder} that requires a {@code Map<String, String>} to resolve its placeholders.
 */
public interface HeadersPlaceholder extends Placeholder<Map<String, String>> {
}
