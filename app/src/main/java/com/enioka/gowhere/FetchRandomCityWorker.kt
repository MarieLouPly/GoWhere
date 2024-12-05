import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class FetchRandomCityWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    // Map associant chaque ville à son pays, une liste d'activités et ses coordonnées géographiques
    private val cityDetails = mapOf(
        "Český Krumlov" to Triple(
            "République Tchèque",
            listOf(
                "Visiter le château médiéval",
                "Descendre la rivière Vltava en canoë",
                "Festival de musique de Český Krumlov"
            ),
            Pair(48.8126, 14.3189) // Coordonnées géographiques (latitude, longitude)
        ),
        "Braga" to Triple(
            "Portugal",
            listOf(
                "Visiter le sanctuaire de Bom Jesus do Monte",
                "Participer à une fête locale",
                "Déguster le Bacalhau à Braga"
            ),
            Pair(41.5475, -8.4297)
        ),
        "Girona" to Triple(
            "Espagne",
            listOf(
                "Marcher sur les remparts médiéaux",
                "Visiter les lieux de tournage de Game of Thrones",
                "Essayer les Xuixos"
            ),
            Pair(41.9794, 2.8215)
        ),
        "Elafonissi" to Triple(
            "Grèce",
            listOf(
                "Plage de sable rose avec eaux turquoise peu profondes",
                "Faire une promenade jusqu'à l’île d'Elafonissi",
                "Snorkeling et plongée sous-marine"
            ),
            Pair(35.2841, 23.5724)
        ),
        "Lagos" to Triple(
            "Portugal",
            listOf(
                "Falaises spectaculaires de Ponta da Piedade",
                "Explorer des grottes et arches marines en kayak",
                "Excursion pour observer les dauphins"
            ),
            Pair(37.1022, -8.6749)
        ),
        "Tromsø" to Triple(
            "Norvège",
            listOf(
                "Observer les aurores boréales",
                "Explorer le musée polaire",
                "Excursion en traîneau à chiens"
            ),
            Pair(69.6496, 18.9560)
        ),
        "Hallstatt" to Triple(
            "Autriche",
            listOf(
                "Flâner dans les rues de ce village alpin",
                "Explorer les anciennes mines de sel",
                "Sentiers de randonnée vers Krippenstein"
            ),
            Pair(47.5615, 13.6492)
        ),
        "Bled" to Triple(
            "Slovénie",
            listOf(
                "Lac entouré de montagnes avec une île au centre",
                "Monter au château de Bled",
                "Excursion en pletna vers l’île"
            ),
            Pair(46.3625, 14.1131)
        )
    )

    override fun doWork(): Result {
        return try {
            // Sélectionner une ville aléatoire
            val randomCity = cityDetails.keys.random()

            // Récupérer le pays, les activités et les coordonnées associées à la ville sélectionnée
            val (country, activities, coordinates) = cityDetails[randomCity] ?: Triple("", emptyList(), Pair(0.0, 0.0))

            // Stocker la ville, le pays, les activités et les coordonnées dans SharedPreferences
            val sharedPrefs = applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit()
                .putString("random_city", randomCity)
                .putString("country_$randomCity", country) // Enregistrer le pays
                .putStringSet("activities_$randomCity", activities.toSet()) // Enregistrer la liste d'activités
                .putString("coordinates_$randomCity", "${coordinates.first},${coordinates.second}") // Enregistrer les coordonnées
                .apply()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}