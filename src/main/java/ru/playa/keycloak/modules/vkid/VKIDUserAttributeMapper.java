package ru.playa.keycloak.modules.vkid;

import ru.playa.keycloak.modules.AbstractRussianJsonUserAttributeMapper;

/**
 * Пользовательские аттрибуты необходимые для авторизации через
 * <a href="https://vk.com">ВКонтакте</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class VKIDUserAttributeMapper
    extends AbstractRussianJsonUserAttributeMapper {

    /**
     * Список совместимых провайдеров.
     */
    private static final String[] COMPATIBLE_PROVIDERS = new String[]{VKIDIdentityProviderFactory.PROVIDER_ID};

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getId() {
        return "vkid-user-attribute-mapper";
    }
}