package com.generic.core.services.serviceimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.generic.core.model.entities.Area;
import com.generic.core.model.entities.City;
import com.generic.core.model.entities.Landmark;
import com.generic.core.onboarding.exceldto.ExcelLocationDto;
import com.generic.core.onboarding.exceldto.ExcelSheetObject;
import com.generic.core.respository.AreaRepository;
import com.generic.core.respository.CityRepository;
import com.generic.core.respository.LandmarkRepository;
import com.generic.core.services.service.LandmarkServiceI;
import com.generic.core.utilities.Util;
import com.generic.core.utilities.UtilConstants;
import com.generic.rest.constants.Constants;
import com.generic.rest.dto.LandmarkDto;
import com.generic.rest.dto.ResponseDto;

@Service
public class LandmarkService implements LandmarkServiceI{ 

	@Resource
	LandmarkRepository landmarkRepository;
	
	@Resource
	CityRepository cityRepository;
	
	@Resource
	AreaRepository areaRepository;
	
	@Override
	public Boolean landmarkPresent(String landmarkId) {
		return landmarkRepository.findOne(landmarkId) == null ? false : true; 
	}
	
	@Override
	public List<LandmarkDto> listsLandmarkForArea(String areaId) {
		Area anArea = new Area(areaId);
		List<Landmark> landmarks = landmarkRepository.findByArea(anArea);
		return convertToLandmarkDto(landmarks);
	}
	
	private List<LandmarkDto> convertToLandmarkDto(List<Landmark> landmarks) {
		List<LandmarkDto> landmarkDtos = new ArrayList<LandmarkDto>();
		for(Landmark anLandmark : landmarks) {
			landmarkDtos.add(new LandmarkDto(anLandmark.getLandmarkId(), anLandmark.getLandmarkName(), anLandmark.getArea().getAreaName()));
		}
		return landmarkDtos;
	}
	
	/**
	 * Onboard landmark by adding areas and new cities.
	 */
	@Override
	public List<ResponseDto> onboardLandmarks(ExcelSheetObject excelSheetObject) {
		
		List<Object> locationSheetRows = excelSheetObject.getRows();
		List<ResponseDto> responseDto = new ArrayList<ResponseDto>();					//response message insertion
		
		List<String> cityInsertedIds = new ArrayList<String>();
		List<String> areaInsertedIds = new ArrayList<String>();
		List<String> landmarkInsertedIds = new ArrayList<String>();
		List<ResponseDto> response = new ArrayList<ResponseDto>();
		int rowCount = 0;
		Map<String, String> cityDetails = getCityIdName(excelSheetObject.getExcelSheetName());
		
		if(!cityDetails.containsKey(UtilConstants.CITY_ID)) {
			responseDto.add(new ResponseDto(Constants.LOCATION_ID_NULL_DATA_CODE, Constants.LOCATION_ID_NULL_DATA_MESSAGE));
			return responseDto;
		}
		City aCity = new City(cityDetails.get(UtilConstants.CITY_ID), cityDetails.get(UtilConstants.CITY_NAME));
		if(!cityPresent(aCity.getCityId())) {				//new City so insert it into DB.
			
			try {
				cityRepository.saveAndFlush(aCity);
				cityInsertedIds.add(aCity.getCityId());
			} catch (Exception e) {
				String errorResponse = Util.generateErrorString(rowCount, Constants.LOGGER_ERROR, e.getMessage());
				response.add(new ResponseDto(Constants.DATABASE_ERROR, errorResponse));
			}
		}
		
		for(Object anObject : locationSheetRows) {
			rowCount++;
			ExcelLocationDto anExcelLocationDto = (ExcelLocationDto)anObject;
			String areaId = anExcelLocationDto.getAreaId();
			String areaName = anExcelLocationDto.getAreaName();
			String landmarkId = anExcelLocationDto.getLandmarkId();
			String landmarkName = anExcelLocationDto.getLandmarkName();
			//AreaIdCity areaIdCityId = new AreaIdCity(areaId, aCity);
			//AreaIdCityId areaIdCityId = new AreaIdCityId(areaId, aCity);
			Area anArea = new Area(areaId, areaName, aCity);
			if(!areaPresent(anArea.getAreaId()) && !areaInsertedIds.contains(areaId)) {				//create a new area if does't exist
				try {
					areaRepository.saveAndFlush(anArea);
					areaInsertedIds.add(anArea.getAreaId());
				} catch (Exception e) {
					String errorResponse = Util.generateErrorString(rowCount, Constants.LOGGER_ERROR, e.getMessage());
					response.add(new ResponseDto(Constants.DATABASE_ERROR, errorResponse));
				}
			}
			//LandmarkIdArea landmarkIdArea = new LandmarkIdArea(landmarkId, anArea);
			//LandmarkIdAreaId landmarkIdAreaId = new LandmarkIdAreaId(landmarkId, anArea);
			Landmark anLandmark = new Landmark(landmarkId, landmarkName, anArea);
			try {
				if(landmarkPresent(anLandmark.getLandmarkId()) || landmarkInsertedIds.contains(anLandmark.getLandmarkId())) {
					String errorContent = "LandmarkId " + Constants.DATABASE_ERROR_KEY_PRESENT;
					String errorResponse = Util.generateErrorString(rowCount, Constants.LOGGER_ERROR, errorContent);
					response.add(new ResponseDto(Constants.DATABASE_ERROR, errorResponse));
					continue;
				}
				landmarkRepository.saveAndFlush(anLandmark);
				landmarkInsertedIds.add(anLandmark.getLandmarkId());
			} catch (Exception e) {
				String errorResponse = Util.generateErrorString(rowCount, Constants.LOGGER_ERROR, e.getCause().getMessage());
				response.add(new ResponseDto(Constants.DATABASE_ERROR, errorResponse));
			}
		}
		
		// delete the ID's if there are some problems.
		if(!response.isEmpty()) {
			for(String alandmarkInserted : landmarkInsertedIds) {
				landmarkRepository.delete(alandmarkInserted);
			}
			for(String aAreaInserted : areaInsertedIds) {
				areaRepository.delete(aAreaInserted);
			}
			for(String aCityInsertedId : cityInsertedIds) {
				cityRepository.delete(aCityInsertedId);
			}
			
		} else {			// no error send success message
			String successResponse = Constants.SUCCESS_RESPONSE_MESSAGE + ". Records Insserted :" + rowCount;
			response.add(new ResponseDto(Constants.SUCCESS_RESPONSE_CODE, successResponse));
		}
		return response;
	}
	
	
	private Boolean cityPresent(String cityId) {
		return cityRepository.findOne(cityId) == null ? false : true;
	}
	
	private Boolean areaPresent(String areaId) {
		return areaRepository.findOne(areaId) == null ? false : true;
	}
	
	
	/**
	 * Get formatted cityId with name
	 * @param cityIdWithName
	 * @return
	 */
	private Map<String, String> getCityIdName(String cityIdWithName) {
		
		String[] cityDetails  = cityIdWithName.split(UtilConstants.CITY_ID_NAME_DELIMETER);
		Map<String, String> cityDetailsMap = new HashMap<String, String>();
		
		if(cityDetails == null)
			return cityDetailsMap;
		
		if(cityDetails.length == 1) { 
			cityDetailsMap.put(UtilConstants.CITY_NAME, cityDetails[0]);
		} else if (cityDetails.length == 2){
			cityDetailsMap.put(UtilConstants.CITY_NAME, cityDetails[0]);
			cityDetailsMap.put(UtilConstants.CITY_ID, cityDetails[1]);
		}
		return cityDetailsMap;
	}
	
}
