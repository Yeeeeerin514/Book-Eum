package com.example.book_m_front.util

import android.content.Context
import android.text.Html
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import java.io.FileInputStream

/**
 * Epub 파싱 결과를 담을 데이터 클래스
 * @param title 책의 전체 제목
 * @param chapters 챕터 목록
 */
data class EpubContent(
    val title: String,
    val chapters: List<Chapter>
)

/**
 * 개별 챕터의 정보를 담을 데이터 클래스
 * @param title 챕터 제목
 * @param content 챕터의 본문 내용 (HTML 형식)
 */
data class Chapter(
    val title: String,
    val content: String
)

/**
 * epublib 라이브러리를 사용하여 EPUB 파일을 파싱하는 유틸리티 객체
 */
object SimpleEpubParser {

    /**
     * 지정된 경로의 EPUB 파일을 읽어 EpubContent 객체로 변환합니다.
     *
     * @param context Context (현재 사용되진 않지만 확장성을 위해 남겨둠)
     * @param filePath 파싱할 EPUB 파일의 절대 경로
     * @return 파싱된 데이터를 담은 EpubContent 객체
     * @throws Exception 파일 읽기 또는 파싱 중 오류 발생 시 예외를 던짐
     */
    fun parseEpub(context: Context, filePath: String): EpubContent {
        try {
            // 1. 파일 경로로부터 InputStream을 생성합니다.
            val inputStream = FileInputStream(filePath)

            // 2. EpubReader를 사용하여 InputStream으로부터 Book 객체를 읽어옵니다.
            val book: Book = EpubReader().readEpub(inputStream)

            // 3. 챕터 목록을 순회하며 Chapter 데이터 클래스로 변환합니다.
            val chapters = book.tableOfContents.tocReferences.mapNotNull { tocReference ->
                // tocReference.completeHref를 통해 리소스에 접근합니다.
                val resource = book.resources.getByHref(tocReference.completeHref)
                if (resource != null) {
                    // 챕터 제목을 가져옵니다. 제목이 없으면 "제목 없음"으로 대체합니다.
                    val chapterTitle = tocReference.title?.let {
                        // HTML 태그가 포함된 경우 제거합니다 (예: <i>Title</i> -> Title)
                        Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY).toString()
                    } ?: "제목 없음"

                    // 리소스의 데이터(byte array)를 UTF-8 문자열로 변환하여 챕터 본문을 가져옵니다.
                    val chapterContent = String(resource.data, Charsets.UTF_8)

                    Chapter(title = chapterTitle, content = chapterContent)
                } else {
                    null // 리소스를 찾을 수 없는 경우 해당 챕터는 무시합니다.
                }
            }

            // 4. 최종적으로 EpubContent 객체를 생성하여 반환합니다.
            return EpubContent(
                title = book.title ?: "제목 없음",
                chapters = chapters
            )

        } catch (e: Exception) {
            // 파일이 존재하지 않거나, EPUB 형식이 잘못된 경우 등 모든 예외를 처리합니다.
            e.printStackTrace()
            throw Exception("EPUB 파일을 파싱하는 데 실패했습니다: ${e.message}")
        }
    }
}
