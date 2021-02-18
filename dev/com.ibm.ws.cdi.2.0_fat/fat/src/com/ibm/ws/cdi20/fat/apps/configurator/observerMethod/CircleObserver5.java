/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.cdi20.fat.apps.configurator.observerMethod;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.ObserverMethod;

@ApplicationScoped
public class CircleObserver5 {

    public void observer(@Observes @Priority(ObserverMethod.DEFAULT_PRIORITY + 5) Circle circle) {
        ObserverMethodConfiguratorTest.circleObservations.add(5);
    }
}