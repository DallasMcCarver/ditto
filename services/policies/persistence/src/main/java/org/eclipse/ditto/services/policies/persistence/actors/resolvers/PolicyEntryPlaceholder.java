/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.services.policies.persistence.actors.resolvers;

import org.eclipse.ditto.services.models.placeholders.Placeholder;
import org.eclipse.ditto.model.policies.PolicyEntry;

/**
 * A {@link org.eclipse.ditto.services.models.placeholders.Placeholder} requiring a {@link PolicyEntry} to resolve information of an policy entry.
 *
 * @since 2.0.0
 */
public interface PolicyEntryPlaceholder extends Placeholder<PolicyEntry> {
}
