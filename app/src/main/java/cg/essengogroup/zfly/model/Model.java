package cg.essengogroup.zfly.model;

public class Model {

    private int type=1;
    private String image,audio,description,commentaire,likes,user_id,createAt,post_id;

    public Model() {
    }

    public Model(int type, String image, String audio, String description, String commentaire, String likes, String user_id, String createAt, String post_id) {
        this.type = type;
        this.image = image;
        this.audio = audio;
        this.description = description;
        this.commentaire = commentaire;
        this.likes = likes;
        this.user_id = user_id;
        this.createAt = createAt;
        this.post_id = post_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }
}
