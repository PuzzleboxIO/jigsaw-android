package io.puzzlebox.jigsaw.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import java.util.List;

import io.puzzlebox.jigsaw.R;

public class NavigationDrawerAdapter extends ArrayAdapter<DrawerItem> {

	private final Context context;
	final List<DrawerItem> drawerItemList;
	final int layoutResID;

	public NavigationDrawerAdapter(Context context, int layoutResourceID,
	                               List<DrawerItem> listItems) {
		super(context, layoutResourceID, listItems);
		this.context = context;
		this.drawerItemList = listItems;
		this.layoutResID = layoutResourceID;

	}

	@Override
	public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {

		DrawerItemHolder drawerHolder;
		View view = convertView;

		if (view == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			drawerHolder = new DrawerItemHolder();

			view = inflater.inflate(layoutResID, parent, false);
			drawerHolder.ItemName = view
					  .findViewById(R.id.drawer_itemName);

			view.setTag(drawerHolder);

		} else {
			drawerHolder = (DrawerItemHolder) view.getTag();

		}

		DrawerItem dItem = this.drawerItemList.get(position);

		drawerHolder.ItemName.setCompoundDrawablesRelativeWithIntrinsicBounds(
				ResourcesCompat.getDrawable(view.getResources(), dItem.getImgResID(), null),
				null, null, null);
		drawerHolder.ItemName.setText(dItem.getItemName());

		return view;
	}

	private static class DrawerItemHolder {
		TextView ItemName;
	}
}

