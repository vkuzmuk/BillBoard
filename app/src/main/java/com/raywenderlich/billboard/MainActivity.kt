package com.raywenderlich.billboard

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.raywenderlich.billboard.accounthelper.AccountHelper
import com.raywenderlich.billboard.act.DescriptionActivity
import com.raywenderlich.billboard.act.EditAdcAct
import com.raywenderlich.billboard.adapters.AdsRcAdapter
import com.raywenderlich.billboard.databinding.ActivityMainBinding
import com.raywenderlich.billboard.dialoghelper.DialogConst
import com.raywenderlich.billboard.dialoghelper.DialogHelper
import com.raywenderlich.billboard.model.Ad
import com.raywenderlich.billboard.viewmodel.FirebaseViewModel
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    AdsRcAdapter.Listener {
    private lateinit var tvAccount: TextView
    private lateinit var imAccount: ImageView
    private lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val mAuth = Firebase.auth
    val adapter = AdsRcAdapter(this)
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initRecyclerView()
        initViewModel()
        firebaseViewModel.loadAllAds()
        bottomMenuOnClick()
        scrollListener()
    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId = R.id.id_home
    }

    private fun onActivityResult() {
        googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        dialogHelper.accHelper.signInFireBaseWithGoogle(account.idToken!!)
                    }
                } catch (e: ApiException) {
                    Log.d("MyLog", "Api error: ${e.message}")
                }
            }
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }

    private fun initViewModel() {
        firebaseViewModel.liveAdsData.observe(this, {
            adapter.updateAdapter(it)
            binding.mainContent.tvEmpty.visibility =
                if (it.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    private fun init() {
        setSupportActionBar(binding.mainContent.toolbar)
        onActivityResult()
        navViewSettings()
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout,
            binding.mainContent.toolbar, R.string.open, R.string.close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)
        imAccount = binding.navView.getHeaderView(0).findViewById(R.id.imAccountImage)
    }

    private fun bottomMenuOnClick() = with(binding) {
        mainContent.bNavView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.id_new_ad -> {
                    val i = Intent(this@MainActivity, EditAdcAct::class.java)
                    startActivity(i)
                }
                R.id.id_my_ads -> {
                    firebaseViewModel.loadMyAds()
                    mainContent.toolbar.title = getString(R.string.ad_my_ads)
                }
                R.id.id_favs -> {
                    firebaseViewModel.loadMyFavs()
                }
                R.id.id_home -> {
                    firebaseViewModel.loadAllAds()
                    mainContent.toolbar.title = getString(R.string.def)
                }
            }
            true
        }
    }

    private fun initRecyclerView() {
        binding.apply {
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.id_my_ads -> {
                Toast.makeText(this, "Pressed id_my_ads", Toast.LENGTH_SHORT).show()
            }
            R.id.id_cars -> {
                Toast.makeText(this, "Pressed id_cars", Toast.LENGTH_SHORT).show()
            }
            R.id.id_computers -> {
                Toast.makeText(this, "Pressed id_computers", Toast.LENGTH_SHORT).show()
            }
            R.id.id_phones -> {
                Toast.makeText(this, "Pressed id_phones", Toast.LENGTH_SHORT).show()
            }
            R.id.id_domestic -> {
                Toast.makeText(this, "Pressed id_domestic", Toast.LENGTH_SHORT).show()
            }
            R.id.id_sign_up -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
            }
            R.id.id_sign_in -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
            }
            R.id.id_sign_out -> {
                if (mAuth.currentUser?.isAnonymous == true) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                uiUpdate(null)
                mAuth.signOut()
                dialogHelper.accHelper.signOutGoogle()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun uiUpdate(user: FirebaseUser?) {
        if (user == null) {
            dialogHelper.accHelper.signInAnonymously(object : AccountHelper.Listener {

                override fun onComplete() {
                    tvAccount.setText(R.string.guest)
                    imAccount.setImageResource(R.drawable.ic_account_default)
                }
            })
        } else if (user.isAnonymous) {
            tvAccount.setText(R.string.guest)
            imAccount.setImageResource(R.drawable.ic_account_default)
        } else if (!user.isAnonymous) {
            tvAccount.text = user.email
            Picasso.get().load(user.photoUrl).into(imAccount)
        }
    }

    override fun onDeleteItem(ad: Ad) {
        firebaseViewModel.deleteItem(ad)
    }

    override fun onAdViewed(ad: Ad) {
        firebaseViewModel.adViewed(ad)
        val i = Intent(this, DescriptionActivity::class.java)
        i.putExtra("AD", ad)
        startActivity(i)
    }

    override fun onFavClicked(ad: Ad) {
        firebaseViewModel.onFavClick(ad)
    }

    private fun navViewSettings() = with(binding) {
        val menu = navView.menu

        val adsCat = menu.findItem(R.id.adsCat)
        val spanAdcCat = SpannableString(adsCat.title)
        spanAdcCat.setSpan(
            ForegroundColorSpan(
                ContextCompat
                    .getColor(this@MainActivity, R.color.color_red)),
            0, adsCat.title.length, 0
        )
        adsCat.title = spanAdcCat

        val accCat = menu.findItem(R.id.accCat)
        val spanAccCat = SpannableString(accCat.title)
        spanAccCat.setSpan(
            ForegroundColorSpan(
                ContextCompat
                    .getColor(this@MainActivity, R.color.color_red)
            ), 0, accCat.title.length, 0
        )
        accCat.title = spanAccCat
    }

    private fun scrollListener() = with(binding.mainContent) {
        rcView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recView, newState)
                if (!recView.canScrollVertically(SCROLL_DOWN) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d("MyLog", "Can't scroll down")
                }
            }
        })
    }

    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
        const val SCROLL_DOWN = 1
    }

}


