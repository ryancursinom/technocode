package com.aula.tiktoktech;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.tiktoktech.model.Post;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private final List<Post> posts;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public PostAdapter(List<Post> posts) { this.posts = posts; }

    @NonNull @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(com.aula.tiktoktech.R.layout.item_post, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull PostViewHolder h, int position) {
        Post post = posts.get(position);

        h.descricao.setText(post.getDescricao());
        h.likes.setText(String.valueOf(post.getLikes()));
        h.dislikes.setText(String.valueOf(post.getDislikes()));
        h.comentarios.setText(String.valueOf(post.getComentarios()));

        Glide.with(h.itemView.getContext())
                .load(post.getUrl())
                .placeholder(R.drawable.fundo_imagem)
                .error(R.drawable.ic_imagem_vazia)
                .centerCrop()
                .into(h.foto);

        h.like.setOnClickListener(v -> atualizarContador(post.getId(), "likes"));
        h.dislike.setOnClickListener(v -> atualizarContador(post.getId(), "dislikes"));
    }

    private void atualizarContador(String id, String campo) {
        if (id == null) return;
        firestore.collection("posts").document(id)
                .update(campo, FieldValue.increment(1));
    }

    @Override public int getItemCount() { return posts.size(); }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        final android.widget.ImageView foto; final TextView descricao, likes, dislikes, comentarios;
        final ImageButton like, dislike;
        PostViewHolder(@NonNull View v) {
            super(v); foto = v.findViewById(R.id.imgFoto); descricao = v.findViewById(R.id.txtDescricao);
            likes = v.findViewById(R.id.txtLikes); dislikes = v.findViewById(R.id.txtDislikes);
            comentarios = v.findViewById(R.id.txtComentarios); like = v.findViewById(R.id.btnLike);
            dislike = v.findViewById(R.id.btnDislike);
        }
    }
}
