package dk.itu.moapd.copenhagenbuzz.maass.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.copenhagenbuzz.maass.R
import dk.itu.moapd.copenhagenbuzz.maass.databinding.ActivityLoginBinding

/**
 * Activity that handles user authentication, including email/password login,
 * registration, Google Sign-In, and guest access.
 */
class LoginActivity : AppCompatActivity() {

    // View binding for the login activity layout
    private lateinit var binding: ActivityLoginBinding
    // Firebase authentication instance
    private lateinit var auth: FirebaseAuth
    // Google Sign-In client
    private lateinit var googleSignInClient: GoogleSignInClient

    private companion object {
        private const val TAG = "LoginActivity"
        private const val RC_GOOGLE_SIGN_IN = 9001
    }

    /**
     * Called when the activity is starting. Initializes authentication,
     * view binding, and sets up click listeners for login, Google sign-in, and guest access.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Set up view binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set up click listeners
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (validateInput(email, password)) {
                loginOrRegisterWithEmail(email, password)
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        binding.btnGuest.setOnClickListener {
            startMainActivity(false) // Guest state
        }
    }

    /**
     * Validates the email and password input fields.
     *
     * @param email The user's email address.
     * @param password The user's password.
     * @return True if both fields are non-empty, false otherwise.
     */
    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.error_email_empty)
            return false
        }
        if (password.isEmpty()) {
            binding.etPassword.error = getString(R.string.error_password_empty)
            return false
        }
        return true
    }

    /**
     * Attempts to log in or register the user with the provided email and password.
     * Checks if the user exists and either logs in or registers accordingly.
     *
     * @param email The user's email address.
     * @param password The user's password.
     */
    private fun loginOrRegisterWithEmail(email: String, password: String) {
        // Check if the user exists first
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods ?: emptyList<String>()
                    Log.d(TAG, "Sign-in methods for $email: $signInMethods")

                    if (signInMethods.isEmpty()) {
                        // User doesn't exist, register
                        Log.d(TAG, "Email not registered, proceeding to registration")
                        registerUser(email, password)
                    } else {
                        // User exists, attempt login
                        Log.d(TAG, "Email already registered, proceeding to login")
                        loginWithEmail(email, password)
                    }
                } else {
                    // Error checking email
                    Log.e(TAG, "Error checking email", task.exception)
                    Toast.makeText(
                        this,
                        "Error checking email: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    /**
     * Attempts to log in the user with email and password.
     * Handles success and error cases, including wrong password.
     *
     * @param email The user's email address.
     * @param password The user's password.
     */
    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login succeeded
                    Log.d(TAG, "signInWithEmail:success")
                    startMainActivity(true)
                } else {
                    // Login failed
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    val e = task.exception
                    when (e) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            // Wrong password
                            Toast.makeText(
                                this,
                                "Incorrect password for $email",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            // Other errors (network, etc.)
                            Toast.makeText(
                                this,
                                e?.message ?: "Login failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
    }

    /**
     * Registers a new user with the provided email and password.
     * If the email is already in use, attempts to log in instead.
     *
     * @param email The user's email address.
     * @param password The user's password.
     */
    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    Toast.makeText(
                        this,
                        getString(R.string.account_created),
                        Toast.LENGTH_SHORT
                    ).show()
                    startMainActivity(true)
                } else {
                    // Registration failed
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    val e = task.exception
                    // Check if the error is due to the email already being in use
                    if (e?.message?.contains("email address is already in use") == true) {
                        // This should not happen due to our previous check, but just in case
                        Log.w(TAG, "Email already in use, attempting login instead")
                        loginWithEmail(email, password)
                    } else {
                        val message = e?.message ?: "Registration failed"
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    /**
     * Initiates the Google Sign-In flow.
     */
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    /**
     * Handles the result from activities started for result, such as Google Sign-In.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode The integer result code returned by the child activity.
     * @param data An Intent, which can return result data to the caller.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    /**
     * Authenticates the user with Firebase using a Google ID token.
     *
     * @param idToken The Google ID token.
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    startMainActivity(true)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    /**
     * Starts the main activity and passes the login state.
     *
     * @param isLoggedIn True if the user is logged in, false if guest.
     */
    private fun startMainActivity(isLoggedIn: Boolean) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("isLoggedIn", isLoggedIn)
        startActivity(intent)
        finish()
    }
}