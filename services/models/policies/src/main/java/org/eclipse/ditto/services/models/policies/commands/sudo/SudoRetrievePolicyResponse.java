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
package org.eclipse.ditto.services.models.policies.commands.sudo;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonField;
import org.eclipse.ditto.json.JsonFieldDefinition;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.model.base.json.JsonParsableCommandResponse;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.eclipse.ditto.model.policies.PoliciesModelFactory;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.signals.base.SignalWithEntityId;
import org.eclipse.ditto.signals.commands.base.AbstractCommandResponse;
import org.eclipse.ditto.signals.commands.base.CommandResponseJsonDeserializer;

/**
 * Response to a {@link SudoRetrievePolicyResponse} command.
 */
@Immutable
@JsonParsableCommandResponse(type = SudoRetrievePolicyResponse.TYPE)
public final class SudoRetrievePolicyResponse extends AbstractCommandResponse<SudoRetrievePolicyResponse>
        implements SudoCommandResponse<SudoRetrievePolicyResponse>, SignalWithEntityId<SudoRetrievePolicyResponse> {

    /**
     * Type of this response.
     */
    public static final String TYPE = TYPE_PREFIX + SudoRetrievePolicy.NAME;

    static final JsonFieldDefinition<JsonObject> JSON_POLICY =
            JsonFactory.newJsonObjectFieldDefinition("payload/policy", FieldType.REGULAR, JsonSchemaVersion.V_2);

    private final PolicyId policyId;
    private final JsonObject policy;

    private SudoRetrievePolicyResponse(final PolicyId policyId,
            final HttpStatus httpStatus,
            final JsonObject policy,
            final DittoHeaders dittoHeaders) {

        super(TYPE, httpStatus, dittoHeaders);
        this.policyId = checkNotNull(policyId, "Policy ID");
        this.policy = checkNotNull(policy, "Policy");
    }

    /**
     * Creates a response to a {@code SudoRetrievePolicyResponse} command.
     *
     * @param policyId the Policy ID.
     * @param policy the retrieved Policy.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static SudoRetrievePolicyResponse of(final PolicyId policyId, final Policy policy,
            final DittoHeaders dittoHeaders) {

        return new SudoRetrievePolicyResponse(policyId, HttpStatus.OK,
                checkNotNull(policy, "Policy")
                        .toJson(dittoHeaders.getSchemaVersion().orElse(policy.getLatestSchemaVersion()),
                                FieldType.regularOrSpecial()),
                dittoHeaders);
    }

    /**
     * Creates a response to a {@code SudoRetrievePolicyResponse} command.
     *
     * @param policyId the Policy ID.
     * @param policy the retrieved Policy.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     */
    public static SudoRetrievePolicyResponse of(final PolicyId policyId, final JsonObject policy,
            final DittoHeaders dittoHeaders) {

        return new SudoRetrievePolicyResponse(policyId, HttpStatus.OK, policy, dittoHeaders);
    }

    /**
     * Creates a response to a {@code SudoRetrievePolicyResponse} command from a JSON string.
     *
     * @param jsonString the JSON string of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code jsonString} is empty.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonString} was not in the expected
     * format.
     */
    public static SudoRetrievePolicyResponse fromJson(final String jsonString, final DittoHeaders dittoHeaders) {
        return fromJson(JsonFactory.newObject(jsonString), dittoHeaders);
    }

    /**
     * Creates a response to a {@code SudoRetrievePolicyResponse} command from a JSON object.
     *
     * @param jsonObject the JSON object of which the response is to be created.
     * @param dittoHeaders the headers of the preceding command.
     * @return the response.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws org.eclipse.ditto.json.JsonParseException if the passed in {@code jsonObject} was not in the expected
     * format.
     */
    public static SudoRetrievePolicyResponse fromJson(final JsonObject jsonObject, final DittoHeaders dittoHeaders) {
        return new CommandResponseJsonDeserializer<SudoRetrievePolicyResponse>(TYPE, jsonObject).deserialize(
                httpStatus -> {
                    final var extractedPolicyId =
                            jsonObject.getValueOrThrow(SudoCommandResponse.JsonFields.JSON_POLICY_ID);
                    final var policyId = PolicyId.of(extractedPolicyId);
                    final var extractedPolicy = jsonObject.getValueOrThrow(JSON_POLICY);

                    return of(policyId, extractedPolicy, dittoHeaders);
                });
    }

    @Override
    public PolicyId getEntityId() {
        return policyId;
    }

    /**
     * Returns the retrieved Policy.
     *
     * @return the retrieved Policy.
     */
    public Policy getPolicy() {
        return PoliciesModelFactory.newPolicy(policy);
    }

    @Override
    public JsonValue getEntity(final JsonSchemaVersion schemaVersion) {
        return policy;
    }

    @Override
    public SudoRetrievePolicyResponse setEntity(final JsonValue entity) {
        checkNotNull(entity, "entity");
        return of(policyId, entity.asObject(), getDittoHeaders());
    }

    @Override
    public SudoRetrievePolicyResponse setDittoHeaders(final DittoHeaders dittoHeaders) {
        return of(policyId, policy, dittoHeaders);
    }

    @Override
    protected void appendPayload(final JsonObjectBuilder jsonObjectBuilder, final JsonSchemaVersion schemaVersion,
            final Predicate<JsonField> thePredicate) {

        final Predicate<JsonField> predicate = schemaVersion.and(thePredicate);
        jsonObjectBuilder.set(SudoCommandResponse.JsonFields.JSON_POLICY_ID, String.valueOf(policyId), predicate);
        jsonObjectBuilder.set(JSON_POLICY, policy, predicate);
    }

    @Override
    protected boolean canEqual(@Nullable final Object other) {
        return other instanceof SudoRetrievePolicyResponse;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SudoRetrievePolicyResponse that = (SudoRetrievePolicyResponse) o;
        return that.canEqual(this) &&
                Objects.equals(policyId, that.policyId) &&
                Objects.equals(policy, that.policy) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), policyId, policy);
    }

    @Override
    public String toString() {
        return super.toString() + "policyId=" + policyId + "policy=" + policy + "]";
    }

}
