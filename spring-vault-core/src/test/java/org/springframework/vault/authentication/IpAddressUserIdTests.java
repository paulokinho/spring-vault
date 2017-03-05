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
package org.springframework.vault.authentication;

import java.util.regex.Pattern;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link IpAddressUserId}.
 * 
 * @author Mark Paluch
 */
public class IpAddressUserIdTests {

	@Test
	public void shouldGenerateUppercaseSha256HexString() throws Exception {

		String userId = new IpAddressUserId().createUserId();

		assertThat(userId).matches(Pattern.compile("[0-9A-F]+")).doesNotMatch(
				Pattern.compile("[a-f]"));
	}
}
