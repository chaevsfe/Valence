package com.chaevsfe.valence.core.module;

import com.chaevsfe.valence.core.config.ConfigSnapshot;
import com.chaevsfe.valence.core.config.ConfigView;
import com.chaevsfe.valence.core.config.OptionBuilder;
import java.util.function.Supplier;
import net.minecraft.world.item.CreativeModeTab;

public abstract class ValenceModule
{
    public final String id;
    public final ModuleCategory category;
    public final ModuleSide side;
    public final String description;

    private volatile boolean enabled;
    private volatile ConfigView options = ConfigView.EMPTY;

    protected ValenceModule (String id, ModuleCategory category, ModuleSide side, String description) {
        this.id = id;
        this.category = category;
        this.side = side;
        this.description = description;
    }

    public final boolean enabled () {
        return enabled;
    }

    public final ConfigView options () {
        return options;
    }

    public boolean enabledByDefault () {
        return true;
    }

    public void defineOptions (OptionBuilder builder) { }

    public void register () { }

    public void init () { }

    public void addTabItems (CreativeModeTab.Output output) { }

    public Supplier<ClientModule> client () {
        return null;
    }

    protected void onConfigChanged () { }

    public final void apply (ConfigSnapshot snap) {
        enabled = snap.enabled(id);
        options = snap.options(id);
        onConfigChanged();
    }
}
