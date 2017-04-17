package io.puzzlebox.jigsaw.ui;

import android.content.Context;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;

import io.puzzlebox.jigsaw.R;


/**
 * Created by sc on 4/17/17.
 */

public class TileViewAnimator extends LinearLayout {

	private final static String TAG = TileViewAnimator.class.getSimpleName();

	private Animation inAnimation;
	private Animation outAnimation;

	Context context;

	public TileViewAnimator(Context context)
	{
		super(context);
		this.context = context;
	}

	public void setInAnimation(Animation inAnimation)
	{
		this.inAnimation = inAnimation;
	}

	public void setOutAnimation(Animation outAnimation)
	{
		this.outAnimation = outAnimation;
	}

	@Override
	public void setVisibility(int visibility) {

	Log.i(TAG, "setVisibility(int visibility): " + visibility);


		if (getVisibility() != visibility)
		{
			if (visibility == VISIBLE)
			{
				if (inAnimation != null) startAnimation(inAnimation);
			}
			else if ((visibility == INVISIBLE) || (visibility == GONE))
			{
				if (outAnimation != null) startAnimation(outAnimation);
			}
		}

		super.setVisibility(visibility);
	}

	public void show(final LinearLayout viewGroup){

		Log.d(TAG, "show");

		final LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		viewGroup.addView(this,p);

	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		Animation anim_list = AnimationUtils.loadAnimation(context, R.anim.tiles);
//		LayoutAnimationController controller = new LayoutAnimationController(anim_list, 0.1f);
		LayoutAnimationController controller = new LayoutAnimationController(anim_list, 0.2f);

		setLayoutAnimation(controller);
	}

}