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
package org.eclipse.ditto.services.gateway.security.authentication.jwt;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.policies.SubjectIssuer;

/**
 * Configuration for a {@link org.eclipse.ditto.model.policies.SubjectIssuer}.
 */
@Immutable
public final class JwtSubjectIssuerConfig {

    private final SubjectIssuer subjectIssuer;
    private final String issuer;
    private final List<String> authSubjectTemplates;

    private static final List<String> DEFAULT_AUTH_SUBJECT = Collections.singletonList("{{jwt:sub}}");

    /**
     * Constructs a new {@code JwtSubjectIssuerConfig}.
     *
     * @param subjectIssuer the subject issuer.
     * @param issuer the issuer.
     *
     */
    public JwtSubjectIssuerConfig(final SubjectIssuer subjectIssuer, final String issuer) {
        this(subjectIssuer, issuer, DEFAULT_AUTH_SUBJECT);
    }

    /**
     * Constructs a new {@code JwtSubjectIssuerConfig}.
     *
     * @param subjectIssuer the subject issuer.
     * @param issuer        the issuer.
     * @param authSubjectTemplates  the authorization subject templates
     *
     */
    public JwtSubjectIssuerConfig(final SubjectIssuer subjectIssuer, final String issuer, final List<String> authSubjectTemplates) {
        this.subjectIssuer = requireNonNull(subjectIssuer);
        this.issuer = requireNonNull(issuer);
        this.authSubjectTemplates = Collections.unmodifiableList(new ArrayList<>(requireNonNull(authSubjectTemplates)));
    }

    /**
     * Returns the issuer.
     *
     * @return the issuer.
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * Returns the subject issuer.
     *
     * @return the subject issuer
     */
    public SubjectIssuer getSubjectIssuer() {
        return subjectIssuer;
    }

    /**
     * Returns the authorization subject templates
     *
     * @return the authorization subject templates
     */
    public List<String> getAuthorizationSubjectTemplates() {
        return authSubjectTemplates;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final JwtSubjectIssuerConfig that = (JwtSubjectIssuerConfig) o;
        return Objects.equals(issuer, that.issuer) &&
                Objects.equals(subjectIssuer, that.subjectIssuer) &&
                Objects.equals(authSubjectTemplates, that.authSubjectTemplates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issuer, subjectIssuer, authSubjectTemplates);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "subjectIssuer=" + subjectIssuer +
                ", issuer=" + issuer +
                ", authSubjectTemplates=" + authSubjectTemplates +
                "]";
    }

}
