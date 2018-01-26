package com.leaps.controller;

import java.io.IOException;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.leaps.model.exceptions.InvalidInputParamsException;
import com.leaps.model.exceptions.InvalidParametersException;
import com.leaps.model.utils.CoordinateUtils;
import com.leaps.model.utils.CoordinateUtils.CoordinatesEnum;
import com.leaps.model.utils.LeapsUtils;

@RestController
@RequestMapping("/utils")
@MultipartConfig
public class UtilsController {
	
	private CoordinatesEnum coordinateUtils = CoordinateUtils.CoordinatesEnum.INSTANCE;

	/**
	 * Get coordinates from given address
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/coordinates")
	public String getCoordinates(HttpServletRequest req, HttpServletResponse resp) {
		
		try {
			return coordinateUtils.getCoordinates(LeapsUtils.getRequestData(req)).toString();
		} catch (IOException ioe) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ioe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (InvalidInputParamsException iipe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, iipe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (InvalidParametersException ipe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ipe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
	
	/**
	 * Get address from given coordinates
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/address")
	public String getAddress(HttpServletRequest req, HttpServletResponse resp) {
		try {
			return coordinateUtils.getAddress(LeapsUtils.getRequestData(req)).toString();
		} catch (IOException ioe) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ioe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (InvalidInputParamsException iipe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, iipe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (InvalidParametersException ipe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ipe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
	
	/**
	 * Get address from given coordinates
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/full_address")
	public String getFullAddress(HttpServletRequest req, HttpServletResponse resp) {
		try {
			return coordinateUtils.getFullAddress(LeapsUtils.getRequestData(req)).toString();
		} catch (IOException ioe) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ioe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (InvalidInputParamsException iipe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, iipe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (InvalidParametersException ipe) {
			try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, ipe.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
}
