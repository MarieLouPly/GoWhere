package com.enioka.gowhere

import FetchRandomCityWorker
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.launch

class DestinationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_destination, container, false)

        val textViewCity: TextView = view.findViewById(R.id.destination_result)
        val textViewCountry: TextView = view.findViewById(R.id.country_result)

        // Récupérer la ville et le pays depuis SharedPreferences
        val sharedPrefs = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val randomCity = sharedPrefs?.getString("random_city", "Aucune ville") ?: "Aucune ville"
        val country = sharedPrefs?.getString("country_$randomCity", "Pays inconnu") ?: "Pays inconnu"

        // Afficher la ville et le pays dans les TextViews
        textViewCity.text = randomCity
        textViewCountry.text = country

        // Récupérer l'ImageView
        val imageView = view.findViewById<ImageView>(R.id.imageView)

        // Mettre à jour l'image
        when (randomCity) {
            "Bled" -> imageView.setImageResource(R.drawable.bled)
            "Braga" -> imageView.setImageResource(R.drawable.braga)
            "Elafonissi" -> imageView.setImageResource(R.drawable.elafonissi)
            "Girona" -> imageView.setImageResource(R.drawable.girona)
            "Hallstatt" -> imageView.setImageResource(R.drawable.hallstatt)
            "Tromsø" -> imageView.setImageResource(R.drawable.tromso)
            "Český Krumlov" -> imageView.setImageResource(R.drawable.cesky)
            "Lagos" -> imageView.setImageResource(R.drawable.lagos)
            else -> imageView.setImageResource(R.drawable.logo_sans_fond)
        }

        // Gérer l'événement du bouton "Afficher le résultat"
        view.findViewById<Button>(R.id.view_destination_btn).setOnClickListener {
            findNavController().navigate(R.id.action_destinationFragment_to_informationsFragment)
        }

        // Gérer l'événement du bouton "Relancer la roue"
        view.findViewById<Button>(R.id.retry_destination_btn).setOnClickListener {
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
                            findNavController().navigate(R.id.action_destinationFragment_to_transitionFragment)
                        }
                    }
            }
        }
        return view
    }
}