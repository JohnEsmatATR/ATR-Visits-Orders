package com.akhnaton.foodvisits.ui.home.main

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.akhnaton.foodvisits.R

class SliderAdapter constructor(private var context: Context) : PagerAdapter() {

    private val sliderImageId = arrayListOf(
        R.raw.slide_01,
        R.raw.slide_02,
        R.raw.slide_03,
        R.raw.slide_04,
        R.raw.slide_05
    )

    override fun getCount(): Int {
        return sliderImageId.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as ImageView
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageResource(sliderImageId[position])
        (container as ViewPager).addView(imageView, 0)
        return imageView
    }

}