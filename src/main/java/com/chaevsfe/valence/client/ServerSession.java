package com.chaevsfe.valence.client;

import com.chaevsfe.valence.core.ModServices;
import java.util.List;
import java.util.Set;

public final class ServerSession
{
    private static volatile Set<String> serverModules;

    private ServerSession () { }

    public static void accept (List<String> enabled) {
        serverModules = Set.copyOf(enabled);
        ModServices.LOG.info("Valence server: {} modules enabled", enabled.size());
    }

    public static void clear () {
        serverModules = null;
    }

    public static boolean present () {
        return serverModules != null;
    }

    public static boolean allows (String moduleId) {
        Set<String> modules = serverModules;
        return modules != null && modules.contains(moduleId);
    }
}
