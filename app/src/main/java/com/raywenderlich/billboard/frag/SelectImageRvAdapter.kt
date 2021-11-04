package com.raywenderlich.billboard.frag

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.billboard.R
import com.raywenderlich.billboard.act.EditAdcAct
import com.raywenderlich.billboard.databinding.SelectImageFragItemBinding
import com.raywenderlich.billboard.utils.AdapterCallback
import com.raywenderlich.billboard.utils.ImageManager
import com.raywenderlich.billboard.utils.ImagePicker
import com.raywenderlich.billboard.utils.ItemTouchMoveCallback


class SelectImageRvAdapter(val adapterCallback: AdapterCallback): RecyclerView.Adapter<SelectImageRvAdapter.ImageHolder>(),
    ItemTouchMoveCallback.ItemTouchAdapter {

   val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val binding = SelectImageFragItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return ImageHolder(binding, parent.context, this)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onMove(startPos: Int, targetPos: Int) {
        val targetItem = mainArray[targetPos]
        mainArray[targetPos] = mainArray[startPos]
        mainArray[startPos] = targetItem
        notifyItemMoved(startPos, targetPos)

    }

    override fun onClear() {
        notifyDataSetChanged()
    }

    class ImageHolder(private val binding: SelectImageFragItemBinding,
                      val context: Context,
                      val adapter: SelectImageRvAdapter) : RecyclerView.ViewHolder(binding.root) {


        fun setData(bitmap: Bitmap) {
            binding.imEditImage.setOnClickListener {
                ImagePicker.launcher(context as EditAdcAct, 1)
                context.editImagePos = adapterPosition
            }

            binding.imDelete.setOnClickListener {

                adapter.mainArray.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
                for (n in 0 until adapter.mainArray.size) adapter.notifyItemChanged(n)
                adapter.adapterCallback.onItemDelete()
            }

            binding.tvTitle.text = context.resources.getStringArray(R.array.title_array)[adapterPosition]
            ImageManager.chooseScaleType(binding.imageView, bitmap)
            binding.imageView.setImageBitmap(bitmap)
        }
    }

    fun updateAdapter(newList: List<Bitmap>, needClear: Boolean){
        if(needClear) mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }



}