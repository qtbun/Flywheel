package com.jozufozu.flywheel.geckolib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.ISerialDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.backend.state.IRenderState;
import com.jozufozu.flywheel.backend.state.NoCullRenderState;
import com.jozufozu.flywheel.backend.state.RenderState;
import com.jozufozu.flywheel.backend.state.TextureRenderState;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;

import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GeoTileInstance<T extends TileEntity & IAnimatable> extends TileEntityInstance<T> implements ISerialDynamicInstance {

	private final AnimatedGeoModel<T> modelProvider;

	private final List<GeoInstanceTree> topLevelBones = new ArrayList<>();
	private final MatrixTransformStack stack;

	public GeoTileInstance(MaterialManager<?> materialManager, T tile, AnimatedGeoModel<T> modelProvider) {
		super(materialManager, tile);
		this.modelProvider = modelProvider;
		stack = new MatrixTransformStack();

		GeoModel model = modelProvider.getModel(modelProvider.getModelLocation(tile));
		IRenderState state = get(modelProvider.getTextureLocation(tile));

		stack.translate(getInstancePosition())
				.translate(0.5, 0.01, 0.5)
				.rotateToFace(getFacing());

		for (GeoBone bone : model.topLevelBones) {
			topLevelBones.add(new GeoInstanceTree(materialManager, state, bone));
		}
	}

	@Override
	public void beginFrame() {
		modelProvider.setLivingAnimations(tile, tile.hashCode());

		for (GeoInstanceTree bone : topLevelBones) {
			bone.recursiveCheckNeedsUpdate();
			bone.transform(stack.unwrap());
		}
	}

	@Override
	public void updateLight() {
		BlockPos pos = getWorldPosition();
		int block = world.getBrightness(LightType.BLOCK, pos);
		int sky = world.getBrightness(LightType.SKY, pos);

		for (GeoInstanceTree bone : topLevelBones) {
			bone.updateLight(block, sky);
		}
	}

	@Override
	public void remove() {
		topLevelBones.forEach(GeoInstanceTree::delete);
	}

	private Direction getFacing() {
		BlockState blockState = tile.getBlockState();
		if (blockState.hasProperty(HorizontalBlock.FACING)) {
			return blockState.getValue(HorizontalBlock.FACING);
		} else if (blockState.hasProperty(DirectionalBlock.FACING)) {
			return blockState.getValue(DirectionalBlock.FACING);
		} else {
			return Direction.NORTH;
		}
	}

	private static final Map<ResourceLocation, IRenderState> states = new HashMap<>();

	// TODO: rework render states, this is too jank
	public static IRenderState get(ResourceLocation texture) {
		return states.computeIfAbsent(texture, p -> RenderState.builder()
				.addState(TextureRenderState.get(p))
				.addState(NoCullRenderState.INSTANCE)
				.build());
	}
}
