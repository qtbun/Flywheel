package com.jozufozu.flywheel.geckolib;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.model.IModel;
import com.jozufozu.flywheel.util.RenderMath;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.util.RenderUtils;

public class BoneModel implements IModel {

	public final GeoBone bone;

	private final int vertexCount;

	public BoneModel(GeoBone bone) {
		this.bone = bone;

		vertexCount = bone.childCubes.size() * 6 * 4; // 6 quads per cube, 4 vertices per quad
	}

	@Override
	public void buffer(VecBuffer buffer) {
		for (GeoCube cube : bone.childCubes) {
			bufferCube(buffer, cube);
		}
	}

	@Override
	public int vertexCount() {
		return vertexCount;
	}

	@Override
	public VertexFormat format() {
		return Formats.UNLIT_MODEL;
	}

	public void bufferCube(VecBuffer buffer, GeoCube cube) {

		MatrixStack stack = new MatrixStack();

		RenderUtils.moveToPivot(cube, stack);
		RenderUtils.rotate(cube, stack);
		RenderUtils.moveBackFromPivot(cube, stack);
		Matrix3f matrix3f = stack.last().normal();
		Matrix4f matrix4f = stack.last().pose();

		for (GeoQuad quad : cube.quads) {
			if (quad == null) {
				continue;
			}
			Vector3f normal = quad.normal.copy();
			normal.transform(matrix3f);

			/*
			 * Fix shading dark shading for flat cubes
			 */
			if ((cube.size.y() == 0 || cube.size.z() == 0) && normal.x() < 0) {
				normal.mul(-1, 1, 1);
			}
			if ((cube.size.x() == 0 || cube.size.z() == 0) && normal.y() < 0) {
				normal.mul(1, -1, 1);
			}
			if ((cube.size.x() == 0 || cube.size.y() == 0) && normal.z() < 0) {
				normal.mul(1, 1, -1);
			}

			byte nx = RenderMath.nb(normal.x());
			byte ny = RenderMath.nb(normal.y());
			byte nz = RenderMath.nb(normal.z());

			for (GeoVertex vertex : quad.vertices) {
				Vector4f vector4f = new Vector4f(vertex.position.x(), vertex.position.y(), vertex.position.z(),
						1.0F);
				vector4f.transform(matrix4f);
//				bufferIn.vertex(vector4f.x(), vector4f.y(), vector4f.z(), red, green, blue, alpha,
//						vertex.textureU, vertex.textureV, packedOverlayIn, packedLightIn, normal.x(), normal.y(),
//						normal.z());
				buffer.putVec3(vector4f.x(), vector4f.y(), vector4f.z())
						.putVec3(nx, ny, nz)
						.putVec2(vertex.textureU, vertex.textureV);
			}
		}
	}
}
