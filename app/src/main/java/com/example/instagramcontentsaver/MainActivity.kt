package com.example.instagramcontentsaver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.instagramcontentsaver.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var pagerAdapter: DynamicPagerAdapter
    private var mediator: TabLayoutMediator? = null
    val tabNames= listOf("Video","IgTv","Reel")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        pagerAdapter = DynamicPagerAdapter(fragmentManager = supportFragmentManager, lifecycle = lifecycle)


        pagerAdapter.registerFragmentTransactionCallback(transactionCallback)
        binding.viewPager2.isUserInputEnabled = true

        binding.viewPager2.adapter = pagerAdapter
        binding.viewPager2.registerOnPageChangeCallback(pageChangeCallback)

        if (mediator != null)
            mediator!!.detach()
        pagerAdapter.removeAllFragments()
        pagerAdapter.addFragments( TabAllFragment() , tabNames)
        binding.viewPager2.offscreenPageLimit =
            pagerAdapter.itemCount


        val tabs = mutableListOf<String>().apply {
            add("Images")
            addAll(tabNames)
        }

        val strategy =
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                tab.text = tabs[position]
            }

        mediator = TabLayoutMediator(
            binding.tabLayout,
            binding.viewPager2,
            strategy
        )

        mediator?.attach()


    }


    private val transactionCallback = object : FragmentStateAdapter.FragmentTransactionCallback() {
        override fun onFragmentMaxLifecyclePreUpdated(
            fragment: Fragment,
            maxLifecycleState: Lifecycle.State
        ): OnPostEventListener {
            if (maxLifecycleState == Lifecycle.State.RESUMED) {
                // This fragment is becoming the active Fragment - set it to
                // the primary navigation fragment in the OnPostEventListener

                if (fragment is DynamicTabFragment)
                // dynamic tab
                    Log.d("testing", "dynamic tab")
                if (fragment is TabAllFragment)
                // all tab
                //fragment.title=currentTopic
                    Log.d("testing", "all tab")

            }


            return super.onFragmentMaxLifecyclePreUpdated(fragment, maxLifecycleState)
        }
    }


    override fun onStop() {
        binding.viewPager2.unregisterOnPageChangeCallback(pageChangeCallback)
        pagerAdapter.unregisterFragmentTransactionCallback(transactionCallback)
        super.onStop()
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            //tinyDB.putInt("libraryListPosition", position)
        }
    }

}