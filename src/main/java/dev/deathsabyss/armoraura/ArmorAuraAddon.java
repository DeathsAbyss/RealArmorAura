package dev.deathsabyss.armoraura;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class ArmorAuraAddon extends MeteorAddon {
    @Override
    public void onInitialize() {
        Modules.get().add(new ArmorAuraModule());
    }

    @Override
    public String getPackage() {
        return "dev.deathsabyss.armoraura";
    }
}
