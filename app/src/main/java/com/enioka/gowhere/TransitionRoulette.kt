package com.enioka.gowhere

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController


class TransitionRoulette : Fragment(R.layout.transition_roulette) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val videoView: VideoView = view.findViewById(R.id.transitionVideo)
        val videoUri = Uri.parse("android.resource://" + requireContext().packageName + "/raw/roulette")    // Définir la vidéo à lire
        videoView.setVideoURI(videoUri)

        videoView.setOnPreparedListener { mp ->
            mp.isLooping = false
            mp.start() // Démarrer directement la vidéo
        }

        // Lorsque la vidéo est terminée, vous pouvez effectuer la transition vers un autre fragment
        videoView.setOnCompletionListener {
            findNavController().navigate(R.id.action_transitionFragment_to_destinationFragment)
        }
    }
}