// File: SignUpActivity.kt
package com.thesis.dishdetective_xml

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.thesis.dishdetective_xml.databinding.ActivitySignUpBinding
import com.thesis.dishdetective_xml.ui.profile.ProfileInputFragment

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                } else if (pass == confirmPass) {

                    val fragment = ProfileInputFragment().apply {
                        arguments = Bundle().apply {
                            putString("email", email)
                            putString("pass", pass)
                        }
                    }
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container, fragment)
                        addToBackStack(null)
                    }
                    binding.emailLayout.visibility = View.GONE
                    binding.passwordLayout.visibility = View.GONE
                    binding.confirmPasswordLayout.visibility = View.GONE
                    binding.button.visibility = View.GONE
                    binding.textView.visibility = View.GONE

                } else {
                    Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}