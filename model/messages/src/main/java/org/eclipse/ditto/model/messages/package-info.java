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

/**
 * Contains the Messages framework around the cornerstone of this package: {@link org.eclipse.ditto.model.messages.Message}.
 * A Message is sent <em>FROM</em> or <em>TO</em> a {@code Thing} or a {@code Feature}.
 *
 * <h3>Object creation</h3>
 * {@link org.eclipse.ditto.model.messages.MessagesModelFactory} is the main entry point for obtaining objects of this
 * package's interfaces to work with.
 */
@org.eclipse.ditto.utils.jsr305.annotations.AllValuesAreNonnullByDefault
package org.eclipse.ditto.model.messages;
