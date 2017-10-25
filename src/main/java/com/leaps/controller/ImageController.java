package com.leaps.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leaps.model.db.DBUserDao;
import com.leaps.model.exceptions.AuthorizationException;
import com.leaps.model.exceptions.EventException;
import com.leaps.model.exceptions.ImageException;
import com.leaps.model.exceptions.InvalidInputParamsException;
import com.leaps.model.exceptions.UserException;
import com.leaps.model.image.ImageDao;
import com.leaps.model.user.User;
import com.leaps.model.user.UserDao;
import com.leaps.model.utils.Configuration;
import com.leaps.model.utils.LeapsUtils;

@RestController
public class ImageController {
	
	/**
	 * User picture insert method
	 * 
		FORM DATA:
		
		key : user_id, value: 123
		key: image, value: MultipartFile
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/pic/user")
	public String addUserPicture(@RequestParam("image") MultipartFile multiPartFile, Model model, @RequestParam("user_id") String userIdParam, HttpServletRequest req, HttpServletResponse resp) {
		long userId;
		int imageId;
		
		InputStream inputStream = null;
	    OutputStream outputStream = null;
	    
		try {
			String token = req.getHeader("Authorization");
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			long checker = Long.valueOf(token);
			
			if (UserDao.getInstance().getUserFromCache(checker) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			if (!LeapsUtils.isNumber(userIdParam)) {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			userId = Long.valueOf(userIdParam);
			
			if ((DBUserDao.getInstance().getAllUserImages(userId).size() + 1) > Configuration.MAX_USER_IMAGE_COUNT) {
				throw new ImageException(Configuration.IMAGE_LIMIT_FOR_USER_IS_REACHED);
			}
			
		    String suffix = LeapsUtils.getFileExtension(multiPartFile.getOriginalFilename());
		    String fileName = String.valueOf(Configuration.USER_IMAGE_PATH + System.currentTimeMillis()) + suffix;
		    			
			
		    File newFile = new File(Configuration.IMAGE_START_PATH + fileName); 
		    
	    	imageId = DBUserDao.getInstance().insertUserImageIntoDB(userId, fileName);
	    	
	    	if (imageId < 0) {
		    	throw new ImageException(Configuration.CANNOT_INSERT_IMAGE_INTO_DATABASE);
		    }
	    	
	        inputStream = multiPartFile.getInputStream();

		    if (!newFile.getParentFile().exists()) {
		    	newFile.getParentFile().mkdirs();
		    }
	        
	        if (!newFile.exists()) {
	            newFile.createNewFile();
	        }
	        
	        outputStream = new FileOutputStream(newFile);
	        
	        int read = 0;
	        byte[] bytes = new byte[1024];

	        while ((read = inputStream.read(bytes)) != -1) {
	            outputStream.write(bytes, 0, read);
	        }
		    
		    JsonObject response = new JsonObject();
		    response.addProperty("image_id", imageId);
		    response.addProperty("url", fileName);

		    return response.toString();
	    } catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (InvalidInputParamsException iip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, iip.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (ImageException ie) {
	    	try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ie.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    } finally {
	    	try {
	    		if (outputStream != null) {
					outputStream.close();
	    		}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}
	
	/**
	 * User main picture insert method
	 * 
		FORM DATA:
		
		key : user_id, value: 123
		key: image, value: MultipartFile
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/pic/user/main")
	public String addMainUserPicture(@RequestParam("image") MultipartFile multiPartFile, Model model, @RequestParam("user_id") String userIdParam, HttpServletRequest req, HttpServletResponse resp) {
		long userId;
		
		InputStream inputStream = null;
	    OutputStream outputStream = null;
	    
		try {
			String token = req.getHeader("Authorization");
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			long checker = Long.valueOf(token);
			
			if (UserDao.getInstance().getUserFromCache(checker) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			if (!LeapsUtils.isNumber(userIdParam)) {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			userId = Long.valueOf(userIdParam);
			
			if ((DBUserDao.getInstance().getAllUserImages(userId).size() + 1) > Configuration.MAX_USER_IMAGE_COUNT) {
				throw new ImageException(Configuration.IMAGE_LIMIT_FOR_USER_IS_REACHED);
			}
			
		    String suffix = LeapsUtils.getFileExtension(multiPartFile.getOriginalFilename());
		    String fileName = String.valueOf(Configuration.USER_IMAGE_PATH + System.currentTimeMillis()) + suffix;
		    			
			
		    File newFile = new File(Configuration.IMAGE_START_PATH + fileName); 
		    
	    	if (!DBUserDao.getInstance().insertUserMainImageIntoDB(userId, fileName)) {
		    	throw new ImageException(Configuration.CANNOT_INSERT_IMAGE_INTO_DATABASE);
		    }
	    	
	    	User user = DBUserDao.getInstance().getUserFromDbById(userId);
	    	
	    	// update the parameter in the cache
	    	UserDao.getInstance().updateUserInCache(user, checker);
	    	
	        inputStream = multiPartFile.getInputStream();

		    if (!newFile.getParentFile().exists()) {
		    	newFile.getParentFile().mkdirs();
		    }
	        
	        if (!newFile.exists()) {
	            newFile.createNewFile();
	        }
	        
	        outputStream = new FileOutputStream(newFile);
	        
	        int read = 0;
	        byte[] bytes = new byte[1024];

	        while ((read = inputStream.read(bytes)) != -1) {
	            outputStream.write(bytes, 0, read);
	        }
		    
		    return HttpStatus.OK.toString();
	    } catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (InvalidInputParamsException iip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, iip.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (UserException ue) {
	    	try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
	    } catch (ImageException ie) {
	    	try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ie.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    } finally {
	    	try {
	    		if (outputStream != null) {
					outputStream.close();
	    		}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}
	
	// TEST
	// TODO: to be removed
	@RequestMapping(method = RequestMethod.POST, value = "/pic/user/main")
	public String test(@RequestParam("image") MultipartFile multiPartFile, Model model, @RequestParam("user_id") String userIdParam, HttpServletRequest req, HttpServletResponse resp) {
		long userId;
		
		InputStream inputStream = null;
	    OutputStream outputStream = null;
	    
		try {
			String token = req.getHeader("Authorization");
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			long checker = Long.valueOf(token);
			
			if (UserDao.getInstance().getUserFromCache(checker) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			if (!LeapsUtils.isNumber(userIdParam)) {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			userId = Long.valueOf(userIdParam);
			
			if ((DBUserDao.getInstance().getAllUserImages(userId).size() + 1) > Configuration.MAX_USER_IMAGE_COUNT) {
				throw new ImageException(Configuration.IMAGE_LIMIT_FOR_USER_IS_REACHED);
			}
			
		    String suffix = LeapsUtils.getFileExtension(multiPartFile.getOriginalFilename());
		    String fileName = String.valueOf(Configuration.USER_IMAGE_PATH + System.currentTimeMillis()) + suffix;
		    			
			
		    File newFile = new File(Configuration.IMAGE_START_PATH + fileName); 
		    
	    	if (!DBUserDao.getInstance().insertUserMainImageIntoDB(userId, fileName)) {
		    	throw new ImageException(Configuration.CANNOT_INSERT_IMAGE_INTO_DATABASE);
		    }
	    	
	    	User user = DBUserDao.getInstance().getUserFromDbById(userId);
	    	
	    	// update the parameter in the cache
	    	UserDao.getInstance().updateUserInCache(user, checker);
	    	
	        inputStream = multiPartFile.getInputStream();

		    if (!newFile.getParentFile().exists()) {
		    	newFile.getParentFile().mkdirs();
		    }
	        
	        if (!newFile.exists()) {
	            newFile.createNewFile();
	        }
	        
	        outputStream = new FileOutputStream(newFile);
	        
	        int read = 0;
	        byte[] bytes = new byte[1024];

	        while ((read = inputStream.read(bytes)) != -1) {
	            outputStream.write(bytes, 0, read);
	        }
		    
		    return HttpStatus.OK.toString();
	    } catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (InvalidInputParamsException iip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, iip.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (ImageException ie) {
	    	try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ie.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
	    } catch (UserException ue) {
	    	try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    } finally {
	    	try {
	    		if (outputStream != null) {
					outputStream.close();
	    		}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}
	// END TEST METHOD
	
	/**
	 * User picture download method
	 * 
		{
		  "image_id" : 423423;
		}
	 */
	@RequestMapping(value="Images/Users/{fileName}.{ext}", method=RequestMethod.GET)
    @ResponseBody
    public void downloadUserPicture(@PathVariable("fileName") String fileName, @PathVariable("ext") String ext, HttpServletResponse resp, Model model) throws IOException {
        File file = new File(Configuration.IMAGE_START_PATH + Configuration.USER_IMAGE_PATH + fileName + "." + ext);
        try {
        	Files.copy(file.toPath(), resp.getOutputStream());
        } catch (Exception e) {
        	try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, Configuration.IMAGE_DOES_NOT_EXISTS);
			} catch (IOException ioe) {}
        }
    }
	
	/**
	 * User picture delete method
	 * 
		{
		  "image_id" : 423423;
		}
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/pic/user")
	public String deleteUserPicture(HttpServletRequest req, HttpServletResponse resp) {
		int imageId;
		try (Scanner sc = new Scanner(req.getInputStream())) {
			String token = req.getHeader("Authorization");
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			long checker = Long.valueOf(token);

			if (UserDao.getInstance().getUserFromCache(checker) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}
			
			String requestData = sb.toString();
			
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();

			if (obj.get("image_id") == null) {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			imageId = obj.get("image_id").getAsInt();
			
			ImageDao.getInstance().deleteUserImageFromServerAndDB(imageId);
			
			return HttpStatus.OK.toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (InvalidInputParamsException iip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, iip.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (ImageException ie) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ie.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (IOException e) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
	
	
	/**
	 * Event picture insert method
	 * 
		FORM DATA:
		
		key : event_id, value: 123
		key: image, value: MultipartFile
		
	 */	
	@RequestMapping(method = RequestMethod.PUT, value = "/pic/event")
	public String addEventPicture(@RequestParam("image") MultipartFile multiPartFile, Model model, @RequestParam("event_id") String eventIdParam, HttpServletRequest req, HttpServletResponse resp) {
		long eventId;
		int imageId;
		
		InputStream inputStream = null;
	    OutputStream outputStream = null;
	    
		try {
			String token = req.getHeader("Authorization");
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			long checker = Long.valueOf(token);
			
			if (UserDao.getInstance().getUserFromCache(checker) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			if (!LeapsUtils.isNumber(eventIdParam)) {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			eventId = Long.valueOf(eventIdParam);
			
			if ((DBUserDao.getInstance().getAllEventImages(eventId).size() + 1) > Configuration.MAX_EVENT_IMAGE_COUNT) {
				throw new ImageException(Configuration.IMAGE_LIMIT_FOR_EVENT_IS_REACHED);
			}
			
		    String suffix = LeapsUtils.getFileExtension(multiPartFile.getOriginalFilename());
		    String fileName = String.valueOf(Configuration.EVENT_IMAGE_PATH + System.currentTimeMillis()) + suffix;
		    			
			
		    File newFile = new File(Configuration.IMAGE_START_PATH + fileName); 
		    
	    	imageId = DBUserDao.getInstance().insertEventImageIntoDB(eventId, fileName);
	    	
	    	if (imageId < 0) {
		    	throw new ImageException(Configuration.CANNOT_INSERT_IMAGE_INTO_DATABASE);
		    }
	    	
	        inputStream = multiPartFile.getInputStream();

		    if (!newFile.getParentFile().exists()) {
		    	newFile.getParentFile().mkdirs();
		    }
	        
	        if (!newFile.exists()) {
	            newFile.createNewFile();
	        }
	        
	        outputStream = new FileOutputStream(newFile);
	        
	        int read = 0;
	        byte[] bytes = new byte[1024];

	        while ((read = inputStream.read(bytes)) != -1) {
	            outputStream.write(bytes, 0, read);
	        }
		    
		    JsonObject response = new JsonObject();
		    response.addProperty("image_id", imageId);
		    response.addProperty("url", fileName);

		    return response.toString();
	    } catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (InvalidInputParamsException iip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, iip.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}catch (ImageException ie) {
	    	try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ie.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    } finally {
	    	try {
	    		if (outputStream != null) {
					outputStream.close();
	    		}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}
	
	/**
	 * Event picture insert method
	 * 
		FORM DATA:
		
		key : user_id, value: 123
		key: image, value: MultipartFile
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/pic/event/main")
	public String addMainEventPicture(@RequestParam("image") MultipartFile multiPartFile, Model model, @RequestParam("event_id") String eventIdParam, HttpServletRequest req, HttpServletResponse resp) {
		long eventId;
		
		InputStream inputStream = null;
	    OutputStream outputStream = null;
	    
		try {
			String token = req.getHeader("Authorization");
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			long checker = Long.valueOf(token);
			
			if (UserDao.getInstance().getUserFromCache(checker) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			if (!LeapsUtils.isNumber(eventIdParam)) {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			eventId = Long.valueOf(eventIdParam);
			
			if ((DBUserDao.getInstance().getAllUserImages(eventId).size() + 1) > Configuration.MAX_EVENT_IMAGE_COUNT) {
				throw new ImageException(Configuration.IMAGE_LIMIT_FOR_EVENT_IS_REACHED);
			}
			
		    String suffix = LeapsUtils.getFileExtension(multiPartFile.getOriginalFilename());
		    String fileName = String.valueOf(Configuration.EVENT_IMAGE_PATH + System.currentTimeMillis()) + suffix;
		    			
			
		    File newFile = new File(Configuration.IMAGE_START_PATH + fileName); 
		    
	    	if (!DBUserDao.getInstance().insertEventMainImageIntoDB(eventId, fileName)) {
		    	throw new ImageException(Configuration.CANNOT_INSERT_IMAGE_INTO_DATABASE);
		    }
	    	
	        inputStream = multiPartFile.getInputStream();

		    if (!newFile.getParentFile().exists()) {
		    	newFile.getParentFile().mkdirs();
		    }
	        
	        if (!newFile.exists()) {
	            newFile.createNewFile();
	        }
	        
	        outputStream = new FileOutputStream(newFile);
	        
	        int read = 0;
	        byte[] bytes = new byte[1024];

	        while ((read = inputStream.read(bytes)) != -1) {
	            outputStream.write(bytes, 0, read);
	        }
		    
		    return HttpStatus.OK.toString();
	    } catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (InvalidInputParamsException iip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, iip.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}catch (ImageException ie) {
	    	try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ie.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    } finally {
	    	try {
	    		if (outputStream != null) {
					outputStream.close();
	    		}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}
	
	/**
	 * Event picture download method
	 * 
		{
		  "image_id" : 423423;
		}
	 */
	@RequestMapping(value="Images/Events/{fileName}.{ext}", method=RequestMethod.GET)
    @ResponseBody
    public void downloadEventPicture(@PathVariable("fileName") String fileName, @PathVariable("ext") String ext, HttpServletResponse resp, Model model) throws IOException {
        File file = new File(Configuration.IMAGE_START_PATH + Configuration.EVENT_IMAGE_PATH + fileName + "." + ext);
        try {
        	Files.copy(file.toPath(), resp.getOutputStream());
        } catch (Exception e) {
        	try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, Configuration.IMAGE_DOES_NOT_EXISTS);
			} catch (IOException ioe) {}
        }
    }
	
	/**
	 * Event picture delete method
	 * 
		{
		  "image_id" : 423423;
		}
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/pic/event")
	public String deleteEventPicture(HttpServletRequest req, HttpServletResponse resp) {
		int imageId;
		try (Scanner sc = new Scanner(req.getInputStream())) {
			String token = req.getHeader("Authorization");
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			long checker = Long.valueOf(token);
			
			if (UserDao.getInstance().getUserFromCache(checker) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			StringBuilder sb = new StringBuilder();
			while (sc.hasNext()) {
				sb.append(sc.nextLine());
			}
			
			String requestData = sb.toString();
			
			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(requestData).getAsJsonObject();

			if (obj.get("image_id") == null) {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			imageId = obj.get("image_id").getAsInt();
			
			ImageDao.getInstance().deleteEventImageFromServerAndDB(imageId);
			
			return HttpStatus.OK.toString();
		} catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (InvalidInputParamsException iip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, iip.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (ImageException ie) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ie.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (IOException e) {
			try {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		}
	}
	
	/**
	 * Rate picture insert method
	 * 
		FORM DATA:
		
		key : event_id, value: 72
		key: image, value: MultipartFile
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/pic/rate")
	public String addRatePicture(@RequestParam("image") MultipartFile multiPartFile, Model model, @RequestParam("rate_id") String rateIdParam, HttpServletRequest req, HttpServletResponse resp) {
		long rateId;
		
		InputStream inputStream = null;
	    OutputStream outputStream = null;
	    
		try {
			String token = req.getHeader("Authorization");
			
			if (token == null || token.isEmpty() || !LeapsUtils.isNumber(token)) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			long checker = Long.valueOf(token);
			
			if (UserDao.getInstance().getUserFromCache(checker) == null) {
				throw new AuthorizationException(Configuration.INVALID_OR_EXPIRED_TOKEN);
			}
			
			if (!LeapsUtils.isNumber(rateIdParam)) {
				throw new InvalidInputParamsException(Configuration.INVALID_INPUT_PAREMETERS);
			}
			
			rateId = Long.valueOf(rateIdParam);
			
		    String suffix = LeapsUtils.getFileExtension(multiPartFile.getOriginalFilename());
		    String fileName = String.valueOf(Configuration.RATE_IMAGE_PATH + System.currentTimeMillis()) + suffix;
			
		    File newFile = new File(Configuration.IMAGE_START_PATH + fileName); 
		    
		    // insert image into db
	    	DBUserDao.getInstance().insertRateImageIntoDB(rateId, fileName);
	    	
	        inputStream = multiPartFile.getInputStream();

		    if (!newFile.getParentFile().exists()) {
		    	newFile.getParentFile().mkdirs();
		    }
	        
	        if (!newFile.exists()) {
	            newFile.createNewFile();
	        }
	        
	        outputStream = new FileOutputStream(newFile);
	        
	        int read = 0;
	        byte[] bytes = new byte[1024];

	        while ((read = inputStream.read(bytes)) != -1) {
	            outputStream.write(bytes, 0, read);
	        }
		    
		    JsonObject response = new JsonObject();
		    response.addProperty("image_id", rateId);
		    response.addProperty("url", fileName);

		    return response.toString();
	    } catch (AuthorizationException ae) {
			try {
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ae.getMessage());
				return null;
			} catch (IOException ioe) {
				return null;
			}
		} catch (InvalidInputParamsException iip) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, iip.getMessage());
				return null;
			} catch (IOException ioe2) {
				return null;
			}
		} catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    } catch (EventException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} finally {
	    	try {
	    		if (outputStream != null) {
					outputStream.close();
	    		}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}
	
	/**
	 * Rate picture download method
	 * 
		{
		  "image_id" : 423423;
		}
	 */
	@RequestMapping(value="Images/Rate/{fileName}.{ext}", method=RequestMethod.GET)
    @ResponseBody
    public void downloadRatePicture(@PathVariable("fileName") String fileName, @PathVariable("ext") String ext, HttpServletResponse resp, Model model) throws IOException {
        File file = new File(Configuration.IMAGE_START_PATH + Configuration.RATE_IMAGE_PATH + fileName + "." + ext);
        try {
        	Files.copy(file.toPath(), resp.getOutputStream());
        } catch (Exception e) {
        	try {
				resp.sendError(HttpServletResponse.SC_CONFLICT, Configuration.IMAGE_DOES_NOT_EXISTS);
			} catch (IOException ioe) {}
        }
    }
}
