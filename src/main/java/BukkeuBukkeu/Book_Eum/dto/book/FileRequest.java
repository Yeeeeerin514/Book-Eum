package BukkeuBukkeu.Book_Eum.dto.book;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Fast API에게 요청할 때 보낼 epub 파일 링크

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileRequest {

    private String filePath;  // Fast API의 file_path와 매핑
}