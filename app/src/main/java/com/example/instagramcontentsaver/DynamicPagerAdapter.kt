package com.example.instagramcontentsaver

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class DynamicPagerAdapter(private val list: MutableList<Fragment> = mutableListOf(),
                          fragmentManager: FragmentManager,
                          lifecycle: Lifecycle
): FragmentStateAdapter(fragmentManager, lifecycle) {

    private val pageIds= mutableListOf<Long>()
    val TAG = "DynamicPagerAdapter"

    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return list[position]
    }

    fun addFragments(f: TabAllFragment, ls:List<String>){
        pageIds.clear()
        list.add(f)
        ls.forEach {
                category ->
            list.add(DynamicTabFragment.getInstance(category))
        }

        pageIds.addAll(list.map { it.hashCode().toLong() })
        notifyDataSetChanged()
    }

    fun removeAllFragments(){
        Log.d("testing","removeAllFragments")
        list.clear()
        pageIds.clear()
        notifyDataSetChanged()
    }


    override fun getItemId(position: Int): Long {
        return pageIds[position]
    }

    override fun containsItem(itemId: Long): Boolean {
        return pageIds.contains(itemId)
    }

    fun refreshDataOfOtherTabs(){
        list.forEach {
            if (it is DynamicTabFragment)
            //it.feedAdapter.notifyDataSetChanged()
                Log.d("testing","refresh data of other tabs")
        }
    }


}