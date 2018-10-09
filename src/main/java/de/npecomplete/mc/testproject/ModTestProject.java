package de.npecomplete.mc.testproject;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION,
		acceptedMinecraftVersions = "[1.12.2,1.13)")
public class ModTestProject {


	@EventHandler
	public void init(FMLInitializationEvent event) {
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
	}
}
