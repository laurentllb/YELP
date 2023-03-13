package logparserweb;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "files")
public class LogFile {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@NotNull
	private String originalFileName;
	
 
	private String fileName;
	
	@NotNull
	private String description;
	
	public LogFile()
	{
		
		
	}
	
	public LogFile(String originalFileName,String description)
	{
		this.originalFileName=originalFileName;
		this.description=description;
		
	}

	public long getId() {
		return id;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public String getFileName() {
		return fileName;
	}

	public String getDescription() {
		return description;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getMyString() {
			return null;
	}

	public String justDoIt() {
		String A = this.getMyString();
		return A;
	}

	
}
