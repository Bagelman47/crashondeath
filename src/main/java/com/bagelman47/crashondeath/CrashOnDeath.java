package com.bagelman47.crashondeath;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;

public class CrashOnDeath implements ModInitializer {
	public static final String MOD_ID = "crashondeath";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[CrashOnDeath] Initializing core event listeners under Yarn mappings...");

		// Registering to the Fabric lifecycle event chain for entity deaths
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, amount) -> {
			if (entity instanceof ServerPlayerEntity player) {
				MinecraftServer server = player.getServer();

				if (server != null) {
					String playerName = player.getGameProfile().getName();

					LOGGER.warn("[CrashOnDeath] Player '{}' died at position [X:{}, Y:{}, Z:{}].",
							playerName, player.getX(), player.getY(), player.getZ());

					try {
						server.saveAll(true, true, true);
						LOGGER.info("[CrashOnDeath] Save successful!");
					} catch (Exception saveException) {
						LOGGER.error("[CrashOnDeath] Save failed! Chunks may be corrupted.", saveException);
					}
				}

				triggerNativeCrash();
			}
			return true;
		});

	}

	private void triggerNativeCrash() {
		try {
			LOGGER.debug("[CrashOnDeath] Attempting sun.misc.Unsafe core abstraction reference lookup...");

			Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			sun.misc.Unsafe unsafe = (sun.misc.Unsafe) field.get(null);

			LOGGER.warn("[CrashOnDeath] EXCEPTION_ACCESS_VIOLATION incoming. SAY GOODBYE!");

			unsafe.putByte(0L, (byte) 0);

		} catch (Throwable throwable) {
			LOGGER.error("[CrashOnDeath] Unsafe access isolation restricted by security manager structures.", throwable);
			LOGGER.error("[CrashOnDeath] Engaging emergency fallback kill vector.");

			Runtime.getRuntime().halt(1);
		}
	}
}