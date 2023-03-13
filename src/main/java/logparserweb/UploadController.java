package logparserweb;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//import com.coverity.annotations.*;
import eventEngine.CompoundEvent;


@Controller
public class UploadController {

	private static final Logger logger = Logger.getLogger(CompoundEvent.class.getName());

	public String checkSafeFileName(String fileName)
	{


	if (fileName != null && (fileName.indexOf("../") >0 || fileName.indexOf("$") > 0|| fileName.indexOf("*") > 0))
		return null;

		return fileName;
	}


	@Autowired
	FileManagerService fileService;

	// Save the uploaded file to this folder
	private static String UPLOADED_FOLDER = "C://Tmp//logparser//";

	@RequestMapping("/upload")
	public String singleFileUpload(@RequestParam("filename") MultipartFile filename, @RequestParam("description") String description,
								   RedirectAttributes redirectAttributes) {

		try {

			if (filename.isEmpty()) {
				redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
				return "redirect:uploadStatus";
			}

			// Get the file and save it somewhere
			byte[] bytes = filename.getBytes();

			//String safeName = checkSafeFileName(filename.getOriginalFilename());
			String safeName=filename.getOriginalFilename(); 
		
	
			if (safeName == null)
			{
				throw new IOException("Invalid Path");
			}
			else {

				
				Path path = Paths.get(UPLOADED_FOLDER + safeName);   //--- First, let's used some tainted data
				//Path path = Paths.get("/var/tmp/"); 

				Files.write(path, bytes);
				Long fileId=fileService.createFile(filename.getOriginalFilename(),description);
				Path newPath = Paths.get(UPLOADED_FOLDER + "log_"+fileId);

				Files.move(path, newPath);
			}


			redirectAttributes.addFlashAttribute("message",
					"You successfully uploaded '" + filename.getOriginalFilename() + "'");

		} catch (IOException e) {
			e.printStackTrace();
		}

		return "redirect:/uploadStatus";
	}

	@GetMapping("/uploadStatus")
	@SuppressWarnings("fallthrough")
	public String uploadStatus() {
		int kate=0;
		switch (kate)
		{
			case 0:
			{
				logger.log(Level.INFO,"bob is 0");
			}
			case 1:
			{
				logger.log(Level.SEVERE,"bob is 1:");
				break;
			}
		}

		return "uploadStatus";
	}

}
