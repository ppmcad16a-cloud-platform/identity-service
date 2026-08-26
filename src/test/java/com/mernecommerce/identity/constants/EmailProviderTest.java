package com.mernecommerce.identity.constants;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

class EmailProviderTest {

    @Test
    void constantHasExpectedValue() {
        assertThat(EmailProvider.EMAIL).isEqualTo("Email");
    }

    @Test
    void privateConstructorIsNotInstantiablePublicly() throws Exception {
        Constructor<EmailProvider> constructor = EmailProvider.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
