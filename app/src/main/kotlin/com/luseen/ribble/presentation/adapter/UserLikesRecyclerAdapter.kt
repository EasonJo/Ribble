package com.luseen.ribble.presentation.adapter

import android.view.View
import android.view.ViewGroup
import com.luseen.ribble.R
import com.luseen.ribble.domain.entity.Like
import com.luseen.ribble.presentation.adapter.holder.UserLikesViewHolder
import com.luseen.ribble.presentation.adapter.listener.ShotClickListener
import com.luseen.ribble.utils.inflate

/**
 * Created by Chatikyan on 15.08.2017.
 */
class UserLikesRecyclerAdapter constructor(likeList: List<Like>,
                                           private val shotClickListener: ShotClickListener)
    : AbstractAdapter<UserLikesViewHolder, Like>(likeList) {

    override fun onBind(holder: UserLikesViewHolder, item: Like) {
        holder.bind(item)
    }

    override fun createViewHolder(parent: ViewGroup): UserLikesViewHolder {
        val view = parent inflate R.layout.liked_shot_item
        return UserLikesViewHolder(view)
    }

    override fun onItemClick(itemView: View, position: Int) {
        val shot = itemList[position].shot
        shot?.let {
            shotClickListener.onShotClicked(shot)
        }
    }
}