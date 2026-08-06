package com.chaevsfe.valence.core;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModServices
{
    public static final Logger LOG = LoggerFactory.getLogger(ModConstants.MOD_ID);
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

    private ModServices () { }

    public static void reportOnce (String site, Throwable t) {
        if (REPORTED.add(site))
            LOG.error("{}", site, t);
    }
}
