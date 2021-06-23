/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.javaee.ddmodel.ejbext;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class EJBJarExtTest extends EJBJarExtTestBase {
    @Parameters
    public static Iterable<? extends Object> data() {
        return TEST_DATA;
    }
    
    public EJBJarExtTest(boolean ejbInWar) {
        super(ejbInWar);
    }

    @Test
    public void testGetVersion() throws Exception {
        Assert.assertEquals("XMI",
                parseEJBJarExtXMI(ejbJarExtXMI("") + "</ejbext:EJBJarExtension>", getEJBJar21())
                    .getVersion());

        Assert.assertEquals("Version should be 1.0",
                "1.0", parseEJBJarExtXML(ejbJarExt10XML() + "</ejb-jar-ext>").getVersion());
        Assert.assertEquals("Version should be 1.1",
                "1.1", parseEJBJarExtXML(ejbJarExt11XML() + "</ejb-jar-ext>").getVersion());
    }

    @Test
    public void testGetEnterpriseBeans() throws Exception {
        Assert.assertEquals("List size should be zero",
                0, parseEJBJarExtXML(ejbJarExt11XML() + "</ejb-jar-ext>").getEnterpriseBeans().size());
    }

    @Test
    public void testGetEnterpriseBeansXMI() throws Exception {
        Assert.assertEquals(Collections.emptyList(),
                parseEJBJarExtXMI(ejbJarExtXMI("") + "</ejbext:EJBJarExtension>", getEJBJar21())
                    .getEnterpriseBeans());
    }

}
