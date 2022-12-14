package com.android.desafiokotlin.ui.activity.ui.Filmes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.desafiokotlin.R
import com.android.desafiokotlin.database.FavoritoDatabase
import com.android.desafiokotlin.databinding.FragmentFilmesBinding
import com.android.desafiokotlin.model.Filme
import com.android.desafiokotlin.repository.Repository
import com.android.desafiokotlin.ui.activity.DetalhesFilmeActivity
import com.android.desafiokotlin.ui.recyclerview.adapter.ListaFilmesAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class FilmesFragment : Fragment() {

    private var _binding: FragmentFilmesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.

    // Variáveis responsáveis pela animação
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim) }
    private var clicked = false

    private val binding get() = _binding!!
    private val arrayList: ArrayList<Filme> = arrayListOf()
    private var filmesFavoritos : ArrayList<Filme> = arrayListOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var scrollListener: RecyclerView.OnScrollListener
    val FilmesViewModel by lazy {
        ViewModelProvider(this).get(FilmesViewModel::class.java)
    }

    private val repository by lazy {
        Repository(
            filmeDAO = FavoritoDatabase.getInstance(requireContext()).favoritoDAO()
        )
    }


    private val adapter by lazy{
        ListaFilmesAdapter(requireContext(),"filmes",filmesFavoritos, arrayList)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilmesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.onCreate(savedInstanceState)
        recyclerView = binding.activityTopFilmesRecyclerview
        lifecycleScope.launchWhenStarted {
            configuraRecyclerView()
            configuraSeleçãoDeFavoritos()
        }
        lifecycleScope.launch {
            adapter.colocaCoracao(repository.buscaTodos() as ArrayList<Filme>)
        }
        setObserver()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FilmesViewModel.getBuscaFilmesApi()
    }

    private fun setObserver() {
        FilmesViewModel.filmeResposta.observe(viewLifecycleOwner){
            arrayList.addAll(it.results!!)
            this.adapter.atualiza(arrayList)
            removeScrollListenerAdapter()
            addScrollListenerAdapter()
        }
    }

    private fun configuraSeleçãoDeFavoritos() {
        var selecionado = 0
        this.adapter.onLongItemClickListener = { isSelected: Boolean ->
            selecionado++
            val fab: FloatingActionButton = binding.navHostActivityMainFabFavorito
            if (isSelected) {
                if (selecionado == 1) modoSelecao()
                fab.setOnClickListener {
                    filmesFavoritos = this.adapter.pegaFavoritos()
                    lifecycleScope.launch {
                        repository.salva(filmesFavoritos)
                    }
                    selecionado = 0
                    modoSelecao()
                }
            } else if (!isSelected) {
                selecionado = 0
                modoSelecao()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        removeScrollListenerAdapter()
        addScrollListenerAdapter()
        this.adapter.atualiza(arrayList)
    }

    override fun onPause() {
        super.onPause()
        removeScrollListenerAdapter()
    }

    private fun addScrollListenerAdapter() {
        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
                    val visibleItemCount = layoutManager.childCount
                    val totalItemVisible = visibleItemCount + pastVisibleItems
                    val totalItemCount = layoutManager.itemCount
                    if (totalItemVisible >= totalItemCount) {
                        lifecycleScope.launch {
                            removeScrollListenerAdapter()
                            FilmesViewModel.getBuscaFilmesApi()
                        }
                    }
                }
            }
        }
        recyclerView.addOnScrollListener(scrollListener)
    }

    private fun removeScrollListenerAdapter() {
        if (::scrollListener.isInitialized) {
            recyclerView.removeOnScrollListener(scrollListener)
        }
    }

    private fun configuraRecyclerView() {
        recyclerView.adapter = this.adapter
        this.adapter.itemClickListener = {
            val intent = Intent(context, DetalhesFilmeActivity::class.java)
            intent.putExtra("filme", it)
            startActivity(intent)
        }
    }

    //  Funções para criação da animação
    private fun modoSelecao() {
        setVisibility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    private fun setAnimation(clicked : Boolean) {
        if (!clicked){
            binding.navHostActivityMainFabFavorito.startAnimation(fromBottom)
            binding.navHostActivityMainFabFavorito.isClickable = true
        } else{
            binding.navHostActivityMainFabFavorito.startAnimation(toBottom)
            binding.navHostActivityMainFabFavorito.isClickable = false
        }
    }

    private fun setVisibility(clicked : Boolean) {
        if(!clicked){
            binding.navHostActivityMainFabFavorito.visibility = View.VISIBLE
        } else {
            binding.navHostActivityMainFabFavorito.visibility = View.GONE
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}