package ru.playa.keycloak.modules;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 *  * Утилитный класс проверки добавлен ли домен в белый список.
 */
public class HostedDomainUtils {

    /**
     * Проверка добавлен ли домен в белый список.
     *
     * @param email Электронная почта.
     * @param domains Белый список доменов.
     * @param provider Название провайдера.
     */
    public static void isHostedDomain(final String email, final String domains, final String provider) {
        String domain = email.substring(email.indexOf("@") + 1);
        boolean match = Optional
                .ofNullable(domains)
                .map(hd -> hd.split(","))
                .map(Arrays::asList)
                .orElse(Collections.singletonList("*"))
                .stream()
                .noneMatch(hd -> hd.equalsIgnoreCase(domain) || hd.equals("*"));

        if (match) {
            throw new IllegalArgumentException(MessageUtils.hostedDomain(provider, domain));
        }
    }

}
