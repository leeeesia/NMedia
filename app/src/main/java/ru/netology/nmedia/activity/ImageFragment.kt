package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentPhotoBinding


class ImageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentPhotoBinding.inflate(
            inflater,
            container,
            false
        )
        val url = arguments?.textArg
            ?: throw NullPointerException("Image URL is undefined")

        Glide.with(binding.root)
            .load("http://10.0.2.2:9999/media/$url")
            .timeout(10_000)
            .into(binding.preview)


        binding.back.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

}