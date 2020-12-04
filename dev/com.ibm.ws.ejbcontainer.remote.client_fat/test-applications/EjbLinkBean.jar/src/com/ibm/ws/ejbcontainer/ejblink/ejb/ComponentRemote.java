/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.ejbcontainer.ejblink.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

public interface ComponentRemote extends EJBObject {
    /**
     * Verify AutoLinked EJB is the expected bean
     **/
    public String getBeanName() throws RemoteException;
}