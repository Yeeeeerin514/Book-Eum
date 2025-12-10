package com.example.book_m_front.util

import android.content.Context
import android.text.Html
import android.util.Log
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import java.io.FileInputStream
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

/**
 * EPUB 콘텐츠를 담는 데이터 클래스
 */
data class EpubContent(
    val title: String,              // 책 제목
    val author: String?,            // 저자
    val chapters: List<Chapter>     // 챕터 목록
)

/**
 * 챕터 정보를 담는 데이터 클래스
 */
data class Chapter(
    val title: String,              // 챕터 제목
    val content: String,            // 챕터 내용 (HTML)
    val href: String? = null        // 원본 파일 경로 (선택적)
)


/**
 * 🔧 안전한 EPUB 파서 (두 가지 방식 시도)
 *
 * 1차: 표준 EPUB 파서 시도
 * 2차: 간단한 폴백 파서 시도
 */
object SafeEpubParser {

    private const val TAG = "SafeEpubParser"

    /**
     * EPUB 파일 파싱
     *
     * @param context Android Context
     * @param epubFilePath EPUB 파일 경로
     * @return EpubContent
     * @throws Exception 모든 파싱 시도 실패 시
     */
    fun parseEpub(context: Context, epubFilePath: String): EpubContent {
        Log.d(TAG, "📖 EPUB 파싱 시작: $epubFilePath")

        // 1차 시도: 표준 EPUB 파서
        try {
            Log.d(TAG, "1차 시도: 표준 EPUB 파서")
            val result = SimpleEpubParser.parseEpub(context, epubFilePath)
            Log.d(TAG, "✅ 표준 파서 성공!")
            return result
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ 표준 파서 실패: ${e.message}")
            e.printStackTrace()
        }

        // 2차 시도: 간단한 폴백 파서
        try {
            Log.d(TAG, "2차 시도: 폴백 EPUB 파서")
            val result = SimpleFallbackEpubParser.parseEpub(context, epubFilePath)
            Log.d(TAG, "✅ 폴백 파서 성공!")
            return result
        } catch (e: Exception) {
            Log.e(TAG, "❌ 폴백 파서도 실패: ${e.message}")
            e.printStackTrace()
        }

        // 모든 시도 실패
        throw Exception(
            "EPUB 파일을 파싱할 수 없습니다. " +
                    "파일이 손상되었거나 지원되지 않는 형식일 수 있습니다."
        )
    }
}


/**
 * epublib 라이브러리를 사용하여 EPUB 파일을 파싱하는 유틸리티 객체
 */

object SimpleEpubParser {

    /**
     * EPUB 파일을 파싱하여 EpubContent로 반환
     *
     * @param context Android Context
     * @param epubFilePath EPUB 파일의 절대 경로
     * @return EpubContent 객체
     * @throws Exception 파싱 실패 시
     */
    fun parseEpub(context: Context, epubFilePath: String): EpubContent {
        val epubFile = File(epubFilePath)

        if (!epubFile.exists()) {
            throw Exception("EPUB 파일을 찾을 수 없습니다: $epubFilePath")
        }

        val zipFile = ZipFile(epubFile)

        try {
            // 1. container.xml에서 content.opf 경로 찾기
            val opfPath = findOpfPath(zipFile)

            // 2. content.opf 파싱
            val opfDoc = parseXml(zipFile, opfPath)

            // 3. 메타데이터 추출
            val title = extractTitle(opfDoc) ?: "제목 없음"
            val author = extractAuthor(opfDoc)

            // 4. Spine에서 읽는 순서 가져오기
            val spineItems = extractSpine(opfDoc)

            // 5. Manifest에서 실제 파일 경로 매핑
            val manifestMap = extractManifest(opfDoc, opfPath)

            // 6. 챕터 내용 추출
            val chapters = spineItems.mapNotNull { itemId ->
                val href = manifestMap[itemId]
                if (href != null) {
                    extractChapter(zipFile, href, opfPath)
                } else {
                    null
                }
            }

            return EpubContent(
                title = title,
                author = author,
                chapters = chapters
            )

        } finally {
            zipFile.close()
        }
    }

    /**
     * container.xml에서 content.opf 경로 찾기
     */
    private fun findOpfPath(zipFile: ZipFile): String {
        val containerEntry = zipFile.getEntry("META-INF/container.xml")
            ?: throw Exception("container.xml을 찾을 수 없습니다")

        val doc = parseXml(zipFile.getInputStream(containerEntry))
        val rootfiles = doc.getElementsByTagName("rootfile")

        if (rootfiles.length == 0) {
            throw Exception("container.xml에 rootfile이 없습니다")
        }

        val fullPath = (rootfiles.item(0) as Element)
            .getAttribute("full-path")

        if (fullPath.isEmpty()) {
            throw Exception("content.opf 경로를 찾을 수 없습니다")
        }

        return fullPath
    }

    /**
     * XML 파일 파싱
     */
    private fun parseXml(zipFile: ZipFile, path: String): Document {
        val entry = zipFile.getEntry(path)
            ?: throw Exception("파일을 찾을 수 없습니다: $path")
        return parseXml(zipFile.getInputStream(entry))
    }

    private fun parseXml(inputStream: InputStream): Document {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val builder = factory.newDocumentBuilder()
        return builder.parse(inputStream)
    }

    /**
     * 책 제목 추출
     */
    private fun extractTitle(doc: Document): String? {
        val titles = doc.getElementsByTagName("dc:title")
        return if (titles.length > 0) {
            titles.item(0).textContent?.trim()
        } else {
            null
        }
    }

    /**
     * 저자 추출
     */
    private fun extractAuthor(doc: Document): String? {
        val creators = doc.getElementsByTagName("dc:creator")
        return if (creators.length > 0) {
            creators.item(0).textContent?.trim()
        } else {
            null
        }
    }

    /**
     * Spine 추출 (읽는 순서)
     */
    private fun extractSpine(doc: Document): List<String> {
        val spine = doc.getElementsByTagName("spine")
        if (spine.length == 0) return emptyList()

        val itemrefs = (spine.item(0) as Element)
            .getElementsByTagName("itemref")

        return (0 until itemrefs.length).mapNotNull { i ->
            val itemref = itemrefs.item(i) as Element
            itemref.getAttribute("idref").takeIf { it.isNotEmpty() }
        }
    }

    /**
     * Manifest 추출 (ID → 파일 경로 매핑)
     */
    private fun extractManifest(doc: Document, opfPath: String): Map<String, String> {
        val manifest = doc.getElementsByTagName("manifest")
        if (manifest.length == 0) return emptyMap()

        val items = (manifest.item(0) as Element)
            .getElementsByTagName("item")

        val opfDir = File(opfPath).parent ?: ""

        return (0 until items.length).associate { i ->
            val item = items.item(i) as Element
            val id = item.getAttribute("id")
            val href = item.getAttribute("href")

            // 상대 경로를 절대 경로로 변환
            val fullHref = if (opfDir.isNotEmpty()) {
                "$opfDir/$href"
            } else {
                href
            }

            id to fullHref
        }
    }

    /**
     * 챕터 내용 추출
     */
    private fun extractChapter(
        zipFile: ZipFile,
        href: String,
        opfPath: String
    ): Chapter? {
        try {
            // URL 디코딩 (공백 등 특수문자 처리)
            val decodedHref = java.net.URLDecoder.decode(href, "UTF-8")

            val entry = zipFile.getEntry(decodedHref)
                ?: zipFile.getEntry(href)
                ?: return null

            val content = zipFile.getInputStream(entry).bufferedReader().use {
                it.readText()
            }

            // HTML에서 제목 추출 시도
            val title = extractChapterTitle(content)
                ?: File(href).nameWithoutExtension

            return Chapter(
                title = title,
                content = content,
                href = href
            )

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * HTML 콘텐츠에서 챕터 제목 추출
     */
    private fun extractChapterTitle(htmlContent: String): String? {
        // <title> 태그에서 제목 추출
        val titleRegex = """<title>(.*?)</title>""".toRegex(RegexOption.IGNORE_CASE)
        titleRegex.find(htmlContent)?.groupValues?.get(1)?.let {
            return it.trim()
        }

        // <h1> 태그에서 제목 추출
        val h1Regex = """<h1[^>]*>(.*?)</h1>""".toRegex(RegexOption.IGNORE_CASE)
        h1Regex.find(htmlContent)?.groupValues?.get(1)?.let {
            return stripHtmlTags(it).trim()
        }

        // <h2> 태그에서 제목 추출
        val h2Regex = """<h2[^>]*>(.*?)</h2>""".toRegex(RegexOption.IGNORE_CASE)
        h2Regex.find(htmlContent)?.groupValues?.get(1)?.let {
            return stripHtmlTags(it).trim()
        }

        return null
    }

    /**
     * HTML 태그 제거
     */
    private fun stripHtmlTags(html: String): String {
        return html.replace("""<[^>]*>""".toRegex(), "")
    }

    /**
     * NodeList를 List로 변환하는 확장 함수
     */
    private fun NodeList.asList(): List<Node> {
        return (0 until length).map { item(it) }
    }
}


/**
 * 🔧 간단한 EPUB 파서 (폴백 버전)
 *
 * 복잡한 EPUB 파싱이 실패할 경우를 대비한 간단한 버전
 * 모든 HTML/XHTML 파일을 순서대로 읽어들입니다.
 */
object SimpleFallbackEpubParser {

    /**
     * 간단한 방식으로 EPUB 파싱
     *
     * 1. EPUB 파일의 모든 HTML/XHTML 파일을 찾음
     * 2. 파일명 순서로 정렬
     * 3. 각 파일을 챕터로 처리
     */
    fun parseEpub(context: Context, epubFilePath: String): EpubContent {
        val epubFile = File(epubFilePath)

        if (!epubFile.exists()) {
            throw Exception("EPUB 파일을 찾을 수 없습니다: $epubFilePath")
        }

        val zipFile = ZipFile(epubFile)
        val chapters = mutableListOf<Chapter>()

        try {
            // 제목 추출 시도
            var title = "제목 없음"
            var author: String? = null

            try {
                // content.opf 찾기
                val opfEntry = zipFile.entries().asSequence()
                    .firstOrNull { it.name.endsWith(".opf") }

                if (opfEntry != null) {
                    val opfContent = zipFile.getInputStream(opfEntry)
                        .bufferedReader().use { it.readText() }

                    // 간단한 정규식으로 제목/저자 추출
                    """<dc:title>(.*?)</dc:title>""".toRegex()
                        .find(opfContent)?.groupValues?.get(1)?.let {
                            title = it.trim()
                        }

                    """<dc:creator>(.*?)</dc:creator>""".toRegex()
                        .find(opfContent)?.groupValues?.get(1)?.let {
                            author = it.trim()
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 모든 HTML/XHTML 파일 찾기
            val htmlFiles = zipFile.entries().asSequence()
                .filter { entry ->
                    !entry.isDirectory && (
                            entry.name.endsWith(".html", ignoreCase = true) ||
                                    entry.name.endsWith(".xhtml", ignoreCase = true) ||
                                    entry.name.endsWith(".htm", ignoreCase = true)
                            )
                }
                .sortedBy { it.name } // 파일명 순서로 정렬
                .toList()

            println("📚 발견된 HTML 파일 수: ${htmlFiles.size}")

            // 각 파일을 챕터로 변환
            htmlFiles.forEachIndexed { index, entry ->
                try {
                    val content = zipFile.getInputStream(entry)
                        .bufferedReader().use { it.readText() }

                    // 챕터 제목 추출
                    val chapterTitle = extractTitleFromHtml(content)
                        ?: "Chapter ${index + 1}"

                    println("📄 챕터 ${index + 1}: $chapterTitle (${entry.name})")

                    chapters.add(
                        Chapter(
                            title = chapterTitle,
                            content = content,
                            href = entry.name
                        )
                    )
                } catch (e: Exception) {
                    println("⚠️ 챕터 로드 실패: ${entry.name}")
                    e.printStackTrace()
                }
            }

            if (chapters.isEmpty()) {
                throw Exception("챕터를 찾을 수 없습니다")
            }

            println("✅ EPUB 파싱 완료: $title (챕터 ${chapters.size}개)")

            return EpubContent(
                title = title,
                author = author,
                chapters = chapters
            )

        } finally {
            zipFile.close()
        }
    }

    /**
     * HTML에서 제목 추출
     */
    private fun extractTitleFromHtml(html: String): String? {
        // <title> 태그
        """<title>(.*?)</title>""".toRegex(RegexOption.IGNORE_CASE)
            .find(html)?.groupValues?.get(1)?.let {
                val cleaned = stripHtml(it).trim()
                if (cleaned.isNotEmpty()) return cleaned
            }

        // <h1> 태그
        """<h1[^>]*>(.*?)</h1>""".toRegex(RegexOption.IGNORE_CASE)
            .find(html)?.groupValues?.get(1)?.let {
                val cleaned = stripHtml(it).trim()
                if (cleaned.isNotEmpty()) return cleaned
            }

        // <h2> 태그
        """<h2[^>]*>(.*?)</h2>""".toRegex(RegexOption.IGNORE_CASE)
            .find(html)?.groupValues?.get(1)?.let {
                val cleaned = stripHtml(it).trim()
                if (cleaned.isNotEmpty()) return cleaned
            }

        return null
    }

    /**
     * HTML 태그 제거
     */
    private fun stripHtml(text: String): String {
        return text
            .replace("""<[^>]*>""".toRegex(), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
    }
}
