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
 package org.eclipse.ditto.protocoladapter.provider;

 import org.eclipse.ditto.protocoladapter.Adapter;
 import org.eclipse.ditto.signals.base.Signal;
 import org.eclipse.ditto.signals.commands.base.CommandResponse;

 public interface RetrieveThingsCommandAdapterProvider<Q extends Signal<?>, R extends CommandResponse<?>> {

     /**
      * @return the query command adapter
      */
     Adapter<Q> getRetrieveThingsCommandAdapter();

     /**
      * @return the query command response adapter
      */
     Adapter<R> getRetrieveThingsCommandResponseAdapter();
 }
