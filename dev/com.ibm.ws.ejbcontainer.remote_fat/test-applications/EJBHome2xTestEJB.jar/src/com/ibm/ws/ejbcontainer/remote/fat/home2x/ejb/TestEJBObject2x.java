/*******************************************************************************
 * Copyright (c) 2014, 2019 IBM Corporation and others.
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
package com.ibm.ws.ejbcontainer.remote.fat.home2x.ejb;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.EJBObject;

public interface TestEJBObject2x extends EJBObject, RMICCompatImplements {
    String echo(String s) throws RemoteException;

    TestEJBHome2x lookupTestEJBHome(String s) throws RemoteException;

    TestEJBObject2x getSessionContextEJBObject() throws RemoteException;

    TestEJBHome2x getSessionContextEJBHome() throws RemoteException;

    @Override
    List<?> testWriteValue(List<?> list) throws RemoteException;
}
