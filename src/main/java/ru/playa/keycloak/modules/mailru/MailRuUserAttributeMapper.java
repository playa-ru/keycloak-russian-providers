package ru.playa.keycloak.modules.mailru;

import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;

/**
 * Пользовательские аттрибуты необходимые для авторизации через
 * <a href="https://my.mail.ru">Мой Мир</a>.
 *
 * @author Anatoliy Pokhresnyi
 */
public class MailRuUserAttributeMapper
        extends AbstractJsonUserAttributeMapper {

    /**
     * Список совместимых провайдеров.
     */
    private static final String[] COMPATIBLE_PROVIDERS = new String[]{MailRuIdentityProviderFactory.PROVIDER_ID};

    @Override
    public String[] getCompatibleProviders() {
        return COMPATIBLE_PROVIDERS;
    }

    @Override
    public String getId() {
        return "mailru-user-attribute-mapper";
    }
}
