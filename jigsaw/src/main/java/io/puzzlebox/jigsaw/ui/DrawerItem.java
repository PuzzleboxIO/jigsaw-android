package io.puzzlebox.jigsaw.ui;

public class DrawerItem {

	final String ItemName;
	final int imgResID;

	public DrawerItem(String itemName, int imgResID) {
		super();
		ItemName = itemName;
		this.imgResID = imgResID;
	}

	public String getItemName() {
		return ItemName;
	}
	public int getImgResID() {
		return imgResID;
	}
}
