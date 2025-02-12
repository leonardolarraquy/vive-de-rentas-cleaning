package com.data.cleaning.main;

public class LinkParser {
	
	public static void main(String[] args) {
		
		String links = "https://drive.google.com/file/d/1Lizfi1-VDXj6cskxi4_fIHCfqGh4DHME/view?usp=drive_link, https://drive.google.com/file/d/1RdOSC0VVY8W6k481kWKp1FjAvuyIyYID/view?usp=drive_link, https://drive.google.com/file/d/1DLSNa3afmQJRDwTGrQLOTf9a9wiyhswI/view?usp=drive_link, https://drive.google.com/file/d/17rMCIwhWh4w4St9HuJFOmGxTlmK8BM3c/view?usp=drive_link, https://drive.google.com/file/d/1ZlCeP_FJY_G2hpMKmfxXyxp4vryp__jB/view?usp=drive_link, https://drive.google.com/file/d/1oyaTmzhKojaro49mSFFnlhSlcgvNeQ-1/view?usp=drive_link, https://drive.google.com/file/d/1S84p-QGbj-c800zXdAmDGBQgUyYdhS4g/view?usp=drive_link";
		String[] arr = links.split(",");
		
		for(String x: arr) {
			System.out.println(x);
		}
	}

}
