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
package org.springframework.vault;

import org.springframework.core.NestedRuntimeException;

/**
 * The Spring Vault specific {@link NestedRuntimeException} implementation.
 *
 * @author Mark Paluch
 */
@SuppressWarnings("serial")
public class VaultException extends NestedRuntimeException {

	/**
	 * Creates a {@code VaultException} with the specified detail message.
	 * 
	 * @param msg the detail message
	 */
	public VaultException(String msg) {
		super(msg);
	}

	/**
	 * Creates a {@code NestedRuntimeException} with the specified detail message and
	 * nested exception.
	 * 
	 * @param msg the detail message
	 * @param cause the nested exception
	 */
	public VaultException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
