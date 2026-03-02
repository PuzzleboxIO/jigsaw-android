package io.puzzlebox.jigsaw.ui;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;

import io.puzzlebox.jigsaw.data.ProfileSingleton;

public class TileViewAnimator extends LinearLayout {

	final Context context;

	public TileViewAnimator(Context context)
	{
		super(context);
		this.context = context;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Animation anim_list = AnimationUtils.loadAnimation(context, ProfileSingleton.getInstance().tilesAnimationId);
		LayoutAnimationController controller = new LayoutAnimationController(anim_list, 0.1f);
		setLayoutAnimation(controller);
	}
}