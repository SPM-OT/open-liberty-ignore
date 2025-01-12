/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.microprofile.reactive.streams.operators.test;

import java.io.File;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;


import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import com.beust.jcommander.Parameters;

/**
 * We weave in the org.reactivestreams.example package. See issue https://github.com/eclipse/microprofile-reactive-streams-operators/issues/101
 */
public class ReactiveStreamsArchiveProcessor implements ApplicationArchiveProcessor {

    /* (non-Javadoc)
     * @see org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor#process(org.jboss.shrinkwrap.api.Archive, org.jboss.arquillian.test.spi.TestClass)
     */
    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        System.out.println("WLP: Processing archive: " + applicationArchive.toString());
        if (applicationArchive instanceof JavaArchive) {
            //Needed by some tck classes. E.G. org.reactivestreams.tck.flow.support.HelperPublisher
            ((JavaArchive) applicationArchive).addPackages(true, "org.reactivestreams.example");
        }
        System.out.println("WLP: final archive: " + applicationArchive.toString(true));
    }
}

