package ru.playa.keycloak.modules.vk;

import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;

/**
 * Пользовательские аттрибуты необходимые для авторизации через
 * <a href="https://vk.com">ВКонтакте</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class VKUserAttributeMapper
        extends AbstractJsonUserAttributeMapper {

    /**
     * Список совместимых провайдеров.
     */
    private static final String[] COMPATIBLE_PROVIDERS = new String[]{VKIdentityProviderFactory.PROVIDER_ID};

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getId() {
        return "vk-user-attribute-mapper";
    }
}
