package util;
import java.io.File;
import java.util.Objects;

import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class TelegramUtil{
	public static java.io.File downloadPhotoByFilePath(String filePath,DefaultAbsSender sender) {
	    try {
	        // Download the file calling AbsSender::downloadFile method
	        return sender.downloadFile(filePath);
	    } catch (TelegramApiRequestException e) {
	        e.printStackTrace();
	    } catch (org.telegram.telegrambots.meta.exceptions.TelegramApiException e) {
			e.printStackTrace();
		}
	    return null;
	}
	@SuppressWarnings("null")
	public static String getFilePath(Document doc, AbsSender sender) throws TelegramApiException {
	    Objects.requireNonNull(doc);

	        // We create a GetFile method and set the file_id from the photo
	        GetFile getFileMethod = new GetFile();
	        getFileMethod.setFileId(doc.getFileId());
	        // We execute the method using AbsSender::getFile method.
//	           File file = sender.getFile(getFileMethod);
			//FIXME
			File file = null;
			// We now have the file_path
			return file.getPath();
	}
	public static String ToHTML(String arg) {
		return 
				"<code>"
				+arg
					.replaceAll("&", "&amp")
					.replaceAll("<", "&lt;")
					.replaceAll(">", "&gt;")
				+"</code>";
	}
}
