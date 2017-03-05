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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.vault.client.VaultHttpHeaders;
import org.springframework.vault.support.VaultToken;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LifecycleAwareSessionManager}.
 *
 * @author Mark Paluch
 */
@RunWith(MockitoJUnitRunner.class)
public class LifecycleAwareSessionManagerUnitTests {

	@Mock
	private ClientAuthentication clientAuthentication;

	@Mock
	private AsyncTaskExecutor taskExecutor;

	@Mock
	private ThreadPoolTaskScheduler taskScheduler;

	@Mock
	private RestOperations restOperations;

	private LifecycleAwareSessionManager sessionManager;

	@Before
	public void before() throws Exception {
		sessionManager = new LifecycleAwareSessionManager(clientAuthentication,
				taskExecutor, restOperations);
	}

	@Test
	public void shouldObtainTokenFromClientAuthentication() {

		when(clientAuthentication.login()).thenReturn(LoginToken.of("login"));

		assertThat(sessionManager.getSessionToken()).isEqualTo(LoginToken.of("login"));
	}

	@Test
	public void shouldRevokeLoginTokenOnDestroy() {

		when(clientAuthentication.login()).thenReturn(LoginToken.of("login"));

		sessionManager.renewToken();
		sessionManager.destroy();

		verify(restOperations)
				.postForObject(
						eq("/auth/token/revoke-self"),
						eq(new HttpEntity<Object>(VaultHttpHeaders.from(LoginToken
								.of("login")))), any(Class.class));
	}

	@Test
	public void shouldNotRevokeRegularTokenOnDestroy() {

		when(clientAuthentication.login()).thenReturn(VaultToken.of("login"));

		sessionManager.renewToken();
		sessionManager.destroy();

		verifyZeroInteractions(restOperations);
	}

	@Test
	public void shouldNotThrowExceptionsOnRevokeErrors() {

		when(clientAuthentication.login()).thenReturn(LoginToken.of("login"));

		when(
				restOperations.postForObject(anyString(), any(),
						ArgumentMatchers.<Class> any())).thenThrow(
				new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

		sessionManager.renewToken();
		sessionManager.destroy();

		verify(restOperations)
				.postForObject(
						eq("/auth/token/revoke-self"),
						eq(new HttpEntity<Object>(VaultHttpHeaders.from(LoginToken
								.of("login")))), any(Class.class));
	}

	@Test
	public void shouldScheduleTokenRenewal() {

		when(clientAuthentication.login()).thenReturn(LoginToken.renewable("login", 5));

		sessionManager.getSessionToken();

		verify(taskExecutor).execute(any(Runnable.class));
	}

	@Test
	public void shouldRunTokenRenewal() {

		when(clientAuthentication.login()).thenReturn(LoginToken.renewable("login", 5));

		ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

		sessionManager.getSessionToken();
		verify(taskExecutor).execute(runnableCaptor.capture());

		runnableCaptor.getValue().run();

		verify(restOperations).postForObject(
				eq("/auth/token/renew-self"),
				eq(new HttpEntity<Object>(VaultHttpHeaders.from(LoginToken.renewable(
						"login", 5)))), any(Class.class));
		verify(clientAuthentication, times(1)).login();
	}

	@Test
	public void shouldReScheduleTokenRenewalAfterSucessfulRenewal() {

		when(clientAuthentication.login()).thenReturn(LoginToken.renewable("login", 5));

		ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

		sessionManager.getSessionToken();
		verify(taskExecutor).execute(runnableCaptor.capture());

		runnableCaptor.getValue().run();

		verify(taskExecutor, times(2)).execute(any(Runnable.class));
	}

	@Test
	public void shouldUseTaskScheduler() {

		sessionManager = new LifecycleAwareSessionManager(clientAuthentication,
				taskScheduler, restOperations);

		when(clientAuthentication.login()).thenReturn(LoginToken.renewable("login", 5));

		ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);

		sessionManager.getSessionToken();
		verify(taskScheduler).schedule(any(Runnable.class), triggerCaptor.capture());

		assertThat(triggerCaptor.getValue().nextExecutionTime(null)).isNotNull();
		assertThat(triggerCaptor.getValue().nextExecutionTime(null)).isNull();
	}

	@Test
	public void shouldNotReScheduleTokenRenewalAfterFailedRenewal() {

		when(clientAuthentication.login()).thenReturn(LoginToken.renewable("login", 5));
		when(
				restOperations.postForObject(anyString(), any(),
						ArgumentMatchers.<Class> any())).thenThrow(
				new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

		ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

		sessionManager.getSessionToken();
		verify(taskExecutor).execute(runnableCaptor.capture());

		runnableCaptor.getValue().run();

		verify(taskExecutor, times(1)).execute(any(Runnable.class));
	}

	@Test
	public void shouldObtainTokenIfNoTokenAvailable() {

		when(clientAuthentication.login()).thenReturn(LoginToken.renewable("login", 5));

		sessionManager.renewToken();

		assertThat(sessionManager.getSessionToken()).isEqualTo(
				LoginToken.renewable("login", 5));
		verify(clientAuthentication, times(1)).login();
	}

	@Test
	public void renewShouldReportFalseIfTokenRenewalFails() {

		when(clientAuthentication.login()).thenReturn(LoginToken.renewable("login", 5));
		when(
				restOperations.postForObject(anyString(),
						ArgumentMatchers.<Object> any(), ArgumentMatchers.<Class> any()))
				.thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST));

		sessionManager.getSessionToken();

		assertThat(sessionManager.renewToken()).isFalse();
		verify(clientAuthentication, times(1)).login();
	}
}
