/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.jcache.internal.fat;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ibm.websphere.simplicity.config.ServerConfiguration;
import com.ibm.websphere.simplicity.config.cache.AuthCache;
import com.ibm.websphere.simplicity.config.cache.Cache;
import com.ibm.websphere.simplicity.config.cache.CacheManager;
import com.ibm.websphere.simplicity.config.cache.CachingProvider;
import com.ibm.websphere.simplicity.config.dsprops.Properties;

import componenttest.annotation.AllowedFFDC;
import componenttest.annotation.CheckForLeakedPasswords;
import componenttest.annotation.Server;
import componenttest.annotation.SkipForRepeat;
import componenttest.custom.junit.runner.FATRunner;
import componenttest.custom.junit.runner.Mode;
import componenttest.custom.junit.runner.Mode.TestMode;
import componenttest.topology.impl.LibertyServer;

/**
 * Contains distributed JCache test dynamic server.xml changes
 */
@SkipForRepeat(SkipForRepeat.EE9_FEATURES)
@RunWith(FATRunner.class)
@Mode(TestMode.FULL)
public class JCacheDynamicUpdateTest extends BaseTestCase {
    private static final Class<?> c = JCacheDynamicUpdateTest.class;
    @Server("io.openliberty.jcache.internal.fat.dynamicupdate.1")
    public static LibertyServer server1;
    /**
     * Nearly empty server configuration. Does not contain any cache config.
     */
    private static ServerConfiguration emptyConfiguration = null;

    @BeforeClass
    public static void setup() throws Exception {
        String groupName = UUID.randomUUID().toString();
        /*
         * Start server 1.
         */
        startServer1(server1, groupName, null, null);

        emptyConfiguration = server1.getServerConfiguration();
    }

    @AfterClass
    public static void after() throws Exception {
        /*
         * Stop the servers in the reverse order they were started.
         */
        stopServer(server1, "CWWKG0033W", "CWLJC0004E", "CWWKE0701E", "CWLJC0011E");
    }

    /**
     * Convenience method to configure the Liberty server with a Cache config.
     *
     * @throws Exception If there was an error configuring the server.
     */
    private static void updateLibertyServer(String cacheManagerRef, String cachingProviderRef, String providerClass, String libraryRef, String uri) throws Exception {
        ServerConfiguration server = emptyConfiguration.clone();

        Cache cache = new Cache("AuthCache", "AuthCache", cacheManagerRef);
        AuthCache authCache = new AuthCache("AuthCache");
        CacheManager cacheManager = new CacheManager("CacheManager", cachingProviderRef, uri);

        Properties properties = new Properties();
        properties.setExtraAttribute("infinispan.client.hotrod.auth_realm", "default");
        properties.setExtraAttribute("infinispan.client.hotrod.auth_server_name", "infinispan");
        properties.setExtraAttribute("infinispan.client.hotrod.uri", "${infinispan.client.hotrod.uri}");
        properties.setExtraAttribute("infinispan.client.hotrod.sasl_mechanism", "DIGEST-MD5");
        properties.setExtraAttribute("infinispan.client.hotrod.java_serial_allowlist", ".*");
        properties.setExtraAttribute("infinispan.client.hotrod.marshaller", "org.infinispan.commons.marshall.JavaSerializationMarshaller");
        cacheManager.setProps(properties);

        CachingProvider cachingProvider = new CachingProvider("CachingProvider", libraryRef, providerClass);

        server.getCaches().add(cache);
        server.getAuthCaches().add(authCache);
        server.getcacheManagers().add(cacheManager);
        server.getcachingProviders().add(cachingProvider);

        updateConfigDynamically(server1, server);
    }

    /**
     * Test dynamically adding and removing additional caches.
     *
     * @throws Exception
     */
    @Test
    public void addAndRemoveCaches() throws Exception {
        // Create a working cache configuration
        updateLibertyServer("CacheManager", "CachingProvider", "org.infinispan.jcache.remote.JCachingProvider", "InfinispanLib,CustomLoginLib",
                            "file:///${shared.resource.dir}/infinispan/infinispan.xml");
        ServerConfiguration current = server1.getServerConfiguration();

        // Add second and third cache
        current.getCaches().add(new Cache("AuthCache2", "AuthCache2", "CacheManager2"));
        current.getCaches().add(new Cache("AuthCache3", "AuthCache3", "CacheManager3"));
        updateConfigDynamically(server1, current);

        // Wait for caches to start
        waitForCreatedOrExistingJCache(server1, "AuthCache2");
        waitForCreatedOrExistingJCache(server1, "AuthCache3");

        // Remove the caches
        current.getCaches().removeById("AuthCache2");
        current.getCaches().removeById("AuthCache3");
        updateConfigDynamically(server1, current);

        //There is no unregister message, but could check here if one is added
    }

    /**
     * Test dynamic change to server.xml
     *
     * Start with bad cacheManagerRef, cachingProviderRef, uri, providerClass, and libraryRef:
     *
     * <cache id="AuthCache" name="AuthCache"
     * cacheManagerRef="CacheManagerBad" />
     *
     * <cachingProvider id="CachingProvider"
     * providerClass="org.infinispan.jcache.remote.JCachingProviderBad"
     * libraryRef="InfinispanLibBad,CustomLoginLib" />
     *
     * <cacheManager cachingProviderRef="CachingProviderBad" id="CacheManager"
     * uri="file:///${shared.resource.dir}/infinispan/infinispanbad.xml">
     *
     * <properties
     * ... />
     * </cacheManager>
     *
     * 1. Fix the cachingProviderRef and cacheManagerRef
     * 2. Fix the providerClass
     * 3. Fix the libraryRef
     * 4. Fix the uri
     * 5. Ensure auth cache / auth provider starts
     *
     * @throws Exception if the test fails for some unforeseen reason.
     */
    @Test
    @CheckForLeakedPasswords(USER1_PASSWORD)
    @AllowedFFDC(value = { "javax.cache.CacheException" })
    public void dynamicUpdate() throws Exception {
        updateLibertyServer("CacheManagerBad", "CachingProviderBad", "org.infinispan.jcache.remote.JCachingProviderBad", "InfinispanLibBad,CustomLoginLib",
                            "file:///${shared.resource.dir}/infinispan/infinispanbad.xml");
        /**
         * First look for two warnings: bad cacheManagerRef and bad cachingProviderRef.
         */
        String error = "CWWKG0033W: The value \\[CachingProviderBad\\] specified for the reference attribute \\[cachingProviderRef\\] was not found in the configuration.";
        assertTrue("Should find '" + error + "' in the logs", !server1.findStringsInLogsAndTraceUsingMark(error).isEmpty());

        error = "CWWKG0033W: The value \\[CacheManagerBad\\] specified for the reference attribute \\[cacheManagerRef\\] was not found in the configuration.";
        assertTrue("Should find '" + error + "' in the logs", !server1.findStringsInLogsAndTraceUsingMark(error).isEmpty());

        //Update server to have correct cacheManagerRef and cachingProviderRef (bad JCachingProvider, libraryRef and uri)
        updateLibertyServer("CacheManager", "CachingProvider", "org.infinispan.jcache.remote.JCachingProviderBad", "InfinispanLibBad,CustomLoginLib",
                            "file:///${shared.resource.dir}/infinispan/infinispanbad.xml");

        /**
         * Next issue is bad cachingProvider providerClass.
         *
         * Look for providerClass error:
         * CWLJC0004E: The io.openliberty.jcache.cache[AuthCache]...
         * javax.cache.CacheException: Failed to load the CachingProvider [org.infinispan.jcache.remote.JCachingProviderBad]
         * Caused by: java.lang.ClassNotFoundException
         *
         */
        error = "javax.cache.CacheException: Failed to load the CachingProvider \\[org.infinispan.jcache.remote.JCachingProviderBad\\]";
        assertTrue("Should find '" + error + "' in the logs", !server1.findStringsInLogsAndTraceUsingMark(error).isEmpty());

        //Update server to have correct JCachingProvider (bad libraryRef and uri)
        updateLibertyServer("CacheManager", "CachingProvider", "org.infinispan.jcache.remote.JCachingProvider", "InfinispanLibBad,CustomLoginLib",
                            "file:///${shared.resource.dir}/infinispan/infinispanbad.xml");

        /**
         * The providerClass is fixed. Look for libraryRef warning.
         * CWWKG0033W: The value [InfinispanLibBad] specified for the reference attribute [libraryRef] was not found in the configuration.
         */
        error = "CWWKG0033W: The value \\[InfinispanLibBad\\] specified for the reference attribute \\[libraryRef\\] was not found in the configuration.";
        assertTrue("Should find '" + error + "' in the logs", !server1.findStringsInLogsAndTraceUsingMark(error).isEmpty());

        //Update server to have the correct library (bad uri)
        updateLibertyServer("CacheManager", "CachingProvider", "org.infinispan.jcache.remote.JCachingProvider", "InfinispanLib,CustomLoginLib",
                            "file:///${shared.resource.dir}/infinispan/infinispanbad.xml");

        /**
         * The providerClass and libraryRef are fixed. Look for bad uri error.
         * CWLJC0011E: Error encountered while retrieving the AuthCache JCache: javax.cache.CacheException: Could not load configuration
         * Caused by: java.io.FileNotFoundException: /Users/eschr/libertyGit/open-liberty/dev/build.image/wlp/usr/shared/resources/infinispan/infinispanbad.xml (No such file or
         * directory)
         */
        error = "CWLJC0011E: Error encountered while retrieving the AuthCache JCache: javax.cache.CacheException: Could not load configuration";
        assertTrue("Should find '" + error + "' in the logs", !server1.findStringsInLogsAndTraceUsingMark(error).isEmpty());

        //Update to working server
        updateLibertyServer("CacheManager", "CachingProvider", "org.infinispan.jcache.remote.JCachingProvider", "InfinispanLib,CustomLoginLib",
                            "file:///${shared.resource.dir}/infinispan/infinispan.xml");

        /**
         * All issues have been fixed, so wait for the auth cache to start.
         */
        waitForCachingProvider(server1, AUTH_CACHE_NAME);
        waitForCreatedOrExistingJCache(server1, AUTH_CACHE_NAME);
    }
}
