package BukkeuBukkeu.Book_Eum.domain.book;

public enum Status {
    NOT_REQUESTED,          // 내용분석&음악생성 요청하지 않음
    ANALYSIS_PROCESSING,    // 내용분석 요청해서 진행중
    ANALYSIS_FAILED,        // 내용분석 실패
    ANALYSIS_COMPLETED,     // 내용분석 완료
    GENERATION_PROCESSING,  // 음악생성 요청해서 진행중
    GENERATION_FAILED,      // 음악생성 실패
    COMPLETED               // 내용분석&음악생성 완료
}
