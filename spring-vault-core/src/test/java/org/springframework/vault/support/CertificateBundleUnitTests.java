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

import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CertificateBundle}.
 * 
 * @author Mark Paluch
 */
public class CertificateBundleUnitTests {

	CertificateBundle certificateBundle;

	@SuppressWarnings("unchecked")
	@Before
	public void before() throws Exception {
		Map<String, String> data = new ObjectMapper().readValue(
				getClass().getResource("/certificate.json"), Map.class);

		certificateBundle = CertificateBundle.of(data.get("serial_number"),
				data.get("certificate"), data.get("issuing_ca"), data.get("private_key"));
	}

	@Test
	public void getPrivateKeySpecShouldCreatePrivateKey() throws Exception {

		KeyFactory kf = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = kf.generatePrivate(certificateBundle.getPrivateKeySpec());

		assertThat(privateKey.getAlgorithm()).isEqualTo("RSA");
		assertThat(privateKey.getFormat()).isEqualTo("PKCS#8");
	}

	@Test
	public void getX509CertificateShouldReturnCertificate() throws Exception {

		X509Certificate x509Certificate = certificateBundle.getX509Certificate();

		assertThat(x509Certificate.getSubjectDN().getName()).isEqualTo(
				"CN=hello.example.com");
	}

	@Test
	public void getX509IssuerCertificateShouldReturnCertificate() throws Exception {

		X509Certificate x509Certificate = certificateBundle.getX509IssuerCertificate();

		assertThat(x509Certificate.getSubjectDN().getName()).startsWith(
				"CN=Intermediate CA Certificate");
	}

	@Test
	public void getAsKeystore() throws Exception {

		KeyStore keyStore = certificateBundle.createKeyStore("mykey");

		assertThat(keyStore.size()).isEqualTo(1);
		assertThat(keyStore.getCertificateChain("mykey")).hasSize(2);
	}
}
