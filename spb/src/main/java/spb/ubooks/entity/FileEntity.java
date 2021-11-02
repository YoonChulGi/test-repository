package spb.ubooks.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="spb_file")
@NoArgsConstructor
@Data
public class FileEntity {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int idx;
	
	@Column(nullable=false)
	private int bookId;
	
	@Column(nullable=false)
	private String originalFileName;
	
	@Column(nullable=false)
	private String storedFilePath;
	
	@Column(nullable=false)
	private long fileSize;
	
	@Column(nullable=false)
	private String creatorId;
	
	@Column(nullable=false)
	private LocalDateTime createdDatetime = LocalDateTime.now();
	
	private String updatorId;
	
	private LocalDateTime updatedDatetime;
	
	@Column(nullable=false)
	private char deletedYn='N';
}
