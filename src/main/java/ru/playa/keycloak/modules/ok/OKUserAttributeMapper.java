package ru.playa.keycloak.modules.ok;

import ru.playa.keycloak.modules.AbstractRussianJsonUserAttributeMapper;

/**
 * Пользовательские аттрибуты необходимые для авторизации через
 * <a href="https://ok.ru/">Одноклассники</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class OKUserAttributeMapper
    extends AbstractRussianJsonUserAttributeMapper {

    /**
     * Список совместимых провайдеров.
     */
    private static final String[] COMPATIBLE_PROVIDERS = new String[]{OKIdentityProviderFactory.PROVIDER_ID};

    private static final String MAPPER_ID = "ok-user-attribute-mapper";

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getId() {
        return MAPPER_ID;
    }
}