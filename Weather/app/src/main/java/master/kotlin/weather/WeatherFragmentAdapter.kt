package master.kotlin.weather

import ForecastFragment
import PressureFragment
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class WeatherFragmentAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragments: MutableMap<Int, Fragment> = HashMap()
    private val fragmentDataList = mutableListOf<Bundle>()

    override fun getItemCount(): Int {
        return 6
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> HumidityFragment()
            1 -> PressureFragment()
            2 -> WindFragment()
            3 -> TemperatureFragment()
            4 -> SunInfoFragment()
            5 -> ForecastFragment() // Ajout du cas pour ForecastFragment
            else -> throw IllegalStateException("Invalid fragment position")
        }

        fragments[position] = fragment

        // Ajoutez ces lignes pour fournir les données aux fragments
        if (position < fragmentDataList.size) {
            fragment.arguments = fragmentDataList[position]
        }

        return fragment
    }

    fun getFragment(position: Int): Fragment? {
        return fragments[position]
    }

    fun setFragmentData(position: Int, data: Bundle) {
        if (position >= fragmentDataList.size) {
            fragmentDataList.addAll(List(position + 1 - fragmentDataList.size) { Bundle() })
        }
        fragmentDataList[position] = data

        // Ajoutez ces lignes pour mettre à jour les arguments des fragments existants
        val existingFragment = fragments[position]
        existingFragment?.arguments = data
    }
}
