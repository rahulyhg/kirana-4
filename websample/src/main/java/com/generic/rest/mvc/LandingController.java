package com.generic.rest.mvc;

import static com.generic.core.utilities.Util.getSessionStoreHouse;
import static com.generic.core.utilities.Util.saveSessionStoreHouse;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.generic.core.cache.SizeCache;
import com.generic.core.services.serviceimpl.ServicesFactory;
import com.generic.rest.constants.SessionAttributes;
import com.generic.rest.constants.SessionStoreConstants;
import com.generic.rest.dto.CategoryDto;
import com.generic.rest.dto.CityDto;
import com.generic.rest.dto.ItemDto;
import com.generic.rest.dto.LocationDto;
import com.generic.rest.dto.ShopDto;
import com.generic.rest.dto.ShopLandmarkDto;

@Controller
@RequestMapping("/rest/landing")
public class LandingController {

	@Resource
	ServicesFactory serviceFactory;

	/**
	 * Get list of cities available
	 * URI : $contextConfigLocation/rest/landing/cities
	 * @return
	 */
	@RequestMapping(value="cities", produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<CityDto> getCities(){
		return serviceFactory.getCityService().getAllCities();
	}
	
	/**
	 * Find all the locations available for a shop
	 * @param cityId
	 * @return
	 */
	@RequestMapping(value="shops/location/{cityId}", produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, ShopLandmarkDto> concatenateShopsLocation(@PathVariable String cityId) {
		return serviceFactory.getShopsLandmarkService().findShopsConcadinatedWithLocation(cityId);
	}
	
	/**
	 * Gets list of areas belonging to a particular city
	 * URI : $contextConfigLocation/rest/landing/areas/{cityId}
	 * @param cityId
	 * @return
	 */
	@Deprecated
	@RequestMapping(value="areas/{cityId}", produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<LocationDto> getAreas(@PathVariable String cityId) {
		//return serviceFactory.getLocationService().findByParentLocation(cityId);
		return null;
	}
	
	/**
	 * Gets list of landmark available for an area
	 * URI : $contextConfigLocation/rest/landing/landmark/{areaId}
	 * @param areaId
	 * @return
	 */
	@Deprecated
	@RequestMapping(value="landmark/{areaId}", produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<LocationDto> getLandmark(@PathVariable String areaId) {
		//return serviceFactory.getLocationService().findByParentLocation(areaId);
		return null;
	}
	
	@Deprecated
	@RequestMapping(value="shoptype/{landmarkId}", produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<LocationDto> getShopType(@PathVariable String landmarkId) {
		
		//return serviceFactory.getShopsLocationService().findShopTypeByLocation(landmarkId);
		return null;
	}
	/**
	 * Gets shops for a particular landmark.
	 * URI : $contextConfigLocation/rest/landing/shops/landmark/{landmarkId}/{shopType}
	 * @param landmarkId
	 * @return
	 */
	@Deprecated
	@RequestMapping(value="shops/landmark/{landmarkId}/{shopType}", produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<ShopDto> getShopsByLandmark(@PathVariable String landmarkId, @PathVariable String shopType) {
		
		//return serviceFactory.getShopsLocationService().findShopsByLocationAndShopType(landmarkId, shopType);
		return null;
		
	}
	
	
	/**
	 * Fing all items for a Shop
	 * URI : $contextConfigLocation/rest/landing/shops/items/{shopId}
	 * 
	 * @param shopId
	 * @return
	 */
	@RequestMapping(value="/shops/items/{shopId}", produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<CategoryDto, Map<CategoryDto, List<ItemDto>>> getItemsForAShop(@PathVariable String shopId, HttpSession session) {
		SessionAttributes sessionAttributes = getSessionStoreHouse(session);
		sessionAttributes.setShopId(shopId);													//setting the shop for the user
		session.setAttribute(SessionStoreConstants.SHOP_SELECTED, new Boolean("true"));			//checkpoint that shop has been selected
		saveSessionStoreHouse(session, sessionAttributes);
		return serviceFactory.getShopsItemsService().findAllItemsForAShop(shopId);
		
	}
	
	
	
	/**
	 * Gets list of all locations sizes available
	 * URI : $contextConfigLocation/rest/landing/permissible/size
	 * @return
	 */
	@RequestMapping(value="/permissible/size", produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String[]> getPermissibleMeasurementQuantities() {
		SizeCache sizeCache = SizeCache.getInstance();
		return sizeCache.getSizeQuantity();
	}
	
}
