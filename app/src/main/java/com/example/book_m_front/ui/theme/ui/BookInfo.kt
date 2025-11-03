package com.example.book_m_front.ui.theme.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.book_m_front.R
import com.example.book_m_front.ui.theme.ui_resource.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookInfo(bookTitle: String, bookAuthor: String, bookPublisher: String, keywords: List<String>,
             plot: String, bookImage: Painter, bookTable: String){
    //일단 Painter로 받는걸로 해뒀는데 흠~~ 인수줄때 헷갈리지 않으려나 쬠 걱정!
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(R.drawable.minilogo),
                        contentDescription = null
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }

            )
        },
        modifier = Modifier.background(color = AppColors.White)
    ){
        innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(30.dp)
                .fillMaxHeight()
        ){
            //책 제목
            BookTitle(bookTitle)

            //책 이미지 및 작가, 출판사
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 20.dp)
            ){
                BookImage(bookImage)
                Spacer(modifier = Modifier.width(15.dp))
                Column(){
                    AuthorPublisher(bookAuthor, bookPublisher)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(60.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalArrangement = Arrangement.Top
                    ) {
                        items(keywords) { keyword ->
                            Keyword(keyword)
                        }
                    }
                }
            }

            //줄거리
            BookPlot(plot)

            //목차
            BookTable(bookTable)

        }

        //아래 플리 바
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.DeepGreen
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                ) {
                    Text(
                        text = "책과 어울리는 음악",
                        fontSize = 20.sp,
                        color = AppColors.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "showing playlist",
                        tint = AppColors.White,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BookImage(bookImage: Painter){
    Image(
        painter = bookImage,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .height(150.dp)
            .width(100.dp)
    )
}

@Composable
fun BookTitle(bookTitle: String){
    Text(
        text = bookTitle,
        fontSize = 33.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(bottom = 20.dp)
    )
}

@Composable
fun AuthorPublisher(bookAuthor: String, bookPublisher: String){
    Column(){
        //작가
        Row (modifier = Modifier.padding(5.dp)){
            Text(
                text = "작가 | ",
                fontWeight = FontWeight.Bold
            )
            Text(
                text = bookAuthor
            )
        }

        //출판사
        Row (modifier = Modifier.padding(5.dp)){
            Text(
                text = "출판사 | ",
                fontWeight = FontWeight.Bold
            )
            Text(
                text = bookPublisher
            )

        }
    }
}

@Composable
fun BookPlot(plot: String){
    Column(
        modifier = Modifier
            .padding(top = 20.dp, bottom = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.plot),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 5.dp)
        )
        Text(
            text = plot
        )
    }
}

@Composable
fun BookTable(bookTable: String){
    Column(
        modifier = Modifier
            .padding(top = 20.dp, bottom = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.table),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 5.dp)
        )
        Text(
            text = bookTable
        )
    }
}

@Preview
@Composable
fun BookInfoPreview(){
    BookInfo(
        bookTitle = "책 제목",
        bookAuthor = "작가",
        bookPublisher = "출판사",
        plot = "줄거리어쩌고저ㅓㅉ고줄거리어쩌고저ㅓㅉ고줄거리어쩌고저ㅓㅉ고줄거리어쩌고저ㅓㅉ고",
        bookImage = painterResource(R.drawable.deepgreenlogo),
        bookTable = "1.목차1 \n2.목차2\n3.목차3\n4.목차4",
        keywords = listOf("asd","Asdf","123","aaa","vvf")
    )
}



@Composable
fun Keyword(keyword: String) {
    Surface(
        color = AppColors.DeepGreen.copy(alpha = 0.6f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(2.dp),
    ) {
        Text(
            text = "# "+keyword,
            fontSize = 15.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            textAlign = TextAlign.Center
        )
    }
}