package com.data.cleaning.main;

public class LinkParser {
	
	public static void main(String[] args) {
		
		String links = "https://drive.google.com/file/d/12HUCJHD7aMTHfYEKyugORK432i9kC3xB/view?usp=drive_link, https://drive.google.com/file/d/1a8BguaBxEkB17KntfmJlWnghsKNnoduZ/view?usp=drive_link, https://drive.google.com/file/d/1FxkpPe7kH9QBeCJF2yBT1q2ZS2-yNVz6/view?usp=drive_link";
		String[] arr = links.split(",");
		
		for(String x: arr) {
			System.out.println(x);
		}
	}

}
