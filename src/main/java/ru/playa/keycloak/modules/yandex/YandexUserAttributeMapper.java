package ru.playa.keycloak.modules.yandex;

import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;

/**
 * Пользовательские аттрибуты необходимые для авторизации через
 * <a href="https://yandex.ru">Яндекс</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class YandexUserAttributeMapper
        extends AbstractJsonUserAttributeMapper {

    /**
     * Список совместимых провайдеров.
     */
    private static final String[] COMPATIBLE_PROVIDERS = new String[]{YandexIdentityProviderFactory.PROVIDER_ID};

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getId() {
        return "yandex-user-attribute-mapper";
    }
}
