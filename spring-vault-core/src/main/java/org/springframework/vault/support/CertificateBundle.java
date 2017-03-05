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
package org.springframework.vault.support;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.util.Assert;
import org.springframework.vault.VaultException;

/**
 * Value object representing a certificate bundle consisting of a private key, the
 * certificate and the issuer certificate. Certificate and keys can be either DER or PEM
 * encoded. DER-encoded certificates can be converted to a {@link KeySpec} and
 * {@link X509Certificate}.
 *
 * @author Mark Paluch
 * @see #getPrivateKeySpec()
 * @see #getX509Certificate()
 * @see #getIssuingCaCertificate()
 */
public class CertificateBundle {

	private final String serialNumber;

	private final String certificate;

	private final String issuingCaCertificate;

	private final String privateKey;

	private CertificateBundle(@JsonProperty("serial_number") String serialNumber,
			@JsonProperty("certificate") String certificate,
			@JsonProperty("issuing_ca") String issuingCaCertificate,
			@JsonProperty("private_key") String privateKey) {

		this.serialNumber = serialNumber;
		this.certificate = certificate;
		this.issuingCaCertificate = issuingCaCertificate;
		this.privateKey = privateKey;
	}

	/**
	 * Create a {@link CertificateBundle} given a private key with certificates and the
	 * serial number.
	 *
	 * @param serialNumber must not be empty or {@literal null}.
	 * @param certificate must not be empty or {@literal null}.
	 * @param issuingCaCertificate must not be empty or {@literal null}.
	 * @param privateKey must not be empty or {@literal null}.
	 * @return the {@link CertificateBundle}
	 */
	public static CertificateBundle of(String serialNumber, String certificate,
			String issuingCaCertificate, String privateKey) {

		Assert.hasText(serialNumber, "Serial number must not be empty");
		Assert.hasText(certificate, "Certificate must not be empty");
		Assert.hasText(issuingCaCertificate, "Issuing CA certificate must not be empty");
		Assert.hasText(privateKey, "Private key must not be empty");

		return new CertificateBundle(serialNumber, certificate, issuingCaCertificate,
				privateKey);
	}

	/**
	 * @return the serial number.
	 */
	public String getSerialNumber() {
		return this.serialNumber;
	}

	/**
	 * @return encoded certificate (PEM or DER-encoded).
	 */
	public String getCertificate() {
		return this.certificate;
	}

	/**
	 * @return encoded certificate of the issuing CA (PEM or DER-encoded).
	 */
	public String getIssuingCaCertificate() {
		return this.issuingCaCertificate;
	}

	/**
	 * @return the private key (decrypted form, PEM or DER-encoded)
	 */
	public String getPrivateKey() {
		return this.privateKey;
	}

	/**
	 * Retrieve the private key as {@link KeySpec}. Only supported if private key is
	 * DER-encoded.
	 *
	 * @return the private {@link KeySpec}. {@link java.security.KeyFactory} can generate
	 * a {@link java.security.PrivateKey} from this {@link KeySpec}.
	 */
	public KeySpec getPrivateKeySpec() {

		try {
			byte[] bytes = Base64.decode(getPrivateKey());
			return KeystoreUtil.getRSAKeySpec(bytes);
		}
		catch (IOException e) {
			throw new VaultException("Cannot create KeySpec from private key", e);
		}
	}

	/**
	 * Retrieve the certificate as {@link X509Certificate}. Only supported if certificate
	 * is DER-encoded.
	 *
	 * @return the {@link X509Certificate}.
	 */
	public X509Certificate getX509Certificate() {

		try {
			byte[] bytes = Base64.decode(getCertificate());
			return KeystoreUtil.getCertificate(bytes);
		}
		catch (IOException e) {
			throw new VaultException("Cannot create Certificate from certificate", e);
		}
		catch (CertificateException e) {
			throw new VaultException("Cannot create Certificate from certificate", e);
		}
	}

	/**
	 * Retrieve the issuing CA certificate as {@link X509Certificate}. Only supported if
	 * certificate is DER-encoded.
	 *
	 * @return the issuing CA {@link X509Certificate}.
	 */
	public X509Certificate getX509IssuerCertificate() {

		try {
			byte[] bytes = Base64.decode(getIssuingCaCertificate());
			return KeystoreUtil.getCertificate(bytes);
		}
		catch (IOException e) {
			throw new VaultException(
					"Cannot create Certificate from issuing CA certificate", e);
		}
		catch (CertificateException e) {
			throw new VaultException(
					"Cannot create Certificate from issuing CA certificate", e);
		}
	}

	/**
	 * Create a {@link KeyStore} from this {@link CertificateBundle} containing the
	 * private key and certificate chain. Only supported if certificate and private key
	 * are DER-encoded.
	 *
	 * @param keyAlias the key alias to use.
	 * @return the {@link KeyStore} containing the private key and certificate chain.
	 */
	public KeyStore createKeyStore(String keyAlias) {

		Assert.hasText(keyAlias, "Key alias must not be empty");

		try {
			return KeystoreUtil.createKeyStore(keyAlias, getPrivateKeySpec(),
					getX509Certificate(), getX509IssuerCertificate());
		}
		catch (GeneralSecurityException e) {
			throw new VaultException("Cannot create KeyStore", e);
		}
		catch (IOException e) {
			throw new VaultException("Cannot create KeyStore", e);
		}
	}
}
