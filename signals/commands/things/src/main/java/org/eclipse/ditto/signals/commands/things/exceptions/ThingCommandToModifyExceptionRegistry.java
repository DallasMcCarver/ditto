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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.signals.base.WithFeatureId;
import org.eclipse.ditto.signals.commands.base.AbstractCommandToExceptionRegistry;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.eclipse.ditto.signals.commands.things.modify.CreateThing;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttribute;
import org.eclipse.ditto.signals.commands.things.modify.DeleteAttributes;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeature;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureDefinition;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureDesiredProperties;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureDesiredProperty;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperties;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatureProperty;
import org.eclipse.ditto.signals.commands.things.modify.DeleteFeatures;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThing;
import org.eclipse.ditto.signals.commands.things.modify.DeleteThingDefinition;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttribute;
import org.eclipse.ditto.signals.commands.things.modify.ModifyAttributes;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeature;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDefinition;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDesiredProperties;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureDesiredProperty;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperties;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatureProperty;
import org.eclipse.ditto.signals.commands.things.modify.ModifyFeatures;
import org.eclipse.ditto.signals.commands.things.modify.ModifyPolicyId;
import org.eclipse.ditto.signals.commands.things.modify.ModifyThing;
import org.eclipse.ditto.signals.commands.things.modify.ModifyThingDefinition;


public final class ThingCommandToModifyExceptionRegistry
        extends AbstractCommandToExceptionRegistry<ThingCommand<?>, DittoRuntimeException> {

    private static final ThingCommandToModifyExceptionRegistry INSTANCE = createInstance();

    private ThingCommandToModifyExceptionRegistry(
            final Map<String, Function<ThingCommand<?>, DittoRuntimeException>> mappingStrategies) {
        super(mappingStrategies);
    }

    /**
     * Returns an instance of {@code ThingCommandToModifyExceptionRegistry}.
     *
     * @return the instance.
     */
    public static ThingCommandToModifyExceptionRegistry getInstance() {
        return INSTANCE;
    }

    private static ThingCommandToModifyExceptionRegistry createInstance() {
        final Map<String, Function<ThingCommand<?>, DittoRuntimeException>> mappingStrategies = new HashMap<>();

        // modify
        mappingStrategies.put(CreateThing.TYPE,
                command -> ThingNotModifiableException.newBuilder(command.getEntityId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(ModifyThing.TYPE,
                command -> ThingNotModifiableException.newBuilder(command.getEntityId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(DeleteThing.TYPE,
                command -> ThingNotDeletableException.newBuilder(command.getEntityId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(ModifyThingDefinition.TYPE,
                command -> ThingDefinitionNotModifiableException.newBuilder(command.getEntityId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(DeleteThingDefinition.TYPE,
                command -> ThingDefinitionNotModifiableException.newBuilder(command.getEntityId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(ModifyAttribute.TYPE,
                command -> AttributeNotModifiableException.newBuilder(command.getEntityId(),
                        command.getResourcePath())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(DeleteAttribute.TYPE,
                command -> AttributeNotModifiableException.newBuilder(command.getEntityId(),
                        command.getResourcePath())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(ModifyAttributes.TYPE,
                command -> AttributesNotModifiableException.newBuilder(command.getEntityId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(DeleteAttributes.TYPE,
                command -> AttributesNotModifiableException.newBuilder(command.getEntityId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(ModifyFeature.TYPE,
                command -> FeatureNotModifiableException.newBuilder(command.getEntityId(),
                        ((WithFeatureId) command).getFeatureId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(DeleteFeature.TYPE,
                command -> FeatureNotModifiableException.newBuilder(command.getEntityId(),
                        ((WithFeatureId) command).getFeatureId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(ModifyFeatures.TYPE,
                command -> FeaturesNotModifiableException.newBuilder(command.getEntityId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(DeleteFeatures.TYPE,
                command -> FeaturesNotModifiableException.newBuilder(command.getEntityId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(ModifyFeatureDefinition.TYPE,
                command -> FeatureDefinitionNotModifiableException.newBuilder(command.getEntityId(),
                        ((WithFeatureId) command).getFeatureId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(ModifyFeatureProperties.TYPE,
                command -> FeaturePropertiesNotModifiableException.newBuilder(command.getEntityId(),
                        ((WithFeatureId) command).getFeatureId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(ModifyFeatureProperty.TYPE,
                command -> FeaturePropertyNotModifiableException.newBuilder(command.getEntityId(),
                        ((WithFeatureId) command).getFeatureId(), command.getResourcePath())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(ModifyFeatureDesiredProperties.TYPE,
                command -> FeatureDesiredPropertiesNotModifiableException.newBuilder(command.getEntityId(),
                        ((WithFeatureId) command).getFeatureId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(ModifyFeatureDesiredProperty.TYPE,
                command -> FeatureDesiredPropertyNotModifiableException.newBuilder(command.getEntityId(),
                        ((WithFeatureId) command).getFeatureId(), command.getResourcePath())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(DeleteFeatureDefinition.TYPE,
                command -> FeatureDefinitionNotModifiableException.newBuilder(command.getEntityId(),
                        ((WithFeatureId) command).getFeatureId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(DeleteFeatureProperties.TYPE,
                command -> FeaturePropertiesNotModifiableException.newBuilder(command.getEntityId(),
                        ((WithFeatureId) command).getFeatureId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(DeleteFeatureProperty.TYPE,
                command -> FeaturePropertyNotModifiableException.newBuilder(command.getEntityId(),
                        ((WithFeatureId) command).getFeatureId(), command.getResourcePath())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(DeleteFeatureDesiredProperties.TYPE,
                command -> FeatureDesiredPropertiesNotModifiableException.newBuilder(command.getEntityId(),
                        ((WithFeatureId) command).getFeatureId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(DeleteFeatureDesiredProperty.TYPE,
                command -> FeatureDesiredPropertyNotModifiableException.newBuilder(command.getEntityId(),
                        ((WithFeatureId) command).getFeatureId(), command.getResourcePath())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());
        mappingStrategies.put(ModifyPolicyId.TYPE,
                command -> PolicyIdNotModifiableException.newBuilder(command.getEntityId())
                        .dittoHeaders(command.getDittoHeaders())
                        .build());

        return new ThingCommandToModifyExceptionRegistry(mappingStrategies);
    }

    @Override
    protected DittoRuntimeException fallback(final ThingCommand<?> command) {
        return ThingNotModifiableException.newBuilder(command.getEntityId())
                .dittoHeaders(command.getDittoHeaders())
                .build();
    }

}
