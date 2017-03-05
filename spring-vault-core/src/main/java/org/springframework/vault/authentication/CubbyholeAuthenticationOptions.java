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

import org.springframework.util.Assert;
import org.springframework.vault.support.VaultToken;

/**
 * Authentication options for {@link CubbyholeAuthentication}.
 * <p>
 * Authentication options provide the path below cubbyhole and the cubbyhole mode.
 * Instances of this class are immutable once constructed.
 *
 * @author Mark Paluch
 * @see CubbyholeAuthentication
 * @see #builder()
 */
public class CubbyholeAuthenticationOptions {

	/**
	 * Initial {@link VaultToken} to access Cubbyhole.
	 */
	private final VaultToken initialToken;

	/**
	 * Path of the Cubbyhole response path.
	 */
	private final String path;

	/**
	 * Indicates whether the Cubbyhole contains a wrapped token.
	 */
	private final boolean wrappedToken;

	private CubbyholeAuthenticationOptions(VaultToken initialToken, String path,
			boolean wrappedToken) {

		this.initialToken = initialToken;
		this.path = path;
		this.wrappedToken = wrappedToken;
	}

	/**
	 * @return a new {@link CubbyholeAuthenticationOptionsBuilder}.
	 */
	public static CubbyholeAuthenticationOptionsBuilder builder() {
		return new CubbyholeAuthenticationOptionsBuilder();
	}

	/**
	 * @return the initial {@link VaultToken} to access Cubbyhole.
	 */
	public VaultToken getInitialToken() {
		return initialToken;
	}

	/**
	 * @return the path of the Cubbyhole response path.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return {@literal true} indicates that the Cubbyhole response contains a wrapped
	 * token, otherwise {@literal false} to expect a token in the {@literal data}
	 * response.
	 */
	public boolean isWrappedToken() {
		return wrappedToken;
	}

	/**
	 * Builder for {@link CubbyholeAuthenticationOptions}.
	 */
	public static class CubbyholeAuthenticationOptionsBuilder {

		private VaultToken initialToken;

		private String path;

		private boolean wrappedToken;

		CubbyholeAuthenticationOptionsBuilder() {
		}

		/**
		 * Configure the initial {@link VaultToken} to access Cubbyhole.
		 *
		 * @param initialToken must not be {@literal null}.
		 * @return {@code this} {@link CubbyholeAuthenticationOptionsBuilder}.
		 */
		public CubbyholeAuthenticationOptionsBuilder initialToken(VaultToken initialToken) {

			Assert.notNull(initialToken, "Initial Vault Token must not be null");

			this.initialToken = initialToken;
			return this;
		}

		/**
		 * Configure the cubbyhole path, such as {@code cubbyhole/token}. Expects a token
		 * in the {@code data} response.
		 *
		 * @param path must not be empty or {@literal null}.
		 * @return {@code this} {@link CubbyholeAuthenticationOptionsBuilder}.
		 */
		public CubbyholeAuthenticationOptionsBuilder path(String path) {

			Assert.hasText(path, "Path must not be empty");

			this.path = path;
			return this;
		}

		/**
		 * Configure whether to use wrapped token responses.
		 *
		 * @return {@code this} {@link CubbyholeAuthenticationOptionsBuilder}.
		 */
		public CubbyholeAuthenticationOptionsBuilder wrapped() {

			this.path = "cubbyhole/response";
			this.wrappedToken = true;
			return this;
		}

		/**
		 * Build a new {@link CubbyholeAuthenticationOptions} instance. Requires
		 * {@link #path(String)} or {@link #wrapped()} to be configured.
		 *
		 * @return a new {@link CubbyholeAuthenticationOptions}.
		 */
		public CubbyholeAuthenticationOptions build() {

			Assert.notNull(initialToken, "Initial Vault Token must not be null");

			return new CubbyholeAuthenticationOptions(initialToken, path, wrappedToken);
		}
	}
}
