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
 package org.eclipse.ditto.services.connectivity.messaging;

 import java.util.Optional;

 import javax.annotation.Nullable;

 import org.eclipse.ditto.model.base.headers.DittoHeaders;
 import org.eclipse.ditto.model.base.headers.DittoHeadersSettable;
 import org.eclipse.ditto.signals.commands.base.CommandResponse;

 /**
  * The result of a published message holding an optional command response (which also can be an acknowledgement).
  */
 public final class SendResult implements DittoHeadersSettable<SendResult> {

     @Nullable private final CommandResponse<?> commandResponse;
     private final DittoHeaders dittoHeaders;

     public SendResult(@Nullable final CommandResponse<?> commandResponse, final DittoHeaders dittoHeaders) {
         this.commandResponse = commandResponse;
         this.dittoHeaders = dittoHeaders;
     }

     @Override
     public SendResult setDittoHeaders(final DittoHeaders dittoHeaders) {
         return new SendResult(commandResponse, dittoHeaders);
     }

     public Optional<CommandResponse<?>> getCommandResponse() {
         return Optional.ofNullable(commandResponse);
     }

     @Override
     public DittoHeaders getDittoHeaders() {
         return dittoHeaders;
     }

 }
