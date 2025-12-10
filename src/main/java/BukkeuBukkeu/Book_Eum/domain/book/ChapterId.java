package BukkeuBukkeu.Book_Eum.domain.book;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

// Chapter의 복합 PK를 위한 엔티티

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChapterId implements Serializable {

    private String isbn;
    private Integer chapterNum;
}
