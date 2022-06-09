package com.rafael.simulator.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.rafael.simulator.R
import com.rafael.simulator.data.MatchesApi
import com.rafael.simulator.databinding.ActivityMainBinding
import com.rafael.simulator.domain.Match
import com.rafael.simulator.ui.adapter.MatchesAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var matchesAdapter: MatchesAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var matchesApi: MatchesApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHttpClient()
        setupMatchesList()
        setupMatchesRefresh()
        setupFloatingActionButton()
    }

    private fun setupHttpClient() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://rafaelkenedy.github.io/matches-simulator-api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        matchesApi = retrofit.create(MatchesApi::class.java)

    }

    private fun setupFloatingActionButton() {
        binding.fabSimulate.setOnClickListener {

            it.animate().rotationBy(360F).setDuration(500)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        val random = Random()
                        for (i in 0 until matchesAdapter.itemCount) {
                            val match: Match = matchesAdapter.matches[i]
                            match.homeTeam.score = (random.nextInt(match.homeTeam.stars + 1))
                            match.visitorTeam.score = (random.nextInt(match.visitorTeam.stars + 1))
                            matchesAdapter.notifyItemChanged(i)
                        }
                    }
                })
        }
    }

    private fun setupMatchesRefresh() {
        binding.srMatches.setOnRefreshListener(this::findMatchesFromApi)
    }

    private fun showErrorMessage() {
        Snackbar.make(binding.fabSimulate, R.string.error_api, Snackbar.LENGTH_LONG).show()
    }

    private fun setupMatchesList() {
        binding.rvMatches.setHasFixedSize(true)
        binding.rvMatches.layoutManager = LinearLayoutManager(this)
        MatchesAdapter(Collections.emptyList()).also { matchesAdapter = it }
        binding.rvMatches.adapter = matchesAdapter
        findMatchesFromApi()
    }

    private fun findMatchesFromApi() {
        binding.srMatches.isRefreshing = true
        with(matchesApi) {
            matches.enqueue(object : Callback<List<Match>> {
                override fun onResponse(call: Call<List<Match>>, response: Response<List<Match>>) {
                    if (response.isSuccessful) {
                        val matches: List<Match>? = response.body()
                        if (matches != null) {

                            matchesAdapter = MatchesAdapter(matches)
                            binding.rvMatches.adapter = matchesAdapter
                        }
                    } else {
                        showErrorMessage()
                    }
                    binding.srMatches.isRefreshing = false

                }

                override fun onFailure(call: Call<List<Match>>, t: Throwable) {
                    showErrorMessage()
                    binding.srMatches.isRefreshing = false
                }

            })
        }
    }
}