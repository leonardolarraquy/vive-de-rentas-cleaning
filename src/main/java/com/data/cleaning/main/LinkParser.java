package com.data.cleaning.main;

public class LinkParser {
	
	public static void main(String[] args) {
		
		String links = "https://drive.google.com/file/d/1w5vje88EPFe8iVQJM1Jst1UG1oIkyzbF/view?usp=drive_link, https://drive.google.com/file/d/1BrItOWQDGxHQG-2mFqpbwvErLBCXT07m/view?usp=drive_link, https://drive.google.com/file/d/1qtFnrm-RUr578-zMim-l-aGfroCifu2G/view?usp=drive_link, https://drive.google.com/file/d/1feSsGrsNwW-NIewL5ZwLPrQd-7COk4wu/view?usp=drive_link, https://drive.google.com/file/d/1uUH2DojDsAw3CqBmraIPCLzt7I0xrwVc/view?usp=drive_link, https://drive.google.com/file/d/1pbxUIfvqNh-176aqYi9KibMfBRv3SEF-/view?usp=drive_link, https://drive.google.com/file/d/11u8HFn_QfUyZIDW1dnPt4k4XxtxOpUMH/view?usp=drive_link, https://drive.google.com/file/d/12ww6HBR6-bzQRjGbn3_W1MjvG0qob5kD/view?usp=drive_link, https://drive.google.com/file/d/1L_N-kbM2YlibrmuIcuPnnCBW-7hDvuSN/view?usp=drive_link";
		String[] arr = links.split(",");
		
		for(String x: arr) {
			System.out.println(x);
		}
	}

}
