package io.puzzlebox.jigsaw;

/**
 * Created by sc on 11/6/14.
 */

public class DrawerItem {

	String ItemName;
	int imgResID;

	public DrawerItem(String itemName, int imgResID) {
		super();
		ItemName = itemName;
		this.imgResID = imgResID;
	}

	public String getItemName() {
		return ItemName;
	}
	public void setItemName(String itemName) {
		ItemName = itemName;
	}
	public int getImgResID() {
		return imgResID;
	}
	public void setImgResID(int imgResID) {
		this.imgResID = imgResID;
	}

}
