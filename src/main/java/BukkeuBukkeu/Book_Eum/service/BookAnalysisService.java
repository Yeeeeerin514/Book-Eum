package BukkeuBukkeu.Book_Eum.service;

import BukkeuBukkeu.Book_Eum.domain.Book;

public interface BookAnalysisService {

    /**
     * 방금 등록된 Book을 분석을 요청
     * LLM/음악 생성 파이프라인 호출
     */
    void requestBookAnalysis(String isbn, String epubFileUrl);
}
