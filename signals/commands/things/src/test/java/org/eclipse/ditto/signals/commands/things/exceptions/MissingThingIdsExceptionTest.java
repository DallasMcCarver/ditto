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
package org.eclipse.ditto.signals.commands.things.exceptions;

import static org.eclipse.ditto.signals.commands.things.TestConstants.EMPTY_DITTO_HEADERS;
import static org.eclipse.ditto.signals.commands.things.TestConstants.Thing.MISSING_THING_IDS_EXCEPTION;
import static org.eclipse.ditto.signals.commands.things.assertions.ThingCommandAssertions.assertThat;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.net.URI;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.signals.base.GlobalErrorRegistry;
import org.junit.Test;

/**
 * Unit test for {@link MissingThingIdsException}.
 */
public final class MissingThingIdsExceptionTest {

    private static final JsonObject KNOWN_JSON = JsonFactory.newObjectBuilder()
            .set(DittoRuntimeException.JsonFields.STATUS, MissingThingIdsException.HTTP_STATUS.getCode())
            .set(DittoRuntimeException.JsonFields.ERROR_CODE, MissingThingIdsException.ERROR_CODE)
            .set(DittoRuntimeException.JsonFields.MESSAGE, MISSING_THING_IDS_EXCEPTION.getMessage())
            .set(DittoRuntimeException.JsonFields.DESCRIPTION,
                    MISSING_THING_IDS_EXCEPTION.getDescription().orElse(null),
                    JsonField.isValueNonNull())
            .set(DittoRuntimeException.JsonFields.HREF,
                    MISSING_THING_IDS_EXCEPTION.getHref().map(URI::toString).orElse(null),
                    JsonField.isValueNonNull())
            .build();


    @Test
    public void assertImmutability() {
        assertInstancesOf(MissingThingIdsException.class, areImmutable());
    }


    @Test
    public void toJsonReturnsExpected() {
        final JsonObject jsonObject = MISSING_THING_IDS_EXCEPTION.toJson();

        assertThat(jsonObject).isEqualTo(KNOWN_JSON);
    }


    @Test
    public void createInstanceFromValidJson() {
        final MissingThingIdsException underTest =
                MissingThingIdsException.fromJson(KNOWN_JSON, EMPTY_DITTO_HEADERS);

        assertThat(underTest).isEqualTo(MISSING_THING_IDS_EXCEPTION);
    }


    @Test
    public void checkThingErrorCodeWorks() {
        final DittoRuntimeException actual =
                GlobalErrorRegistry.getInstance().parse(KNOWN_JSON, EMPTY_DITTO_HEADERS);

        assertThat(actual).isEqualTo(MISSING_THING_IDS_EXCEPTION);
    }

}
