package cg.essengogroup.zfly.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Music implements Parcelable {

    private String album,artiste,chanson,cover,genre,morceau,user_id,racine;

    public Music() {
    }

    public Music(String album, String artiste, String chanson, String cover, String genre, String morceau, String user_id, String racine) {
        this.album = album;
        this.artiste = artiste;
        this.chanson = chanson;
        this.cover = cover;
        this.genre = genre;
        this.morceau = morceau;
        this.user_id = user_id;
        this.racine = racine;
    }

    protected Music(Parcel in) {
        album = in.readString();
        artiste = in.readString();
        chanson = in.readString();
        cover = in.readString();
        genre = in.readString();
        morceau = in.readString();
        user_id = in.readString();
        racine = in.readString();
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtiste() {
        return artiste;
    }

    public void setArtiste(String artiste) {
        this.artiste = artiste;
    }

    public String getChanson() {
        return chanson;
    }

    public void setChanson(String chanson) {
        this.chanson = chanson;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getMorceau() {
        return morceau;
    }

    public void setMorceau(String morceau) {
        this.morceau = morceau;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getRacine() {
        return racine;
    }

    public void setRacine(String racine) {
        this.racine = racine;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(album);
        dest.writeString(artiste);
        dest.writeString(chanson);
        dest.writeString(cover);
        dest.writeString(genre);
        dest.writeString(morceau);
        dest.writeString(user_id);
        dest.writeString(racine);
    }


}
