package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;

/**
 * An interface giving {@link TileEntityInstance}s a hook to have a function called at
 * the start of a frame. By implementing {@link ISerialDynamicInstance}, a {@link TileEntityInstance}
 * can animate its models in ways that could not be easily achieved by shader attribute
 * parameterization.
 *
 * <br><br> If your goal is offloading work to shaders, but you're unsure exactly how you need
 * to parameterize the instances, you're encouraged to implement this for prototyping.
 */
@Deprecated
public interface ISerialDynamicInstance extends IInstance {
	/**
	 * Called every frame.
	 * <br>
	 * <em>DISPATCHED IN SERIAL</em>, don't attempt to mutate anything outside of this instance.
	 * <br>
	 * {@link Instancer}/{@link InstanceData} creation/acquisition is safe here.
	 */
	void beginFrame();
}
