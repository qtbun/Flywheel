package com.jozufozu.flywheel.geckolib;

import java.util.Map;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.jozufozu.flywheel.mixin.TileEntityRendererAccessor;

import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class GeckolibCompat {

	public static void init(GatherContextEvent event) {
		if (!event.isFirstLoad()) return;

		Map<TileEntityType<?>, TileEntityRenderer<?>> renderers = ((TileEntityRendererAccessor) TileEntityRendererDispatcher.instance).getRenderers();

		renderers.forEach((type, renderer) -> {
			if (renderer instanceof GeoBlockRenderer) {
				GeoBlockRenderer<?> geo = (GeoBlockRenderer<?>) renderer;

				AnimatedGeoModel<?> modelProvider = geo.getGeoModelProvider();

				registerInstanceFactoryGenericsHack(type, modelProvider);
			}
		});
	}

	private static <T extends TileEntity & IAnimatable> void registerInstanceFactoryGenericsHack(TileEntityType<?> type, AnimatedGeoModel<?> modelProvider) {
		InstancedRenderRegistry.getInstance()
				.tile(type)
				.setSkipRender(true)
				.factory((manager, te) -> (TileEntityInstance) (new GeoTileInstance<>(manager, (T) te, (AnimatedGeoModel<T>) modelProvider)));
	}
}
