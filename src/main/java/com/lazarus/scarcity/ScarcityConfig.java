package com.lazarus.scarcity;

import net.minecraftforge.common.ForgeConfigSpec;

final class ScarcityConfig {
    static final ForgeConfigSpec SPEC;
    static final ForgeConfigSpec.IntValue SECOND_DAY, THIRD_DAY, FOURTH_DAY;
    static final ForgeConfigSpec.DoubleValue[] COMMON = new ForgeConfigSpec.DoubleValue[4];
    static final ForgeConfigSpec.DoubleValue[] RISKY = new ForgeConfigSpec.DoubleValue[4];

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.comment("World-age scavenging. Chances are per loot location, not per item.").push("progression");
        SECOND_DAY = b.defineInRange("secondStageDay", 7, 1, 100000);
        THIRD_DAY = b.defineInRange("thirdStageDay", 15, 2, 100000);
        FOURTH_DAY = b.defineInRange("fourthStageDay", 30, 3, 100000);
        double[] common = {1.0, 0.72, 0.42, 0.16};
        double[] risky = {1.0, 0.86, 0.62, 0.36};
        for (int i = 0; i < 4; i++) {
            COMMON[i] = b.defineInRange("commonChanceStage" + i, common[i], 0, 1);
            RISKY[i] = b.defineInRange("riskyChanceStage" + i, risky[i], 0, 1);
        }
        b.pop();
        SPEC = b.build();
    }
    private ScarcityConfig() {}
}
