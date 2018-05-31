package com.harvey.arcfacedamo.adapter;

import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.harvey.arcfacedamo.R;
import com.harvey.arcface.moodel.FaceFindMatchModel;
import com.harvey.image.ImageLoader;

/**
 * Created by hanhui on 2017/11/22 0022 14:19
 */

public class MatchFaceAdapter extends BaseQuickAdapter<FaceFindMatchModel, BaseViewHolder> {

	public MatchFaceAdapter() {
		super(R.layout.item_match_face);
	}

	@Override
	protected void convert(BaseViewHolder helper, FaceFindMatchModel item) {
		helper.setText(R.id.tv_student_name, item.getName());
		helper.setText(R.id.tv_student_sex, item.getGender());
		helper.setText(R.id.tv_score, String.format("可信度:%.2f", item.getScore()));
		ImageLoader.displayCircleImage(mContext, item.getImagePath(), (ImageView) helper.getView(R.id.iv_student_image),
				false);
	}
}