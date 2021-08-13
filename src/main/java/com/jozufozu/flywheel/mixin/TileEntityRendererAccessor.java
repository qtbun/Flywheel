package com.jozufozu.flywheel.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntityType;

@Mixin(TileEntityRendererDispatcher.class)
public interface TileEntityRendererAccessor {

	@Accessor("renderers")
	Map<TileEntityType<?>, TileEntityRenderer<?>> getRenderers();
}
