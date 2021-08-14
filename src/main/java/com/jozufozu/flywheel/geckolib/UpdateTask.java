package com.jozufozu.flywheel.geckolib;

public enum UpdateTask {
	/**
	 * Do nothing.
	 */
	SKIP,
	/**
	 * Hide all bones in a subtree.
	 */
	HIDE,
	/**
	 * Re-calculate and re-submit all matrices in a subtree.
	 */
	UPDATE,
	/**
	 * Re-calculate the matrix for a bone, but don't re-submit.
	 * A descendent bone needs it, so pass it to all the bone's children.
	 */
	PASSTHROUGH;

	/**
	 * @return True if this Task requires a bone's parent to be at least passthrough.
	 */
	public boolean needsParentPassthrough() {
		return this == UPDATE || this == PASSTHROUGH;
	}
}
