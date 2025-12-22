package com.captain.voyage.ui.home

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.captain.voyage.VoyageApplication

class HomeFragment : Fragment() {

    // MainActivity와 뷰모델 인스턴스를 공유하여 알림 신호를 주고받습니다.
    private val viewModel: HomeViewModel by activityViewModels {
        HomeViewModelFactory((requireActivity().application as VoyageApplication).repository)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}