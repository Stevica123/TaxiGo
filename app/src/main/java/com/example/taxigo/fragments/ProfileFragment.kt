package com.example.taxigo.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.taxigo.R
import com.example.taxigo.WelcomeActivity
import com.example.taxigo.models.*
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var profileIdText: TextView
    private lateinit var profileOrdersCountText: TextView
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)


        val profileImage = view.findViewById<ImageView>(R.id.profileImageView)
        val profileName = view.findViewById<TextView>(R.id.profileNameText)
        val profileEmail = view.findViewById<TextView>(R.id.profileEmailText)
        profileIdText = view.findViewById(R.id.profileIdText)
        profileOrdersCountText = view.findViewById(R.id.profileOrderCount)
        val logoutButton = view.findViewById<MaterialButton>(R.id.logoutButton)


        profileIdText.visibility = View.GONE
        profileOrdersCountText.visibility = View.GONE


        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userModel = UserModel.fromFirebaseUser(firebaseUser)

        if (userModel != null) {

            saveUserToFirestore(userModel)
            saveUserToRoom(userModel)

            when {
                userModel.isAnonymous -> {
                    profileImage.setImageResource(R.drawable.ic_anonymous)
                    profileName.text = getString(R.string.anonymous_user)
                    profileEmail.text = ""

                    profileIdText.visibility = View.VISIBLE
                    profileIdText.text = getString(R.string.profile_id_placeholder, userModel.uid.take(6))

                    profileOrdersCountText.visibility = View.GONE
                }

                userModel.isGoogle -> {
                    profileName.text = userModel.name ?: getString(R.string.google_user)
                    profileEmail.text = getString(R.string.email_label, userModel.email ?: "")
                    profileIdText.text = getString(R.string.profile_id_placeholder, userModel.uid.take(6))

                    profileIdText.visibility = View.VISIBLE
                    profileOrdersCountText.visibility = View.VISIBLE

                    Glide.with(this)
                        .load(userModel.photoUrl)
                        .circleCrop()
                        .into(profileImage)

                    loadOrdersCount(userModel.uid)
                }

                else -> {
                    val email = userModel.email ?: getString(R.string.unknown_email)
                    val initial = email.firstOrNull()?.uppercaseChar() ?: '?'
                    val drawable = UserModel.generateInitialCircle(requireContext(), initial)

                    profileImage.setImageDrawable(drawable)
                    profileName.text = ""
                    profileEmail.text = getString(R.string.email_label, email)
                    profileIdText.text = getString(R.string.profile_id_placeholder, userModel.uid.take(6))

                    profileIdText.visibility = View.VISIBLE
                    profileOrdersCountText.visibility = View.VISIBLE

                    loadOrdersCount(userModel.uid)
                }
            }
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    private fun saveUserToFirestore(userModel: UserModel) {
        val userDoc = firestore.collection("users").document(userModel.uid)

        val userMap = mapOf(
            "uid" to userModel.uid,
            "name" to userModel.name,
            "email" to userModel.email,
            "photoUrl" to userModel.photoUrl,
            "isAnonymous" to userModel.isAnonymous,
            "isGoogle" to userModel.isGoogle
        )

        userDoc.set(userMap)
            .addOnSuccessListener {
                println("✅ Корисникот е зачуван во Firestore!")
            }
            .addOnFailureListener { e ->
                println("❌ Firestore грешка: ${e.message}")
            }
    }


    private fun saveUserToRoom(userModel: UserModel) {
        val userEntity = UserEntity(
            uid = userModel.uid,
            name = userModel.name,
            email = userModel.email,
            photoUrl = userModel.photoUrl,
            isAnonymous = userModel.isAnonymous,
            isGoogle = userModel.isGoogle
        )

        val db = AppDatabase.getDatabase(requireContext())
        val userDao = db.userDao()

        viewLifecycleOwner.lifecycleScope.launch {
            userDao.insertUser(userEntity)
            println("✅ Корисникот е зачуван локално во Room!")
        }
    }


    private fun loadOrdersCount(userId: String?) {
        if (userId == null) {
            profileOrdersCountText.text = getString(R.string.order_count, 0)
            return
        }

        firestore.collection("orders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val count = result.size()
                profileOrdersCountText.text = getString(R.string.order_count, count)
            }
            .addOnFailureListener {
                profileOrdersCountText.text = getString(R.string.order_count, 0)
            }
    }
}
