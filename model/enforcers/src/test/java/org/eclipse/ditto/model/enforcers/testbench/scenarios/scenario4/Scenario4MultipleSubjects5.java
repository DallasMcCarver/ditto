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
package org.eclipse.ditto.model.enforcers.testbench.scenarios.scenario4;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.ditto.model.base.auth.AuthorizationSubject;
import org.eclipse.ditto.model.enforcers.testbench.algorithms.PolicyAlgorithm;
import org.eclipse.ditto.model.enforcers.testbench.scenarios.Scenario;
import org.eclipse.ditto.model.enforcers.testbench.scenarios.ScenarioSetup;
import org.eclipse.ditto.model.policies.Permissions;
import org.eclipse.ditto.model.policies.PoliciesResourceType;
import org.eclipse.ditto.model.policies.SubjectId;
import org.eclipse.ditto.model.policies.SubjectIssuer;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;


@State(Scope.Benchmark)
public class Scenario4MultipleSubjects5 implements Scenario4MultipleSubjects {

    private final ScenarioSetup setup;

    public Scenario4MultipleSubjects5() {
        final String resource = "/features/foo1";
        setup = Scenario.newScenarioSetup(
                false,
                "Subject_1 has READ+WRITE granted on '/'. " + "Subject_3 has WRITE revoked on '/'. "
                        + "Subject_3 has READ+WRITE granted on '/attributes'. "
                        + "Subject_3 has READ+WRITE granted on '/features'. " +
                        "Subject_3 has READ revoked on '" + resource + "'. "
                        + "Subject_3 has WRITE revoked on '/features/foo2'. "
                        + "Subject_3 has READ+WRITE revoked on '/features/foo3'. "
                        + "Subject_3 has READ+WRITE revoked on '/attributes/nogo1'. "
                        + "Subject_3 has READ revoked on '/attributes/nogo2'. "
                        + "Subject_3 has WRITE revoked on '/attributes/nogo2'. "
                        + "Subject_3 has READ+WRITE granted on '/attributes/nogo1/go1'. "
                        + "Subject_3 has READ granted on '/attributes/nogo2/go2'. "
                        + "Subject_5 has READ granted on '/features/public/properties/location'. "
                        + "Subject_5 has WRITE granted on '/features/lamp/properties/config/on'. "
                        + "Is NOT able to READ '/features/foo1' with hasPermissionsOnResourceOrAnySubresource()",
                getPolicy(),
                Scenario.newAuthorizationContext(SUBJECT_1, SUBJECT_3, SUBJECT_5),
                resource,
                Collections.emptySet(),
                policyAlgorithm -> {
                    final Set<AuthorizationSubject> sids = policyAlgorithm.getSubjectsWithPartialPermission(
                            PoliciesResourceType.thingResource(resource), Permissions.newInstance("READ"));
                    return sids.contains(AuthorizationSubject.newInstance(createSubjectString(SUBJECT_1))) &&
                            !sids.contains(AuthorizationSubject.newInstance(createSubjectString(SUBJECT_3))) &&
                            !sids.contains(AuthorizationSubject.newInstance(createSubjectString(SUBJECT_5)));
                },
                "READ");
    }

    private static String createSubjectString(final CharSequence subjectId) {
        return SubjectId.newInstance(SubjectIssuer.GOOGLE, subjectId).toString();
    }

    @Override
    public ScenarioSetup getSetup() {
        return setup;
    }

    @Override
    public Function<PolicyAlgorithm, Boolean> getApplyAlgorithmFunction() {
        // algorithm invoked with hasPermissionsOnResourceOrAnySubresource! as we would like to know if the subject can read anywhere
        // in the hierarchy below the passed path:
        return algorithm -> algorithm.hasPermissionsOnResourceOrAnySubresource(getSetup());
    }

}
