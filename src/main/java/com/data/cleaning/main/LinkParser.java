package com.data.cleaning.main;

public class LinkParser {
	
	public static void main(String[] args) {
		
		String links = "https://drive.google.com/file/d/1ueJcdLTmagl7bx-Rv6GTX6GRud-RTHT9/view?usp=drive_link, https://drive.google.com/file/d/14h0eoquOu-DisQdrxiNuoEzKumtCoTMf/view?usp=drive_link, https://drive.google.com/file/d/1vkWBd9-s6vaXC3k-U2S8C9rNmWloe9RJ/view?usp=drive_link, https://drive.google.com/file/d/1VKuoKDPOr1ECNq8NGAEXEaOwXgfk-6ap/view?usp=drive_link, https://drive.google.com/file/d/1vn7F_1qBzoGYqhe04YCl8klrUMA7BkPN/view?usp=drive_link";
		String[] arr = links.split(",");
		
		for(String x: arr) {
			System.out.println(x);
		}
	}

}
