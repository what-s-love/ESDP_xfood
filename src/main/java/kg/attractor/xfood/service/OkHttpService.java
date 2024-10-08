package kg.attractor.xfood.service;

import kg.attractor.xfood.dto.okhttp.PizzeriaStaffMemberDto;
import kg.attractor.xfood.dto.okhttp.PizzeriasShowDodoIsDto;

import java.util.List;

public interface OkHttpService {
	
	void getWorksheetOfPizzeriaManagers(Long pizzeriaUuid, String supervisorUsername);
	
	List<PizzeriasShowDodoIsDto> getPizzeriasByMatch(String name);
	
	void rewritePizzeriasToRedis();
	
	List<PizzeriaStaffMemberDto> getPizzeriaStaff(String countryCode, String pizzeriaUuid);
	
}
