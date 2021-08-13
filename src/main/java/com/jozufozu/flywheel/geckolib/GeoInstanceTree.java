package com.jozufozu.flywheel.geckolib;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.backend.state.IRenderState;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.mojang.blaze3d.matrix.MatrixStack;

import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.util.RenderUtils;

public class GeoInstanceTree {

	private static final MatrixStack IDENTITY = new MatrixStack();

	public final GeoBone bone;
	private final ModelData boneInstance;

	private final List<GeoInstanceTree> children;

	private boolean modelsHidden;

	public GeoInstanceTree(MaterialManager<?> materialManager, IRenderState tex, GeoBone bone) {
		this.bone = bone;

		if (bone.childCubes.isEmpty()) {
			boneInstance = null;
		} else {
			boneInstance = materialManager.cutout(tex)
					.material(Materials.TRANSFORMED)
					.model(bone, () -> new BoneModel(bone))
					.createInstance()
					.setTransform(IDENTITY);
		}

		ImmutableList.Builder<GeoInstanceTree> builder = ImmutableList.builder();
		for (GeoBone childBone : bone.childBones) {
			builder.add(new GeoInstanceTree(materialManager, tex, childBone));
		}

		children = builder.build();
	}

	public void transform(MatrixStack stack) {
		if (bone.isHidden() && !modelsHidden) {
			hide();
			modelsHidden = true;
			return;
		}

		modelsHidden = false;

		stack.pushPose();
		RenderUtils.translate(bone, stack);
		RenderUtils.moveToPivot(bone, stack);
		RenderUtils.rotate(bone, stack);
		RenderUtils.scale(bone, stack);
		RenderUtils.moveBackFromPivot(bone, stack);

		if (boneInstance != null)
			boneInstance.setTransform(stack);

		for (GeoInstanceTree child : children) {
			child.transform(stack);
		}

		stack.popPose();
	}

	private void hide() {
		if (boneInstance != null)
			boneInstance.setEmptyTransform();

		children.forEach(GeoInstanceTree::hide);
	}

	public void delete() {
		if (boneInstance != null)
			boneInstance.delete();

		children.forEach(GeoInstanceTree::delete);
	}

	public void updateLight(int blockLight, int skyLight) {
		if (boneInstance != null) {
			boneInstance.setBlockLight(blockLight)
					.setSkyLight(skyLight);
		}

		for (GeoInstanceTree child : children) {
			child.updateLight(blockLight, skyLight);
		}
	}
}
