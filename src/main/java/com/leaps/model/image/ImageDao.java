package com.leaps.model.image;

import java.io.File;

import com.leaps.interfaces.IImageDao;
import com.leaps.model.db.DBUserDao;
import com.leaps.model.exceptions.ImageException;
import com.leaps.model.utils.Configuration;

public class ImageDao implements IImageDao {
	private static ImageDao instance = null;

	protected ImageDao() {
	}

	public static ImageDao getInstance() {
		if (instance == null) {
			instance = new ImageDao();
		}
		return instance;
	}
	
	public Image createNewImage(long imageId, long eventId, String imageName) {
		return new Image(imageId, eventId, imageName);
	}

	public boolean deleteUserImageFromServerAndDB(int imageId) throws ImageException {
		boolean success = true;
		String imageName;
		
		try {
			imageName = DBUserDao.getInstance().getUserImageNameById(imageId);
			
			if (imageName == null || !DBUserDao.getInstance().removeUserImageFromDB(imageId)) {
				throw new ImageException(Configuration.CANNOT_REMOVE_IMAGE_FROM_DB);
			}
			
			File file = new File(Configuration.IMAGE_START_PATH + imageName);
			
    		if (!file.delete()) {
    			throw new ImageException(Configuration.CANNOT_REMOVE_IMAGE_FROM_SERVER);
    		}
		} catch (ImageException ie) {
			throw new ImageException(ie.getMessage());
		}
		
		return success;
	}

	public boolean deleteEventImageFromServerAndDB(int imageId) throws ImageException {
		boolean success = true;
		String imageName;
		
		try {
			imageName = DBUserDao.getInstance().getEventImageNameById(imageId);
			
			if (imageName == null || !DBUserDao.getInstance().removeEventImageFromDB(imageId)) {
				throw new ImageException(Configuration.CANNOT_REMOVE_IMAGE_FROM_DB);
			}
			
			File file = new File(Configuration.IMAGE_START_PATH + imageName);
			
    		if (!file.delete()) {
    			throw new ImageException(Configuration.CANNOT_REMOVE_IMAGE_FROM_SERVER);
    		}
		} catch (ImageException ie) {
			throw new ImageException(ie.getMessage());
		}
		
		return success;
	}
}
