package ru.playa.keycloak.modules;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.logging.Logger;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;

/**
 * Работаы с кэшом в котором храниться state.
 *
 * @author Anatoliy Pokhresnyi
 */
public final class InfinispanUtils {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(InfinispanUtils.class);

    /**
     * Имя кэша в infinispan в котором храниться state.
     */
    private static final String INFINISPAN_CACHE_NAME = "russianProvidersSessions";

    /**
     * Кэш state-ов.
     */
    private static volatile Cache<Object, Object> cache;

    /**
     * Конструктор.
     */
    private InfinispanUtils() {

    }

    /**
     * Инициализация кэша.
     *
     * @param session Сессия Keycloak.
     */
    public static synchronized void init(final KeycloakSession session) {
        if (cache != null) {
            return;
        }

        Cache<Object, Object> asCache = session
                .getProvider(InfinispanConnectionProvider.class)
                .getCache(InfinispanConnectionProvider.AUTHENTICATION_SESSIONS_CACHE_NAME);

        EmbeddedCacheManager manager = asCache.getCacheManager();

        if (!manager.cacheExists(INFINISPAN_CACHE_NAME)) {
            LOGGER.infof("Create new cache with name %s", INFINISPAN_CACHE_NAME);

            cache = manager.createCache(INFINISPAN_CACHE_NAME, asCache.getCacheConfiguration());
        } else {
            LOGGER.infof("Cache with name %s already exists", INFINISPAN_CACHE_NAME);

            cache = manager.getCache(INFINISPAN_CACHE_NAME);
        }
    }

    /**
     * Сохранение данных в кэш.
     *
     * @param key Ключ.
     * @param value Значение.
     */
    public static void put(final String key, final String value) {
        LOGGER.infof("Put value in cache %s. Key %s. Value %s", INFINISPAN_CACHE_NAME, key, value);

        cache.put(key, value);
    }

    /**
     * Получение значения из кэша.
     *
     * @param key Ключ.
     * @return Значение
     */
    public static String get(final String key) {
        LOGGER.infof("Get value from cache %s. Key %s.", INFINISPAN_CACHE_NAME, key);

        return cache.getOrDefault(key, key).toString();
    }

}
