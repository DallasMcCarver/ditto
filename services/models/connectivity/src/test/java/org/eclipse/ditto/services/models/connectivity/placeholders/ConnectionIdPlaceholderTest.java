 /*
  * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
 package org.eclipse.ditto.services.models.connectivity.placeholders;

 import static org.assertj.core.api.Assertions.assertThat;

 import java.util.Collections;

 import org.eclipse.ditto.model.connectivity.ConnectionId;
 import org.eclipse.ditto.services.models.placeholders.ExpressionResolver;
 import org.eclipse.ditto.services.models.placeholders.PlaceholderFactory;
 import org.eclipse.ditto.services.models.placeholders.PlaceholderResolver;
 import org.junit.Test;

 public final class ConnectionIdPlaceholderTest {


     @Test
     public void testExpressionResolver() {

         final ConnectionId connectionId = ConnectionId.generateRandom();

         final PlaceholderResolver<ConnectionId> connectionIdResolver = PlaceholderFactory.newPlaceholderResolver(
                 ConnectivityPlaceholders.newConnectionIdPlaceholder(), connectionId);

         final ExpressionResolver underTest =
                 PlaceholderFactory.newExpressionResolver(Collections.singletonList(connectionIdResolver));

         assertThat(underTest.resolve("{{ connection:id }}"))
                 .contains(connectionId.toString());
     }

     @Test
     public void testPlaceholderResolver() {
         final ConnectionId connectionId = ConnectionId.generateRandom();

         final PlaceholderResolver<ConnectionId> underTest = PlaceholderFactory.newPlaceholderResolver(
                 ConnectivityPlaceholders.newConnectionIdPlaceholder(), connectionId);

         assertThat(underTest.resolve("id"))
                 .contains(connectionId.toString());
     }
 }
