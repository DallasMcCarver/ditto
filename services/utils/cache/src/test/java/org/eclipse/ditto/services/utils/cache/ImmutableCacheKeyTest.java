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
package org.eclipse.ditto.services.utils.cache;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.util.Objects;

import javax.annotation.Nullable;

import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.base.entity.type.EntityType;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Tests {@link ImmutableCacheKey}.
 */
public class ImmutableCacheKeyTest {

    private static final EntityType THING_TYPE = EntityType.of("thing");
    private static final EntityId ENTITY_ID = EntityId.of(THING_TYPE, "entity:id");
    private static final CacheKey CACHE_KEY = ImmutableCacheKey.of(ENTITY_ID);
    private static final String EXPECTED_SERIALIZED_ENTITY_ID =
            String.join(ImmutableCacheKey.DELIMITER, THING_TYPE, ENTITY_ID);

    @Test
    public void assertImmutability() {
        assertInstancesOf(ImmutableCacheKey.class,
                areImmutable(),
                provided(EntityId.class, CacheLookupContext.class).isAlsoImmutable());
    }

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImmutableCacheKey.class)
                .verify();
    }

    @Test
    public void testSerialization() {
        // check preconditions
        assertThat(CACHE_KEY).isNotNull();
        assertThat((CharSequence) CACHE_KEY.getId()).isEqualTo(ENTITY_ID);

        // assert serialization
        assertThat(CACHE_KEY.toString()).isEqualTo(EXPECTED_SERIALIZED_ENTITY_ID);
    }

    @Test
    public void testDeserialization() {
        assertThat(ImmutableCacheKey.readFrom(EXPECTED_SERIALIZED_ENTITY_ID)).isEqualTo(CACHE_KEY);
    }

    @Test
    public void testSerializationWithDifferentType() {
        final OtherEntityIdImplementation otherImplementation = new OtherEntityIdImplementation("entity:id");
        final CacheKey original = ImmutableCacheKey.of(otherImplementation);
        // as the entity id inside ImmutableEntityIdWithResourceType is updated to type default entity id, we have this
        // side-effect:
        assertThat((CharSequence) original.getId()).isNotEqualTo(otherImplementation);

        final String serialized = original.toString();
        final CacheKey deserialized = ImmutableCacheKey.readFrom(serialized);

        assertThat(deserialized).isEqualTo(original);
    }

    /**
     * Implementation of {@link EntityId} to support verifying serialization and deserialization are working properly.
     */
    private static class OtherEntityIdImplementation implements EntityId {

        private final String id;

        private OtherEntityIdImplementation(final String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " [" +
                    "id=" + id +
                    "]";
        }

        @Override
        public boolean equals(@Nullable final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final OtherEntityIdImplementation that = (OtherEntityIdImplementation) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public EntityType getEntityType() {
            return THING_TYPE;
        }
    }

}
