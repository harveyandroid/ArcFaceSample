package com.harvey.arcfacedemo.adapter

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.harvey.arcface.model.FaceFindMatchModel
import com.harvey.arcfacedemo.R
import com.harvey.image.ImageLoader

/**
 * Created by hanhui on 2018/6/1 0001 13:50
 */


class MatchFaceAdapter : BaseQuickAdapter<FaceFindMatchModel, BaseViewHolder>(R.layout.item_match_face) {

    override fun convert(helper: BaseViewHolder, item: FaceFindMatchModel) {
        helper.setText(R.id.tv_student_name, item.name)
        helper.setText(R.id.tv_student_sex, item.gender)
        helper.setText(R.id.tv_score, String.format("可信度:%.2f", item.score))
        ImageLoader.displayCircleImage(mContext, item.imagePath, helper.getView<View>(R.id.iv_student_image) as ImageView,
                false)
    }
}