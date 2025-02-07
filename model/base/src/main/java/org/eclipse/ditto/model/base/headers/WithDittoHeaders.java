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
package org.eclipse.ditto.model.base.headers;

/**
 * Common interface for all classes which have {@link DittoHeaders} available.
 */
public interface WithDittoHeaders {

    /**
     * Returns the {@link DittoHeaders} which are associated with this object.
     *
     * @return the DittoHeaders of this object.
     */
    DittoHeaders getDittoHeaders();

}
