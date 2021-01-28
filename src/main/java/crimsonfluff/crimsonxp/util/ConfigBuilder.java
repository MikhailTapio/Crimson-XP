package crimsonfluff.crimsonxp.util;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigBuilder {
    public final ForgeConfigSpec COMMON;

    public ForgeConfigSpec.IntValue CharmRange;
    public ForgeConfigSpec.BooleanValue CharmShowParticles;
    public ForgeConfigSpec.BooleanValue CharmCurios;
    public ForgeConfigSpec.BooleanValue CharmBottlePickup;

    public ConfigBuilder() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("XP Charm Settings");

        CharmRange = builder
            .comment("What should the range of the XP Charm be ?  Default: 16")
            .defineInRange("CharmRange", 16, 5, 48);

        CharmShowParticles = builder
                .comment("Show particle effects ?  Default: true")
                .define("ShowParticles", true);

        CharmCurios = builder
                .comment("Create the XP Charm Curio slot ?  Default: true")
                .define("CharmCurios", true);

        CharmBottlePickup = builder
                .comment("Convert picked up Experience Bottles ?  Default: true")
                .define("CharmBottlePickup", true);

        builder.pop();

        COMMON = builder.build();
    }
}
