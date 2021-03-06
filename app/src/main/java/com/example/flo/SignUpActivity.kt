package com.example.flo

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flo.databinding.ActivitySignupBinding

class SignUpActivity : AppCompatActivity(), SignUpView {
    lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupBtnSignupTv.setOnClickListener{
            signUp()
            finish()
        }
    }

    private fun getUser(): User {
        val email : String = binding.signupIdEt.text.toString() + "@" + binding.signupEmailEt.text.toString()
        val pwd : String = binding.signupPasswordEt.text.toString()
        val name : String = binding.signupNameEt.text.toString()

        return User(email, pwd, name)
    }

    /*private fun signUp(){
        if(binding.signupIdEt.text.toString().isEmpty() || binding.signupEmailEt.text.toString().isEmpty()){
            Toast.makeText(this,"이메일 형식이 잘못되었습니다.",Toast.LENGTH_SHORT).show()
            return
        }

        if(binding.signupPasswordEt.text.toString() != binding.signupPasswordCheckEt.text.toString()){
            Toast.makeText(this,"비밀번호가 일치하지 않습니다.",Toast.LENGTH_SHORT).show()
            return
        }

        val userDB = SongDatabase.getInstance(this)!!
        userDB.userDao().insert(getUser())

        val users = userDB.userDao().getUsers()
        Log.d("SIGNUPACT",users.toString())
    }*/

    private fun signUp() {
        if (binding.signupIdEt.text.toString().isEmpty() || binding.signupEmailEt.text.toString()
                .isEmpty()
        ) {
            Toast.makeText(this, "이메일 형식이 잘못되었습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (binding.signupNameEt.text.toString().isEmpty()) {
            Toast.makeText(this, "이름 형식이 잘못되었습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (binding.signupPasswordEt.text.toString() != binding.signupPasswordCheckEt.text.toString()) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val authService = AuthService()
        authService.setSignUpView(this)

        authService.signUp(getUser())

        Log.d("SIGNUPACT/ASNTC","Hello, ")
    }

    override fun onSignUpLoading() {
        binding.signupLoadingPb.visibility = View.VISIBLE
    }

    override fun onSignUpSuccess() {
        binding.signupLoadingPb.visibility = View.GONE

        finish()
    }

    override fun onSignUpFailure(code: Int, message: String) {
        binding.signupLoadingPb.visibility = View.GONE

        when(code){
            2016, 2017 -> {
                binding.signupEmailErrorTv.visibility = View.VISIBLE
                binding.signupEmailErrorTv.text = message
            }
        }
    }

}
