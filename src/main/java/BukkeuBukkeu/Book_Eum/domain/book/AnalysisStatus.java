package BukkeuBukkeu.Book_Eum.domain.book;

public enum AnalysisStatus {
    PENDING,        // 분석&음악생성 요청됨, 시작 전
    PROCESSING,     // 분석&음악생성 진행 중
    COMPLETED,      // 분석&음악생성 생성 완료
    FAILED          // 분석&음악생성 실패
}
