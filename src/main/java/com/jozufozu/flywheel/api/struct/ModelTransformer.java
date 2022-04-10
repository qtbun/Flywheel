package com.jozufozu.flywheel.api.struct;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public interface ModelTransformer<S> {
	void renderInto(S struct, VertexConsumer buffer, PoseStack poseStack);
}
