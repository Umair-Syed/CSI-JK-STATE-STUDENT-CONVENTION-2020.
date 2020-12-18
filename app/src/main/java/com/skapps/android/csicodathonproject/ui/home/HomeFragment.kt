package com.skapps.android.csicodathonproject.ui.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.skapps.android.csicodathonproject.R
import com.skapps.android.csicodathonproject.data.Product
import com.skapps.android.csicodathonproject.data.viewmodels.HomeViewModel
import com.skapps.android.csicodathonproject.databinding.FragmentHomeBinding
import com.skapps.android.csicodathonproject.ui.login.DATA_STORE_KEY
import com.skapps.android.csicodathonproject.ui.login.PREF_KEY_IS_ADMIN
import com.skapps.android.csicodathonproject.util.KEY_COLLECTION_PRODUCTS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), ProductsListAdapter.ItemAdapterListener {
    private lateinit var binding: FragmentHomeBinding

//    private var productList = ArrayList<Product>()
    private val viewModel by viewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)

        if (activity != null) {
            (activity as MainActivity).supportActionBar?.title = "Products"
        }

        setUpFab()

//        binding.productsRecyclerView.adapter = ProductsListAdapter(requireContext(), productList,this)
        binding.productsRecyclerView.layoutManager = LinearLayoutManager(requireActivity())


        viewModel.getProducts().observe(viewLifecycleOwner) {
//            productList = it as ArrayList<Product>
            binding.productsRecyclerView.adapter = ProductsListAdapter(requireContext(), it as ArrayList<Product>,this)
//            binding.productsRecyclerView.adapter.notifyDataSetChanged()
        }
    }

    //admin only
    private fun setUpFab() {
        val dataStore: DataStore<Preferences> = requireActivity().createDataStore(
            name = DATA_STORE_KEY
        )
        val isAdminKey = preferencesKey<Boolean>(PREF_KEY_IS_ADMIN)
        val isAdminFlow: Flow<Boolean> = dataStore.data.map {
            it[isAdminKey] ?: false
        }

        lifecycleScope.launch {
            isAdminFlow.collect { isAdmin ->
                if (!isAdmin) {
                    binding.fab.visibility = View.GONE
                } else {
                    binding.fab.visibility = View.VISIBLE
                }

                binding.fab.setOnClickListener {
                    if (isAdmin) {
                        val action = HomeFragmentDirections.actionHomeFragmentToAddProductFragment()
                        findNavController().navigate(action)
                    }
                }

            }
        }
    }



    override fun onItemClicked(product: Product) {
        val action = HomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(product)
        findNavController().navigate(action)
    }
}