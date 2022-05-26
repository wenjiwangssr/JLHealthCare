package com.example.healthcare.ui.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.healthcare.R
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy

class PDFViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfview)
        //隐藏底部导航栏
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions

        val pdfView = findViewById<PDFView>(R.id.pdfView)
        pdfView.fromAsset("pdf/demo.pdf")
//            .pages(0, 2, 1, 3, 3, 3) // all pages are displayed by default
            .enableSwipe(true) // allows to block changing pages using swipe
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(0)
//            // allows to draw something on the current page, usually visible in the middle of the screen
//            .onDraw(onDrawListener)
//            // allows to draw something on all pages, separately for every page. Called only for visible pages
//            .onDrawAll(onDrawListener)
//            .onLoad(onLoadCompleteListener) // called after document is loaded and starts to be rendered
//            .onPageChange(onPageChangeListener)
//            .onPageScroll(onPageScrollListener)
//            .onError(onErrorListener)
//            .onPageError(onPageErrorListener)
//            .onRender(onRenderListener) // called after document is rendered for the first time
//            // called on single tap, return true if handled, false to toggle scroll handle visibility
//            .onTap(onTapListener)
//            .onLongPress(onLongPressListener)
            .enableAnnotationRendering(true) // render annotations (such as comments, colors or forms)
            .password(null)
            .scrollHandle(DefaultScrollHandle(this))
            .enableAntialiasing(true) // improve rendering a little bit on low-res screens
            // spacing between pages in dp. To define spacing color, set view background
            .spacing(0)
            .autoSpacing(false) // add dynamic spacing to fit each page on its own on the screen
//            .linkHandler(DefaultLinkHandler())
            .pageFitPolicy(FitPolicy.BOTH) // mode to fit pages in the view
            .fitEachPage(true) // fit each page to the view, else smaller pages are scaled relative to largest page.
            .pageSnap(false) // snap pages to screen boundaries
            .pageFling(true) // make a fling change only a single page like ViewPager
            .nightMode(false) // toggle night mode
            .load();
    }
}