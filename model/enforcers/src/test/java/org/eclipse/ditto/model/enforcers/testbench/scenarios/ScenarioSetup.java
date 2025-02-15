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
package org.eclipse.ditto.model.enforcers.testbench.scenarios;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.auth.AuthorizationContext;
import org.eclipse.ditto.model.base.auth.AuthorizationSubject;
import org.eclipse.ditto.model.base.json.Jsonifiable;
import org.eclipse.ditto.model.enforcers.testbench.algorithms.PolicyAlgorithm;
import org.eclipse.ditto.model.policies.Permissions;
import org.eclipse.ditto.model.policies.Policy;


public final class ScenarioSetup {

    private final Policy policy;
    private final AuthorizationContext authorizationContext;
    private final JsonPointer resource;
    private final String type;
    private final Permissions requiredPermissions;
    private final boolean expectedResult;
    private final Jsonifiable.WithFieldSelectorAndPredicate<JsonField> fullJsonifiable;
    private final JsonObject expectedJsonView;
    private final Set<AuthorizationSubject> expectedSubjects;
    private final String description;
    private final Function<PolicyAlgorithm, Boolean> additionalAlgorithmFunction;

    public ScenarioSetup(final Policy policy,
            final AuthorizationContext authorizationContext,
            final JsonPointer resource,
            final String type,
            final Permissions requiredPermissions,
            final boolean expectedResult,
            final String description,
            final Set<AuthorizationSubject> expectedSubjects) {

        this(policy, authorizationContext, resource, type, requiredPermissions, expectedResult, null, null, description,
                expectedSubjects, null);
    }

    public ScenarioSetup(final Policy policy,
            final AuthorizationContext authorizationContext,
            final JsonPointer resource,
            final String type,
            final Permissions requiredPermissions,
            final boolean expectedResult,
            final String description,
            final Set<AuthorizationSubject> expectedSubjects,
            final Function<PolicyAlgorithm, Boolean> additionalAlgorithmFunction) {

        this(policy, authorizationContext, resource, type, requiredPermissions, expectedResult, null, null, description,
                expectedSubjects, additionalAlgorithmFunction);
    }

    public ScenarioSetup(final Policy policy,
            final AuthorizationContext authorizationContext,
            final JsonPointer resource,
            final String type,
            final Permissions requiredPermissions,
            final boolean expectedResult,
            final Jsonifiable.WithFieldSelectorAndPredicate<JsonField> fullJsonifiable,
            final JsonObject expectedJsonView,
            final String description,
            final Set<AuthorizationSubject> expectedSubjects) {

        this(policy, authorizationContext, resource, type, requiredPermissions, expectedResult, fullJsonifiable,
                expectedJsonView, description, expectedSubjects, null);
    }

    public ScenarioSetup(final Policy policy,
            final AuthorizationContext authorizationContext,
            final JsonPointer resource,
            final String type,
            final Permissions requiredPermissions,
            final boolean expectedResult,
            final Jsonifiable.WithFieldSelectorAndPredicate<JsonField> fullJsonifiable,
            final JsonObject expectedJsonView,
            final String description,
            final Set<AuthorizationSubject> expectedSubjects,
            final Function<PolicyAlgorithm, Boolean> additionalAlgorithmFunction) {

        this.policy = policy;
        this.authorizationContext = authorizationContext;
        this.resource = resource;
        this.requiredPermissions = requiredPermissions;
        this.expectedResult = expectedResult;
        this.fullJsonifiable = fullJsonifiable;
        this.description = description;
        this.expectedJsonView = expectedJsonView;
        this.expectedSubjects = expectedSubjects;
        this.type = type;
        this.additionalAlgorithmFunction = additionalAlgorithmFunction;
    }

    public Policy getPolicy() {
        return policy;
    }

    public AuthorizationContext getAuthorizationContext() {
        return authorizationContext;
    }

    public JsonPointer getResource() {
        return resource;
    }

    public Permissions getRequiredPermissions() {
        return requiredPermissions;
    }

    public boolean getExpectedResult() {
        return expectedResult;
    }

    public Optional<Jsonifiable.WithFieldSelectorAndPredicate<JsonField>> getFullJsonifiable() {
        return Optional.ofNullable(fullJsonifiable);
    }

    public Optional<Set<AuthorizationSubject>> getExpectedSubjects() {
        return Optional.ofNullable(expectedSubjects);
    }

    public Optional<JsonObject> getExpectedJsonView() {
        return Optional.ofNullable(expectedJsonView);
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public Optional<Function<PolicyAlgorithm, Boolean>> getAdditionalAlgorithmFunction() {
        return Optional.ofNullable(additionalAlgorithmFunction);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + "policy=\n" + policy.toJsonString() +
                ",\nauthorizationContext=" + authorizationContext +
                ",\nresource=" + resource +
                ",\nrequiredPermissions=" + requiredPermissions +
                ",\nexpectedResult=" + expectedResult +
                ",\nfullJsonifiable=" + fullJsonifiable +
                ",\nexpectedJsonView=" + expectedJsonView +
                ",\ndescription=" + description + "]";
    }

}
