package ru.playa.keycloak.modules.ok;

import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;

/**
 * Пользовательские аттрибуты необходимые для авторизации через
 * <a href="https://ok.ru/">Одноклассники</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class OKUserAttributeMapper
        extends AbstractJsonUserAttributeMapper {

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