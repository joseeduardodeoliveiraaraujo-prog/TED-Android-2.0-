package com.example.a3_teste_paineldevotao;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a3_teste_paineldevotao.data.FirebaseManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Tela de uso do professor para listar os votantes.
 * Em um sistema real, esta tela seria restrita a usuários com perfil de professor
 * (privacidade/administração). Aqui, o acesso é protegido por senha simples na MainActivity.
 */
public class ListaVotantesActivity extends AppCompatActivity {

    private static final String TAG = "ListaVotantes";

    private FirebaseManager firebaseManager;
    private ListView listVotantes;
    private Button btnAtualizar;
    private ArrayAdapter<String> adapter;
    private final List<String> itens = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_votantes);

        configurarToolbar();
        inicializar();
        carregarVotantes();
    }

    private void configurarToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarLista);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Lista de votantes");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void inicializar() {
        firebaseManager = FirebaseManager.getInstance(this);
        listVotantes = findViewById(R.id.listVotantes);
        btnAtualizar = findViewById(R.id.btnAtualizar);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itens);
        listVotantes.setAdapter(adapter);

        btnAtualizar.setOnClickListener(v -> carregarVotantes());
    }

    private void carregarVotantes() {
        firebaseManager
                .getEnqueteRef()
                .collection("votos")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    itens.clear();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();
                    for (int i = 0; i < docs.size(); i++) {
                        DocumentSnapshot doc = docs.get(i);
                        String nome = "Votante " + (i + 1);
                        String opcao = doc.getString("opcaoEscolhida");
                        Timestamp ts = doc.getTimestamp("timestamp");
                        String data = "—";
                        if (ts != null) {
                            Date d = ts.toDate();
                            data = sdf.format(d);
                        }
                        String linha = nome + " - Opção " + (opcao != null ? opcao : "—") + " - " + data;
                        itens.add(linha);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Erro ao carregar votantes: ", e));
    }
}

