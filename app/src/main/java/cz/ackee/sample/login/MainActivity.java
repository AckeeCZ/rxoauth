package cz.ackee.sample.login;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import cz.ackee.sample.detail.DetailActivity;
import cz.ackee.sample.R;


public class MainActivity extends AppCompatActivity implements ILoginView {


    private LoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        presenter = new LoginPresenter();

        Button btn = (Button) findViewById(R.id.btn_login);
        EditText editName = (EditText) findViewById(R.id.edit_email);
        EditText editPass = (EditText) findViewById(R.id.edit_pass);
        btn.setOnClickListener(v -> presenter.login(editName.getText().toString(), editPass.getText().toString()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onViewAttached(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onViewDetached();
    }

    @Override
    public void showError() {

    }

    @Override
    public void openDetail() {
        startActivity(new Intent(this, DetailActivity.class));
    }
}
