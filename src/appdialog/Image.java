package appdialog;

abstract class Image {
	

	protected int image[] = new int[5808];
	

	public int getByte(int position){
		return image[position];
	}
	public int getNoRevByte(int position){
		return image[position];
	}
}
