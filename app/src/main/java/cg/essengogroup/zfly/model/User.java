package cg.essengogroup.zfly.model;


import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String Apseudo,nom,prenom,pseudo,tel,user_id,image,imageCouverture=null,status;
    private long  createAt;
    private boolean isArtiste,hasNewSMS;

    public User() {
    }

    public User(String apseudo, String nom, String prenom, String pseudo, String tel, String user_id, String image, String imageCouverture, String status, long createAt, boolean isArtiste) {
        Apseudo = apseudo;
        this.nom = nom;
        this.prenom = prenom;
        this.pseudo = pseudo;
        this.tel = tel;
        this.user_id = user_id;
        this.image = image;
        this.imageCouverture = imageCouverture;
        this.status = status;
        this.createAt = createAt;
        this.isArtiste = isArtiste;
    }

    protected User(Parcel in) {
        Apseudo = in.readString();
        nom = in.readString();
        prenom = in.readString();
        pseudo = in.readString();
        tel = in.readString();
        user_id = in.readString();
        image = in.readString();
        status = in.readString();
        imageCouverture = in.readString();
        createAt = in.readLong();
        isArtiste = in.readByte() != 0;
        hasNewSMS = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getApseudo() {
        return Apseudo;
    }

    public void setApseudo(String apseudo) {
        Apseudo = apseudo;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }

    public boolean isArtiste() {
        return isArtiste;
    }

    public void setArtiste(boolean artiste) {
        isArtiste = artiste;
    }

    public String getImageCouverture() {
        return imageCouverture;
    }

    public void setImageCouverture(String imageCouverture) {
        this.imageCouverture = imageCouverture;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isHasNewSMS() {
        return hasNewSMS;
    }

    public void setHasNewSMS(boolean hasNewSMS) {
        this.hasNewSMS = hasNewSMS;
    }

    @Override
    public String toString() {
        return "User{" +
                "Apseudo='" + Apseudo + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", pseudo='" + pseudo + '\'' +
                ", tel='" + tel + '\'' +
                ", user_id='" + user_id + '\'' +
                ", image='" + image + '\'' +
                ", imageCouverture='" + imageCouverture + '\'' +
                ", createAt=" + createAt +
                ", isArtiste=" + isArtiste +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Apseudo);
        dest.writeString(nom);
        dest.writeString(prenom);
        dest.writeString(pseudo);
        dest.writeString(tel);
        dest.writeString(user_id);
        dest.writeString(image);
        dest.writeString(status);
        dest.writeString(imageCouverture);
        dest.writeLong(createAt);
        dest.writeByte((byte) (isArtiste ? 1 : 0));
        dest.writeByte((byte) (hasNewSMS ? 1 : 0));
    }
}
