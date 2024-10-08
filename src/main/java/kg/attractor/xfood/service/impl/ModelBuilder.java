package kg.attractor.xfood.service.impl;

import kg.attractor.xfood.dto.CountryDto;
import kg.attractor.xfood.dto.LocationDto;
import kg.attractor.xfood.dto.okhttp.PizzeriaManagerShiftDto;
import kg.attractor.xfood.dto.opportunity.OpportunityCreateDto;
import kg.attractor.xfood.dto.pizzeria.PizzeriaDto;
import kg.attractor.xfood.dto.shift.ShiftCreateDto;
import kg.attractor.xfood.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class ModelBuilder {
    
    protected Manager buildManager(PizzeriaManagerShiftDto dto) {
        return Manager.builder()
                .name(dto.getName())
                .surname(dto.getSurname())
                .uuid(dto.getStaffId())
                .phoneNumber(dto.getPhNumber())
                .build();

    }

    protected WorkSchedule buildWorkSchedule(PizzeriaManagerShiftDto dto, Pizzeria p, Manager m) {
        return WorkSchedule
                .builder()
                .endTime(dto.getScheduledShiftEndAtLocal())
                .startTime(dto.getScheduledShiftStartAtLocal())
                .manager(m)
                .pizzeria(p)
                .build();
    }

    protected Opportunity buildNewOpportunity(OpportunityCreateDto dto, LocalDate date, User user) {
        return Opportunity.builder()
                .id(dto.getId())
                .user(user)
                .date(date)
//                .startTime(LocalTime.of(dto.getStartTimeHour(), dto.getStartTimeMinute()))
//                .endTime(LocalTime.of(dto.getEndTimeHour(), dto.getEndTimeMinute()))
                .build();
    }

    protected Shift buildNewShift(ShiftCreateDto dto, Opportunity opportunity) {
        return Shift.builder()
                .id(dto.getId())
                .startTime(
                        LocalTime.of(
                                dto.getStartTimeHour(),
                                dto.getStartTimeMinute() != null ? dto.getStartTimeMinute() : 0
                        )
                )
                .endTime(
                        LocalTime.of(
                                dto.getEndTimeHour(),
                                dto.getEndTimeMinute() != null ? dto.getEndTimeMinute() : 0
                        )
                )
                .opportunity(opportunity)
                .build();
    }
    
    public Location buildLocation(LocationDto dto) {
        return Location.builder()
                .id(dto.getId())
                .name(dto.getName())
                .country(this.buildCountry(dto.getCountry()))
                .timezone(dto.getTimezone())
                .build();
    }
    
    public Country buildCountry(CountryDto dto) {
        return Country.builder()
                .countryCode(dto.getCountryCode())
                .countryName(dto.getCountryName())
                .apiUrl(dto.getApiUrl())
                .authUrl(dto.getAuthUrl())
                .build();
    }
    
    public Pizzeria buildPizzeria(PizzeriaDto dto, Location location) {
        return Pizzeria.builder()
                .id(dto.getId())
                .location(location)
                .name(dto.getName())
                .uuid(dto.getUuid())
                .build();
    }
}
