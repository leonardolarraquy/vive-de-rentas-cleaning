package com.data.cleaning.main.edificioburjassot;

import com.data.cleaning.main.Commons;
import com.data.cleaning.main.livinguniversidad.LivingUniversidadConvenioAdhesion;

public class EdificioBurjassot extends LivingUniversidadConvenioAdhesion {
	
	/*ESTE PROYECTO LO EJECUTE A MANO PORQUE NO SE LEEN LOS ARCHIVOS */
	
	
	public String getEnajenante(String content) {
		return "Javier Eduardo Aguilera Cifuentes";
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
		return "Contrato de préstamo participativo";
	}

	public String getProyecto() {
		return "Edificio Burjassot";
	}

	public String getFolderPath() {
		return "/Users/leonardo.larraquy/eclipse-workspace/data-cleaning/edificio-burjassot/";
	}
	
	public static void main(String[] args) {
		EdificioBurjassot parser = new EdificioBurjassot();
		parser.process();
	}	

}