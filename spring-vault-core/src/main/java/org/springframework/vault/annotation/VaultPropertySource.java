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
package org.springframework.vault.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Annotation providing a convenient and declarative mechanism for adding a
 * {@link VaultPropertySource} to Spring's
 * {@link org.springframework.core.env.Environment Environment}. To be used in conjunction
 * with @{@link Configuration} classes. <h3>Example usage</h3>
 * <p>
 * Given a Vault path {@code secret/my-application} containing the configuration data pair
 * {@code database.password=mysecretpassword}, the following {@code @Configuration} class
 * uses {@code @VaultPropertySource} to contribute {@code secret/my-application} to the
 * {@code Environment}'s set of {@code PropertySources}.
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;VaultPropertySource(&quot;secret/my-application&quot;)
 * public class AppConfig {
 * 
 * 	&#064;Autowired
 * 	Environment env;
 * 
 * 	&#064;Bean
 * 	public TestBean testBean() {
 * 		TestBean testBean = new TestBean();
 * 		testBean.setPassword(env.getProperty(&quot;database.password&quot;));
 * 		return testBean;
 * 	}
 * }
 * </pre>
 *
 * Notice that the {@code Environment} object is
 * {@link org.springframework.beans.factory.annotation.Autowired @Autowired} into the
 * configuration class and then used when populating the {@code TestBean} object. Given
 * the configuration above, a call to {@code testBean.getPassword()} will return
 * "mysecretpassword".
 * <p>
 * In certain situations, it may not be possible or practical to tightly control property
 * source ordering when using {@code @VaultPropertySource} annotations. For example, if
 * the {@code @Configuration} classes above were registered via component-scanning, the
 * ordering is difficult to predict. In such cases - and if overriding is important - it
 * is recommended that the user fall back to using the programmatic PropertySource API.
 * See {@link org.springframework.core.env.ConfigurableEnvironment
 * ConfigurableEnvironment} and
 * {@link org.springframework.core.env.MutablePropertySources MutablePropertySources}
 * javadocs for details.
 *
 * @author Mark Paluch
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(VaultPropertySources.class)
@Import(VaultPropertySourceRegistrar.class)
public @interface VaultPropertySource {

	/**
	 * Indicate the Vault path(s) of the properties to be retrieved. For example,
	 * {@code "secret/myapp"} or {@code "secret/my-application/profile"}.
	 * <p>
	 * Each location will be added to the enclosing {@code Environment} as its own
	 * property source, and in the order declared.
	 */
	String[] value();

	/**
	 * Property name prefix for properties obtained from Vault. All properties will be
	 * prefixed with {@code propertyNamePrefix}.
	 */
	String propertyNamePrefix() default "";

	/**
	 * Configure the name of the {@link org.springframework.vault.core.VaultTemplate} bean
	 * to be used with the property sources.
	 */
	String vaultTemplateRef() default "vaultTemplate";
}
