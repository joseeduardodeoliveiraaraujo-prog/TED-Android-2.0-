package com.example.a3_teste_paineldevotao.data;

import android.content.Context;
import android.provider.Settings;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * FirebaseManager simplificado para gravação anônima.
 *
 * - Não usa FirebaseAuth (não é necessário para votos anônimos).
 * - Sempre retorna a referência da enquete principal.
 * - Sempre cria um novo documento automático na coleção "votos".
 *
 * Estrutura no Firestore:
 *   enquetes/enquete_geral/votos/{id_gerado}
 */
public class FirebaseManager {

    private static FirebaseManager instance;

    private final FirebaseFirestore db;
    private final DocumentReference enqueteRef;
    private final String voterId;
    private final FirebaseAuth auth;

    private FirebaseManager(Context context) {
        FirebaseApp.initializeApp(context.getApplicationContext());
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Documento fixo da enquete
        enqueteRef = db.collection("enquetes").document("enquete_geral");

        // Identificador estável do dispositivo para representar "um usuário"
        voterId = Settings.Secure.getString(
                context.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }

    public static FirebaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseManager(context);
        }
        return instance;
    }

    /**
     * Retorna referência do documento principal da enquete.
     */
    public DocumentReference getEnqueteRef() {
        return enqueteRef;
    }

    /**
     * Exponibiliza o FirebaseAuth para chamadas que ainda usam autenticação anônima.
     */
    public FirebaseAuth getAuth() {
        return auth;
    }

    /**
     * Retorna a referência do voto para o "usuário" atual (identificado por dispositivo).
     * Estrutura:
     *   enquetes/enquete_geral/votos/{ANDROID_ID}
     *
     * Não depende de FirebaseAuth. Retorna sempre uma referência válida.
     */
    public DocumentReference getUserVoteRef() {
        String docId;
        if (auth.getCurrentUser() != null) {
            docId = auth.getCurrentUser().getUid();
        } else {
            docId = voterId;
        }
        return enqueteRef.collection("votos").document(docId);
    }

    /**
     * Gera um novo documento anônimo para salvar o voto.
     *
     * Exemplo de caminho gerado:
     *   enquetes/enquete_geral/votos/abc123XYZ...
     */
    public DocumentReference createAnonymousVoteRef() {
        return enqueteRef.collection("votos").document(); // Cria ID aleatório
    }

    /**
     * Retorna a instância do Firestore caso você precise acessar manualmente.
     */
    public FirebaseFirestore getDb() {
        return db;
    }
}
