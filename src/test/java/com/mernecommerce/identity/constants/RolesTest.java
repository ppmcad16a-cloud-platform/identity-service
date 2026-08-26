package com.mernecommerce.identity.constants;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

class RolesTest {

    @Test
    void constantsHaveExpectedValues() {
        assertThat(Roles.ADMIN).isEqualTo("ROLE ADMIN");
        assertThat(Roles.MEMBER).isEqualTo("ROLE MEMBER");
        assertThat(Roles.MERCHANT).isEqualTo("ROLE MERCHANT");
    }

    @Test
    void privateConstructorIsNotInstantiablePublicly() throws Exception {
        Constructor<Roles> constructor = Roles.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
