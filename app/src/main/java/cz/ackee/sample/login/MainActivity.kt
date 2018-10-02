package cz.ackee.sample.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import cz.ackee.sample.R
import cz.ackee.sample.detail.DetailActivity

class MainActivity : AppCompatActivity(), ILoginView {

    private var presenter: LoginPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter = LoginPresenter()

        val btn = findViewById<Button>(R.id.btn_login)
        val editName = findViewById<EditText>(R.id.edit_email)
        val editPass = findViewById<EditText>(R.id.edit_pass)
        btn.setOnClickListener { presenter!!.login(editName.text.toString(), editPass.text.toString()) }
    }

    override fun onResume() {
        super.onResume()
        presenter!!.onViewAttached(this)
    }

    override fun onPause() {
        super.onPause()
        presenter!!.onViewDetached()
    }

    override fun showError() {
    }

    override fun openDetail() {
        startActivity(Intent(this, DetailActivity::class.java))
    }
}
