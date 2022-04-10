package com.jozufozu.flywheel.backend.instancing.batching;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.struct.Batched;
import com.jozufozu.flywheel.api.struct.ModelTransformer;
import com.jozufozu.flywheel.backend.instancing.AbstractInstancer;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.model.DirectVertexConsumer;
import com.jozufozu.flywheel.core.model.Model;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.RenderType;

public class CPUInstancer<D extends InstanceData> extends AbstractInstancer<D> {

	private final Batched<D> batchingType;
	private final RenderType renderType;
	private final ModelTransformer<D> transformer;

	public CPUInstancer(Batched<D> type, Model modelData, RenderType renderType) {
		super(type::create, modelData);
		batchingType = type;
		this.renderType = renderType;
		transformer = type.createTransformer(modelData, renderType);
	}

	void submitTasks(PoseStack stack, TaskEngine pool, DirectVertexConsumer consumer) {
		int instances = getInstanceCount();

		while (instances > 0) {
			int end = instances;
			instances -= 512;
			int start = Math.max(instances, 0);

			int verts = getModelVertexCount() * (end - start);

			DirectVertexConsumer sub = consumer.split(verts);

			pool.submit(() -> drawRange(stack, sub, start, end));
		}
	}

	private void drawRange(PoseStack stack, VertexConsumer buffer, int from, int to) {
		for (D d : data.subList(from, to)) {
			transformer.renderInto(d, buffer, stack);
		}
	}

	void drawAll(PoseStack stack, VertexConsumer buffer) {
		for (D d : data) {
			transformer.renderInto(d, buffer, stack);
		}
	}

	void setup() {
		if (anyToRemove) {
			data.removeIf(InstanceData::isRemoved);
			anyToRemove = false;
		}
	}

	@Override
	public void notifyDirty() {
		// noop
	}
}
