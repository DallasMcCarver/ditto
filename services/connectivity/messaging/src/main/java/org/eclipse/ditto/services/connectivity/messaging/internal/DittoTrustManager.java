/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial contribution
 */
package org.eclipse.ditto.services.connectivity.messaging.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Trust manager with identity verification. The check succeeds if the expected hostname is the common name (CN) or a
 * subject alternative DNS name in the certificate (including wildcard match), or the expected ip is a subject
 * alternative IP address, or the server certificate is equal to a certificate in the trust store.
 */
final class DittoTrustManager implements X509TrustManager {

    // overly inclusive ipv4 pattern that excludes all valid DNS names
    private static final Pattern IPV4PATTERN =
            Pattern.compile("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");

    // overly inclusive ipv6 pattern that excludes all valid DNS names
    private static final Pattern IPV6PATTERN = Pattern.compile("\\[[0-9A-Fa-f:.]{1,39}]");

    private final X509TrustManager delegate;
    private final String hostnameOrIp;
    private final boolean isIp;
    @Nullable private final String leftMostSubdomain;

    private DittoTrustManager(final X509TrustManager delegate, final String hostnameOrIp) {
        this.delegate = delegate;
        this.hostnameOrIp = hostnameOrIp;
        isIp = shouldTreatAsIpAddress(hostnameOrIp);
        leftMostSubdomain = isIp ? null : extractSubdomain(hostnameOrIp).orElse(null);
    }

    /**
     * Create an array of {@code DittoTrustManager} from other trust managers.
     *
     * @param trustManagers trust managers to convert.
     * @param hostnameOrIp expected hostname or IP address in URI-embedded format.
     * @return array of trust managers such that all X509 trust managers are converted to Ditto trust managers.
     */
    static TrustManager[] wrapTrustManagers(final TrustManager[] trustManagers,
            @Nullable final String hostnameOrIp) {

        if (hostnameOrIp != null) {
            return Arrays.stream(trustManagers)
                    .map(trustManager -> trustManager instanceof X509TrustManager
                            ? new DittoTrustManager((X509TrustManager) trustManager, hostnameOrIp)
                            : trustManager)
                    .toArray(TrustManager[]::new);
        } else {
            return trustManagers;
        }
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String s) throws CertificateException {
        delegate.checkClientTrusted(chain, s);
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {

        // verify certificate chain
        delegate.checkServerTrusted(chain, authType);

        // verify hostname
        if (chain.length <= 0) {
            throw new CertificateException("Cannot verify hostname - empty certificate chain");
        }
        final X509Certificate serverCertificate = chain[0];
        if (!isServerCertificateInTrustStore(serverCertificate) && shouldRejectHostnameOrIp(serverCertificate)) {
            final String message = String.format("Host '%s' does not match signed hosts '%s' ",
                    hostnameOrIp,
                    Stream.concat(getSignedHostnames(serverCertificate), getSignedIps(serverCertificate))
                            .collect(Collectors.joining("', '")));
            throw new CertificateException(message);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return delegate.getAcceptedIssuers();
    }

    private boolean isServerCertificateInTrustStore(final X509Certificate serverCertificate) {
        return Arrays.stream(delegate.getAcceptedIssuers()).anyMatch(serverCertificate::equals);
    }

    private boolean shouldRejectHostnameOrIp(final X509Certificate serverCertificate) {
        if (isIp) {
            return shouldRejectIpAddress(serverCertificate, hostnameOrIp);
        } else {
            return shouldRejectHostname(serverCertificate, hostnameOrIp, leftMostSubdomain);
        }
    }

    private static boolean shouldRejectIpAddress(final X509Certificate certificate, final String expectedIpString) {
        try {
            final InetAddress expectedIp = InetAddress.getByName(expectedIpString);
            final Stream<InetAddress> signedIps = getSignedIps(certificate).flatMap(ip -> {
                try {
                    return Stream.of(InetAddress.getByName(ip));
                } catch (final UnknownHostException e) {
                    return Stream.empty();
                }
            });
            return signedIps.noneMatch(expectedIp::equals);
        } catch (final UnknownHostException e) {
            return true;
        }
    }

    private static boolean shouldRejectHostname(final X509Certificate certificate, final String expectedHostname,
            @Nullable final String leftMostSubdomain) {
        return getSignedHostnames(certificate)
                .map(extractedHostname -> replaceWildCard(extractedHostname, leftMostSubdomain))
                .noneMatch(expectedHostname::equalsIgnoreCase);
    }

    /**
     * Replaces the wildcard character (*) at the first position in the name extracted from the certificate with the
     * left-most part of the subdomain from the expected hostname. Wildcard is only replaced if it's the first
     * character as RFC6125 recommends (see https://tools.ietf.org/search/rfc6125#section-6.4.3).
     *
     * @param extractedHostname a hostname entry from the certificate that may contain the wildcard (*) e.g.
     * *.eclipse.org
     * @param leftMostSubdomain the left-most subdomain part e.g. ditto from ditto.eclipse.org
     * @return the extracted hostname, with the wildcard (*) replaced if it is the first character (*.eclipse.org,
     * NOT ditto.*.eclipse.org) AND the expected hostname contains a subdomain e.g. ditto.eclipse.org,
     * otherwise the hostname is not touched
     */
    private static String replaceWildCard(final String extractedHostname, @Nullable final String leftMostSubdomain) {
        if (leftMostSubdomain != null && extractedHostname.startsWith("*")) {
            return new StringBuilder(extractedHostname).replace(0, 1, leftMostSubdomain).toString();
        } else {
            return extractedHostname;
        }
    }

    private static Stream<String> getSignedHostnames(final X509Certificate certificate) {
        final Integer dNSNameFlag = 2;
        return Stream.concat(getCommonNames(certificate), getSubjectAltNames(certificate, dNSNameFlag::equals));
    }

    private static Stream<String> getSignedIps(final X509Certificate certificate) {
        final Integer ipFlag = 7;
        return getSubjectAltNames(certificate, ipFlag::equals);
    }

    private static Stream<String> getSubjectAltNames(final X509Certificate certificate,
            final Predicate<Object> flagPredicate) {

        try {
            final Stream<List<?>> subjectAltNames =
                    Optional.ofNullable(certificate.getSubjectAlternativeNames())
                            .map(Collection::stream)
                            .orElseGet(Stream::empty);
            return subjectAltNames.filter(entry -> entry.size() == 2 && flagPredicate.test(entry.get(0)))
                    .map(entry -> entry.get(1).toString());
        } catch (final CertificateParsingException e) {
            // server certificate unreadable
            return Stream.empty();
        }

    }

    private static Stream<String> getCommonNames(final X509Certificate serverCertificate) {
        try {
            final String subjectDN = serverCertificate.getSubjectX500Principal().getName();
            final LdapName ldapDN = new LdapName(subjectDN);
            return ldapDN.getRdns()
                    .stream()
                    .filter(rdn -> Objects.equals("CN", rdn.getType()))
                    .map(rdn -> rdn.getValue().toString());
        } catch (final InvalidNameException e) {
            // DN unreadable
            return Stream.empty();
        }
    }

    /**
     * Roughly check whether the host string of an URI should be treated as IP without querying DNS. Guaranteed to
     * return {@code true} for all valid URI-embedded IP addresses and {@code false} for all valid DNS names.
     *
     * @param hostnameOrIp host string of an URI.
     * @return whether the string is an invalid DNS name.
     */
    private static boolean shouldTreatAsIpAddress(final String hostnameOrIp) {
        return IPV4PATTERN.matcher(hostnameOrIp).matches() || IPV6PATTERN.matcher(hostnameOrIp).matches();
    }

    private static Optional<String> extractSubdomain(final String hostname) {
        final String[] split = hostname.split("\\.");
        if (split.length > 2 && split[0].length() > 0) {
            return Optional.of(split[0]);
        } else {
            return Optional.empty();
        }
    }
}
