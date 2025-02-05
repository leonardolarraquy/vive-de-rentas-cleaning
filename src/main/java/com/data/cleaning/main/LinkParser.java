package com.data.cleaning.main;

public class LinkParser {
	
	public static void main(String[] args) {
		
		String links = "https://drive.google.com/file/d/10PV3qgGRExdrUIGbaDGmsZNWjSXObmd7/view?usp=drive_link, https://drive.google.com/file/d/19Y7uHR27-PY_lFTyT1wr1c2_3wLLBjvH/view?usp=drive_link, https://drive.google.com/file/d/16YU1PXQD6EiNd6BAAN2QPduhPNwd9-iV/view?usp=drive_link, https://drive.google.com/file/d/1ied8hIUUXCM7znR6E7V-l7UVOj5jYX6o/view?usp=drive_link, https://drive.google.com/file/d/1N3seN9G_U6CZggEx_QTC2nhfWGgYwqnC/view?usp=drive_link, https://drive.google.com/file/d/1IXLDYm7zWbLd32f1-ZFYAxSjxQDFBLVx/view?usp=drive_link, https://drive.google.com/file/d/1gdnrhA8Igf6t5QVOOoXAJKXaHx9mzIeb/view?usp=drive_link, https://drive.google.com/file/d/1GSmwGZ6NWbuasb0VEUPCr2y4gqcRIB01/view?usp=drive_link, https://drive.google.com/file/d/1ju_2Di_neuyf-R-DKjeShfd10pFjqB_r/view?usp=drive_link, https://drive.google.com/file/d/1ZDqXvk4r9mFupNM7TIlJYn-pINWEB6WT/view?usp=drive_link, https://drive.google.com/file/d/13Hcqazq1RKi7YtsVH1e2y7noT8zG1LTp/view?usp=drive_link, https://drive.google.com/file/d/1JK-AjEQ_fFrlUdT6ooLxRNyVCUI8Le4O/view?usp=drive_link, https://drive.google.com/file/d/1NsMkRN50Y7NsFgVeG2-0WJo0IdJ7dFdF/view?usp=drive_link, https://drive.google.com/file/d/1MD345JAnKrmpA28HYnX383-ya4JrXesh/view?usp=drive_link, https://drive.google.com/file/d/1L59as0taX2PD_qFfjuNquBnRr8DLu_OX/view?usp=drive_link, https://drive.google.com/file/d/1Z-hmFHf4s4K89e4j1wOdZRJLWvF5TLoQ/view?usp=drive_link";
		String[] arr = links.split(",");
		
		for(String x: arr) {
			System.out.println(x);
		}
	}

}
