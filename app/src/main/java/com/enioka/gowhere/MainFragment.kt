package com.enioka.gowhere

import FetchRandomCityWorker
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.launch

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
        ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        view.findViewById<Button>(R.id.destination_btn).setOnClickListener {
            // In your main activity
            val fetchCityWorkRequest = OneTimeWorkRequestBuilder<FetchRandomCityWorker>().build()
            WorkManager.getInstance(requireContext()).enqueue(fetchCityWorkRequest)

            // Observe WorkInfo and navigate
            lifecycleScope.launch {
                WorkManager.getInstance(requireContext())
                    .getWorkInfoByIdLiveData(fetchCityWorkRequest.id)
                    .observe(viewLifecycleOwner) { workInfo ->
                        if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                            val sharedPrefs = requireContext().getSharedPreferences(
                                "app_prefs",
                                Context.MODE_PRIVATE
                            )
                            val randomCityName = sharedPrefs.getString("random_city", "")
                            findNavController().navigate(R.id.action_mainFragment_to_transitionFragment)
                        }
                    }
            }
        }
        return view
    }
}