package com.jozufozu.flywheel.api.struct;

import com.jozufozu.flywheel.core.model.Model;

import net.minecraft.client.renderer.RenderType;

public interface Batched<S> extends StructType<S> {
	ModelTransformer<S> createTransformer(Model modelData, RenderType renderType);
}
