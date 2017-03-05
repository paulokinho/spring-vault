/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.vault.authentication;

import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.Assert;
import org.springframework.vault.VaultException;
import org.springframework.vault.client.VaultResponses;
import org.springframework.vault.support.VaultResponse;
import org.springframework.vault.support.VaultToken;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;

/**
 * TLS Client Certificate {@link ClientAuthentication}.
 *
 * @author Mark Paluch
 */
public class ClientCertificateAuthentication implements ClientAuthentication {

	private final static Log logger = LogFactory
			.getLog(ClientCertificateAuthentication.class);

	private final RestOperations restOperations;

	/**
	 * Creates a {@link ClientCertificateAuthentication} using {@link RestOperations}.
	 *
	 * @param restOperations must not be {@literal null}.
	 */
	public ClientCertificateAuthentication(RestOperations restOperations) {

		Assert.notNull(restOperations, "RestOperations must not be null");

		this.restOperations = restOperations;
	}

	@Override
	public VaultToken login() {
		return createTokenUsingTlsCertAuthentication("cert");
	}

	private VaultToken createTokenUsingTlsCertAuthentication(String path) {

		try {
			VaultResponse response = restOperations.postForObject("/auth/{mount}/login",
					Collections.emptyMap(), VaultResponse.class, path);

			logger.debug("Login successful using TLS certificates");

			return LoginTokenUtil.from(response.getAuth());
		}
		catch (HttpStatusCodeException e) {
			throw new VaultException(String.format(
					"Cannot login using TLS certificates: %s",
					VaultResponses.getError(e.getResponseBodyAsString())));
		}
	}
}
