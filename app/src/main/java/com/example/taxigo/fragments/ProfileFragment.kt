package com.example.taxigo.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.taxigo.R
import com.example.taxigo.WelcomeActivity
import com.example.taxigo.models.UserModel
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var profileIdText: TextView
    private lateinit var profileOrdersCountText: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userModel = UserModel.fromFirebaseUser(firebaseUser)

        val profileImage = view.findViewById<ImageView>(R.id.profileImageView)
        val profileName = view.findViewById<TextView>(R.id.profileNameText)
        val profileEmail = view.findViewById<TextView>(R.id.profileEmailText)
        profileIdText = view.findViewById(R.id.profileIdText)
        profileOrdersCountText = view.findViewById(R.id.profileOrderCount)
        val logoutButton = view.findViewById<MaterialButton>(R.id.logoutButton)


        profileIdText.visibility = View.GONE
        profileOrdersCountText.visibility = View.GONE

        if (userModel != null) {
            when {
                userModel.isAnonymous -> {
                    profileImage.setImageResource(R.drawable.ic_anonymous)
                    profileName.text = "Анонимен корисник"
                    profileEmail.text = ""

                    profileIdText.visibility = View.VISIBLE
                    profileIdText.text = "ID: ${firebaseUser?.uid?.take(6) ?: "N/A"}"


                    profileOrdersCountText.visibility = View.GONE
                }

                userModel.isGoogle -> {
                    profileOrdersCountText.visibility = View.VISIBLE
                    profileIdText.visibility = View.VISIBLE

                    profileName.text = userModel.name ?: "Google корисник"
                    profileEmail.text = "Email: ${userModel.email ?: ""}"
                    profileIdText.text = "ID: ${firebaseUser?.uid?.take(6) ?: "N/A"}"

                    Glide.with(this)
                        .load(userModel.photoUrl)
                        .circleCrop()
                        .into(profileImage)

                    loadOrdersCount(firebaseUser?.uid)
                }

                else -> {
                    profileOrdersCountText.visibility = View.VISIBLE
                    profileIdText.visibility = View.VISIBLE

                    val email = userModel.email ?: "Непознат email"
                    val initial = email.firstOrNull()?.uppercaseChar() ?: '?'
                    val drawable = UserModel.generateInitialCircle(requireContext(), initial)

                    profileImage.setImageDrawable(drawable)
                    profileName.text = ""
                    profileEmail.text = "Email: $email"
                    profileIdText.text = "ID: ${firebaseUser?.uid?.take(6) ?: "N/A"}"

                    loadOrdersCount(firebaseUser?.uid)
                }
            }
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            Toast.makeText(requireContext(), "Успешна одјава", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    private fun loadOrdersCount(userId: String?) {
        if (userId == null) {
            profileOrdersCountText.text = "Направени нарачки: 0"
            return
        }

        db.collection("orders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val count = result.size()
                profileOrdersCountText.text = "Направени нарачки: $count"
            }
            .addOnFailureListener {
                profileOrdersCountText.text = "Направени нарачки: 0"
            }
    }
}
