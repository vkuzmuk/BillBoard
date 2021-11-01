package com.raywenderlich.billboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.raywenderlich.billboard.act.EditAdcAct
import com.raywenderlich.billboard.adapters.AdsRcAdapter
import com.raywenderlich.billboard.databinding.ActivityMainBinding
import com.raywenderlich.billboard.dialoghelper.DialogConst
import com.raywenderlich.billboard.dialoghelper.DialogHelper
import com.raywenderlich.billboard.dialoghelper.GoogleAccConst
import com.raywenderlich.billboard.model.Ad
import com.raywenderlich.billboard.viewmodel.FirebaseViewModel

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AdsRcAdapter.Listener {
    private lateinit var tvAccount: TextView
    private lateinit var rootElement: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val mAuth = Firebase.auth
    val adapter = AdsRcAdapter(this)
    private val firebaseViewModel: FirebaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityMainBinding.inflate(layoutInflater)
        setContentView(rootElement.root)
        init()
        initRecyclerView()
        initViewModel()
        firebaseViewModel.loadAllAds()
        bottomMenuOnClick()
    }

    override fun onResume() {
        super.onResume()
        rootElement.mainContent.bNavView.selectedItemId = R.id.id_home
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == GoogleAccConst.GOOGLE_SIGN_IN_REQUEST_CODE) {
            //Log.d("MyLog", "Sign in result")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if(account != null) {
                    dialogHelper.accHelper.signInFireBaseWithGoogle(account.idToken!!)
                }
            } catch(e: ApiException) {
                Log.d("MyLog", "Api error: ${e.message}")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }

    private fun initViewModel() {
        firebaseViewModel.liveAdsData.observe(this, {
            adapter.updateAdapter(it)

        })
    }

    private fun init(){
        setSupportActionBar(rootElement.mainContent.toolbar)
        val toggle = ActionBarDrawerToggle(this, rootElement.drawerLayout,
            rootElement.mainContent.toolbar, R.string.open, R.string.close )
        rootElement.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        rootElement.navView.setNavigationItemSelectedListener(this)
        tvAccount = rootElement.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)
    }

    private fun bottomMenuOnClick() = with(rootElement) {
        mainContent.bNavView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.id_new_ad -> {
                    val i = Intent(this@MainActivity, EditAdcAct ::class.java)
                    startActivity(i)
                }
                R.id.id_my_ads -> {
                    firebaseViewModel.loadMyAds()
                    mainContent.toolbar.title = getString(R.string.ad_my_ads)
                }
                R.id.id_favs -> {
                    Toast.makeText(this@MainActivity, "My Favs", Toast.LENGTH_LONG).show()
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
        rootElement.apply {
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
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
                uiUpdate(null)
                mAuth.signOut()
                dialogHelper.accHelper.signOutGoogle()
            }
        }
        rootElement.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun uiUpdate(user: FirebaseUser?) {
        tvAccount.text = if(user == null) {
            resources.getString(R.string.not_reg)
        } else {
            user.email
        }
    }

    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
    }

    override fun onDeleteItem(ad: Ad) {
        firebaseViewModel.deleteItem(ad)
    }

    override fun onAdViewed(ad: Ad) {
        firebaseViewModel.adViewed(ad)
    }
}