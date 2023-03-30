package master.kotlin.weather

import PressureFragment
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class WeatherFragmentAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragmentDataList = mutableListOf<Bundle>()

    override fun getItemCount(): Int {
        return fragmentDataList.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> HumidityFragment()
            1 -> PressureFragment()
            2 -> SunSetFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
        fragment.arguments = fragmentDataList[position]
        return fragment
    }

    fun setFragmentData(position: Int, data: Bundle) {
        if (position >= fragmentDataList.size) {
            fragmentDataList.addAll(List(position + 1 - fragmentDataList.size) { Bundle() })
        }
        fragmentDataList[position] = data
    }
}
