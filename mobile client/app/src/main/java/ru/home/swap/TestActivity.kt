package ru.home.swap

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import ru.home.swap.di.ViewModelFactory
import ru.home.swap.ui.profile.ProfileViewModel
import javax.inject.Inject

class TestActivity: AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stub_layout)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container_new, TestFragment())
            .commit()

/*//        TestActivityBinding.inflate(LayoutInflater.from(this), container, false)

//        val viewModel: ProfileViewModel by viewModels()
        (application as AppApplication).getComponent().inject(this)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(ProfileViewModel::class.java)

        lifecycleScope.launchWhenResumed {
            viewModel.testState.collect { it ->
                Log.d(App.TAG, "{New item from flow ${it}}")
            }
        }

        onClick.setOnClickListener {
            viewModel.add(System.currentTimeMillis())
        }*/
    }
}

