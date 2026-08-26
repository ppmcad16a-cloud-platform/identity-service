package com.mernecommerce.identity.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JsonTest {

    @Test
    void buildsOrderedMapFromPairs() {
        Map<String, Object> result = Json.of("a", 1, "b", "two", "c", null);

        assertThat(result).hasSize(3);
        assertThat(result).containsEntry("a", 1);
        assertThat(result).containsEntry("b", "two");
        assertThat(result).containsKey("c");
        assertThat(result.get("c")).isNull();
    }

    @Test
    void emptyWhenNoPairs() {
        assertThat(Json.of()).isEmpty();
    }

    @Test
    void privateConstructorIsNotInstantiablePublicly() throws Exception {
        Constructor<Json> constructor = Json.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
