package com.data.cleaning.main.livingup;

import com.data.cleaning.main.Commons;
import com.data.cleaning.main.livinguniversidad.LivingUniversidadConvenioAdhesion;

public class LivingUp extends LivingUniversidadConvenioAdhesion {
	
	public String getEnajenante(String content) {
		return "FIDEICOMISO BMI 85101677 (OCHO CINCO UNO CERO UNO SEIS SIETE SIETE) - VIVE DE LAS RENTAS";
	}

	public String getAdquiriente(String content) {
		String res = Commons.extract(content, "SEÑORA", "(", "").replaceAll("SEÑORA", "");
		if(res.length() == 0)
			res = Commons.extract(content, "SEÑOR", "(", "").replaceAll("SEÑOR", "");

		if(res.length() == 0)
			res = Commons.extract(content, "SENORA", "(", "").replaceAll("SENORA", "");

		if(res.length() == 0)
			res = Commons.extract(content, "SENOR", "(", "").replaceAll("SENOR", "");

		return res;
	}


	public String getTipoContrato() {
		return "Convenio de adhesión";
	}

	public String getProyecto() {
		return "Living up";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/living-up-convenio-adhesion/";
	}
	
	public static void main(String[] args) {
		LivingUp parser = new LivingUp();
		parser.process();
	}	

}