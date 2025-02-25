package com.data.cleaning.main;

public class LinkParser {
	
	public static void main(String[] args) {
		
		String links = "https://drive.google.com/file/d/1GH7n8fQv3MzCfsdVmq448wQwGaTFwdZX/view?usp=drive_link, https://drive.google.com/file/d/1iz1UoJDwgefbLGrJiZWihgr-vaYOSZ2N/view?usp=drive_link, https://drive.google.com/file/d/10ql1Ma6rO6EZhSm2vqGcgs5tqlppQPPw/view?usp=drive_link, https://drive.google.com/file/d/1FwUEcmGSdxIqB4uhXamTFjjHYbyUr3v7/view?usp=drive_link, https://drive.google.com/file/d/1NlXO7Bkyoq6h7EsL8LuCPZ2EPvVlK6xH/view?usp=drive_link, https://drive.google.com/file/d/1k20qBb5MfxwOO3SZNbzwtXliC8XDF4hc/view?usp=drive_link, https://drive.google.com/file/d/1S42LR25LfFFAzn9cl_CixghSvL4_53rH/view?usp=drive_link, https://drive.google.com/file/d/1qIbSM6Oq-7cWKi6WfnNJdBWiLuSd-5c1/view?usp=drive_link";
		String[] arr = links.split(",");
		
		for(String x: arr) {
			System.out.println(x);
		}
	}

}
