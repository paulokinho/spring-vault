/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.vault.support;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.util.Assert;

/**
 * Transit backend key creation request options.
 * 
 * @author Mark Paluch
 */
public class VaultTransitKeyCreationRequest {

	private final Boolean derived;

	@JsonProperty("type")
	private final String type;

	@JsonProperty("convergent_encryption")
	private final Boolean convergentEncryption;

	private VaultTransitKeyCreationRequest(Boolean derived, String type,
			Boolean convergentEncryption) {
		this.derived = derived;
		this.type = type;
		this.convergentEncryption = convergentEncryption;
	}

	/**
	 * @return a new {@link VaultTransitKeyCreationRequestBuilder}.
	 */
	public static VaultTransitKeyCreationRequestBuilder builder() {
		return new VaultTransitKeyCreationRequestBuilder();
	}

	/**
	 *
	 * @return {@literal true} if key derivation MUST be used.
	 */
	public Boolean getDerived() {
		return derived;
	}

	/**
	 *
	 * @return {@literal true} if convergent encryption should be used (where the same
	 * plaintext creates the same cipher text).
	 */
	public Boolean getConvergentEncryption() {
		return convergentEncryption;
	}

	/**
	 *
	 * @return the key type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Builder for {@link VaultTransitKeyCreationRequest}.
	 */
	public static class VaultTransitKeyCreationRequestBuilder {

		private Boolean derived;
		private String type = "aes256-gcm96";
		private Boolean convergentEncryption;

		VaultTransitKeyCreationRequestBuilder() {
		}

		/**
		 * Configure key derivation.
		 *
		 * @param type the type of key to create, must not be empty or {@literal null}.
		 * @return {@code this} {@link VaultTransitKeyCreationRequestBuilder}.
		 */
		public VaultTransitKeyCreationRequestBuilder type(String type) {

			Assert.hasText(type, "Type must not be empty");

			this.type = type;
			return this;
		}

		/**
		 * Configure key derivation.
		 * 
		 * @param derived {@literal true} if key derivation MUST be used. If enabled, all
		 * encrypt/decrypt requests to this named key must provide a context which is used
		 * for key derivation. Defaults to {@literal false}.
		 * @return {@code this} {@link VaultTransitKeyCreationRequestBuilder}.
		 */
		public VaultTransitKeyCreationRequestBuilder derived(boolean derived) {

			this.derived = derived;
			return this;
		}

		/**
		 * Configure convergent encryption where the same plaintext creates the same
		 * ciphertext. Requires {@link #derived(boolean)} to be {@literal true}.
		 *
		 * @param convergentEncryption {@literal true} the same plaintext creates the same
		 * ciphertext. Defaults to {@literal false}.
		 * @return {@code this} {@link VaultTransitKeyCreationRequestBuilder}.
		 */
		public VaultTransitKeyCreationRequestBuilder convergentEncryption(
				boolean convergentEncryption) {
			this.convergentEncryption = convergentEncryption;
			return this;
		}

		/**
		 * Build a new {@link VaultTransitKeyCreationRequest} instance. Requires
		 * {@link #type(String)} to be configured.
		 *
		 * @return a new {@link VaultTransitKeyCreationRequest}.
		 */
		public VaultTransitKeyCreationRequest build() {

			Assert.hasText(type, "Type must not be empty");

			return new VaultTransitKeyCreationRequest(derived, type, convergentEncryption);
		}
	}
}
