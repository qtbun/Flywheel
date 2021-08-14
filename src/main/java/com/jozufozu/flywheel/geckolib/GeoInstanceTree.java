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


	private UpdateTask action;

	private boolean hidden = false;

	private float lastScaleX = Float.NaN;
	private float lastScaleY = Float.NaN;
	private float lastScaleZ = Float.NaN;
	private float lastPositionX = Float.NaN;
	private float lastPositionY = Float.NaN;
	private float lastPositionZ = Float.NaN;
	private float lastRotationX = Float.NaN;
	private float lastRotationY = Float.NaN;
	private float lastRotationZ = Float.NaN;
	private float lastPivotX = Float.NaN;
	private float lastPivotY = Float.NaN;
	private float lastPivotZ = Float.NaN;

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

	/**
	 * Figure out what needs to be done this frame.
	 *
	 * <p>
	 *     This calculates the minimal amount of work needed to correctly update this bone.
	 * </p>
	 *
	 * @return The task we will perform.
	 */
	public UpdateTask recursiveCheckNeedsUpdate() {
		// by default, we assume there's nothing to do
		action = UpdateTask.SKIP;

		// hidden propagates to children, no need to search further
		if (bone.isHidden() && !hidden) {
			return action = UpdateTask.HIDE;
		}

		// no need to search further
		// changes to our bone MUST pe passed on to all our child bones
		if (boneNeedsUpdate()) {
			return action = UpdateTask.UPDATE;
		}

		for (GeoInstanceTree child : children) {
			UpdateTask childTask = child.recursiveCheckNeedsUpdate();

			// don't early return here because we need to check all the children too
			if (childTask.needsParentPassthrough())
				action = UpdateTask.PASSTHROUGH;
		}

		return action;
	}

	public void transform(MatrixStack stack) {
		switch (action) {
		case HIDE:
			hide();
			break;
		case PASSTHROUGH:
			update(stack, false);
			break;
		case UPDATE:
			update(stack, true);

			//
			this.lastScaleX = bone.getScaleX();
			this.lastScaleY = bone.getScaleY();
			this.lastScaleZ = bone.getScaleZ();
			this.lastPositionX = bone.getPositionX();
			this.lastPositionY = bone.getPositionY();
			this.lastPositionZ = bone.getPositionZ();
			this.lastRotationX = bone.getRotationX();
			this.lastRotationY = bone.getRotationY();
			this.lastRotationZ = bone.getRotationZ();
			this.lastPivotX = bone.getPivotX();
			this.lastPivotY = bone.getPivotY();
			this.lastPivotZ = bone.getPivotZ();
		default:
		}
	}

	/**
	 * Calculates the bone transform matrix and passes it on to the children.
	 *
	 * @param stack The MatrixStack we'll use to compute all the bone transforms.
	 * @param thisChanged If true, all descendent nodes will be updated regardless of their {@link #action}
	 */
	private void update(MatrixStack stack, boolean thisChanged) {
		stack.pushPose();
		RenderUtils.translate(bone, stack);
		RenderUtils.moveToPivot(bone, stack);
		RenderUtils.rotate(bone, stack);
		RenderUtils.scale(bone, stack);
		RenderUtils.moveBackFromPivot(bone, stack);

		if (thisChanged) {
			if (boneInstance != null)
				boneInstance.setTransform(stack);

			for (GeoInstanceTree child : children) {
				child.update(stack, true);
			}
		} else {
			for (GeoInstanceTree child : children) {
				child.transform(stack);
			}
		}

		stack.popPose();
	}

	private boolean boneNeedsUpdate() {
		return this.lastScaleX != bone.getScaleX()
				|| this.lastScaleY != bone.getScaleY()
				|| this.lastScaleZ != bone.getScaleZ()
				|| this.lastPositionX != bone.getPositionX()
				|| this.lastPositionY != bone.getPositionY()
				|| this.lastPositionZ != bone.getPositionZ()
				|| this.lastRotationX != bone.getRotationX()
				|| this.lastRotationY != bone.getRotationY()
				|| this.lastRotationZ != bone.getRotationZ()
				|| this.lastPivotX != bone.getPivotX()
				|| this.lastPivotY != bone.getPivotY()
				|| this.lastPivotZ != bone.getPivotZ();
	}

	private void hide() {
		if (boneInstance != null)
			boneInstance.setEmptyTransform();

		children.forEach(GeoInstanceTree::hide);

		hidden = true;
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
