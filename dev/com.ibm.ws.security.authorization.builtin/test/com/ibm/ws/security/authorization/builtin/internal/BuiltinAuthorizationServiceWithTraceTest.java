/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
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
package com.ibm.ws.security.authorization.builtin.internal;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Drive all of the tests with trace enabled. This has two purposes:
 * 1. Catches any potentially issues that only occur when trace is turned on.
 * 2. Helps improve code coverage by not "penalizing" for not executing trace lines.
 */
public class BuiltinAuthorizationServiceWithTraceTest extends BuiltinAuthorizationServiceTest {

    @BeforeClass
    public static void enableTrace() {
        outputMgr.trace("*=all=enabled");
    }

    @AfterClass
    public static void disableTrace() {
        outputMgr.trace("*=all=disabled");
    }
}
