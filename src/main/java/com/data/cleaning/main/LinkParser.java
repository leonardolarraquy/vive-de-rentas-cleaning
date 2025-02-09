package com.data.cleaning.main;

public class LinkParser {
	
	public static void main(String[] args) {
		
		String links = "https://drive.google.com/file/d/1DMFLpTVr5-5r0bTR-5Qul6-y4E9bguy9/view?usp=drive_link, https://drive.google.com/file/d/1vtcYQ6h-DZD8mjqhQMU5Hw7ZEx8cn1gI/view?usp=drive_link, https://drive.google.com/file/d/1atJ9kyDk03p_8jDPCxPXyGSFXZy9dRPL/view?usp=drive_link, https://drive.google.com/file/d/1bjhj6fLzfl2N1BJ571STnMyCckuUWT4o/view?usp=drive_link, https://drive.google.com/file/d/1PmGyrVWVX__y_P-0ooTBaTQMl60dxDh4/view?usp=drive_link";
		String[] arr = links.split(",");
		
		for(String x: arr) {
			System.out.println(x);
		}
	}

}
