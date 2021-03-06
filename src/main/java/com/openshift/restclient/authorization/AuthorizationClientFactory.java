/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package com.openshift.restclient.authorization;

import com.openshift.internal.restclient.authorization.AuthorizationClient;

/**
 * @author Jeff Cantrill
 */
public class AuthorizationClientFactory {
	
	public IAuthorizationClient create(){
		return new AuthorizationClient();
	}
}
