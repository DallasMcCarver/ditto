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
package org.eclipse.ditto.services.models.policies;

import static java.util.Objects.requireNonNull;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.ditto.model.base.common.Validator;
import org.eclipse.ditto.model.policies.PoliciesResourceType;
import org.eclipse.ditto.model.policies.PolicyEntry;
import org.eclipse.ditto.model.policies.ResourceKey;
import org.eclipse.ditto.model.policies.Subject;
import org.eclipse.ditto.model.policies.Subjects;

/**
 * Instances of this class can be used to validate a specified {@link org.eclipse.ditto.model.policies.Policy} with
 * minimum required permissions.
 */
public final class PoliciesValidator implements Validator {

    private static final ResourceKey ROOT_RESOURCE = PoliciesResourceType.policyResource("/");

    private static final String NO_AUTH_SUBJECT_PATTERN =
            "It must contain at least one permanent Subject with permission(s) <{0}> on resource <{1}>!";

    private final Iterable<PolicyEntry> policyEntries;
    private boolean validationResult;
    private String reason;

    private PoliciesValidator(final Iterable<PolicyEntry> policyEntries) {
        this.policyEntries = policyEntries;
        validationResult = true;
        reason = null;
    }

    /**
     * Creates a new {@code PoliciesValidator} instance.
     *
     * @param policyEntries the policyEntries to be validated.
     * @return a new {@code PoliciesValidator} object.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static PoliciesValidator newInstance(final Iterable<PolicyEntry> policyEntries) {
        requireNonNull(policyEntries, "The policyEntries to validate must not be null!");

        return new PoliciesValidator(policyEntries);
    }

    @Override
    public boolean isValid() {
        // Disregard expiring subjects when testing for permissions granted because those are deleted after some time.
        final Set<Subject> withPermissionGranted = StreamSupport.stream(policyEntries.spliterator(), false)
                .filter(this::hasPermissionGranted)
                .map(PolicyEntry::getSubjects)
                .flatMap(Subjects::stream)
                .filter(subject -> subject.getExpiry().isEmpty())
                .collect(Collectors.toSet());

        final Set<Subject> withPermissionRevoked = StreamSupport.stream(policyEntries.spliterator(), false)
                .filter(this::hasPermissionRevoked)
                .map(PolicyEntry::getSubjects)
                .flatMap(Subjects::stream)
                .collect(Collectors.toSet());

        withPermissionGranted.removeAll(withPermissionRevoked);

        validationResult = !withPermissionGranted.isEmpty();

        if (!validationResult) {
            reason = MessageFormat.format(NO_AUTH_SUBJECT_PATTERN, Permission.MIN_REQUIRED_POLICY_PERMISSIONS,
                    ROOT_RESOURCE);
        }

        return validationResult;
    }

    private boolean hasPermissionGranted(final PolicyEntry policyEntry) {
        return policyEntry.getResources().stream()
                .anyMatch(resource -> {
                    final boolean isRootResource = ROOT_RESOURCE.equals(resource.getResourceKey());
                    final boolean containsGrantedPermissions = resource.getEffectedPermissions()
                            .getGrantedPermissions()
                            .contains(Permission.MIN_REQUIRED_POLICY_PERMISSIONS);

                    return isRootResource && containsGrantedPermissions;
                });
    }

    private boolean hasPermissionRevoked(final PolicyEntry policyEntry) {
        return policyEntry.getResources().stream()
                .anyMatch(resource -> {
                    final boolean isRootResource = ROOT_RESOURCE.equals(resource.getResourceKey());
                    final boolean containsRevokedPermissions = resource.getEffectedPermissions()
                            .getRevokedPermissions()
                            .contains(Permission.MIN_REQUIRED_POLICY_PERMISSIONS);

                    return isRootResource && containsRevokedPermissions;
                });
    }

    @Override
    public Optional<String> getReason() {
        return Optional.ofNullable(reason);
    }
}
