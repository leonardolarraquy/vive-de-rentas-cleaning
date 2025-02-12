package com.data.cleaning.main;

public class LinkParser {
	
	public static void main(String[] args) {
		
		String links = "https://drive.google.com/file/d/1t7up26JbSLStnPZcESAlWA0z2mjhqfR9/view?usp=drive_link, https://drive.google.com/file/d/1gzBI_pge8kpVcFo_5WWQAdgiyE_3PJKY/view?usp=drive_link, https://drive.google.com/file/d/1WSpXlro2qdrM-8bRk4XB0cZMZrAHAiIs/view?usp=drive_link, https://drive.google.com/file/d/1c7u2A0BDHkY1H69q8J6527yDTOKgIQ9B/view?usp=drive_link, https://drive.google.com/file/d/14-v548VPZPVGuj5CQuG74a96HsaDbkXb/view?usp=drive_link, https://drive.google.com/file/d/1PoUuPFPTGkO6Pcf7sJuFODLk5xwj575u/view?usp=drive_link, https://drive.google.com/file/d/1KXg3u-1bFbN8VvrUpGQzEOEjlhb-1mI9/view?usp=drive_link, https://drive.google.com/file/d/1-S-APYIZaeVz415C8sKpH5o8jnjUY_HP/view?usp=drive_link, https://drive.google.com/file/d/1QfXXLDzbnLg24hq03cC59NpO1YusSpS8/view?usp=drive_link, https://drive.google.com/file/d/1kkBJXWoVZ7Px3_dIVOjRTMJMVg7dhYkz/view?usp=drive_link, https://drive.google.com/file/d/1XsWoBrNkBh2DUrbDYCCGEZNZSqRq2E3a/view?usp=drive_link";
		String[] arr = links.split(",");
		
		for(String x: arr) {
			System.out.println(x);
		}
	}

}
