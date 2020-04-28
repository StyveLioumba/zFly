package cg.essengogroup.zfly.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Place implements Parcelable {
    private String user_id,image_couverture,nom,description,type,adresse,numero,createAt,place_id;

    public Place() {
    }

    public Place(String user_id, String image_couverture, String nom, String description, String type, String adresse, String numero, String createAt, String place_id) {
        this.user_id = user_id;
        this.image_couverture = image_couverture;
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.adresse = adresse;
        this.numero = numero;
        this.createAt = createAt;
        this.place_id = place_id;
    }

    protected Place(Parcel in) {
        user_id = in.readString();
        image_couverture = in.readString();
        nom = in.readString();
        description = in.readString();
        type = in.readString();
        adresse = in.readString();
        numero = in.readString();
        createAt = in.readString();
        place_id = in.readString();
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_couverture() {
        return image_couverture;
    }

    public void setImage_couverture(String image_couverture) {
        this.image_couverture = image_couverture;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(image_couverture);
        dest.writeString(nom);
        dest.writeString(description);
        dest.writeString(type);
        dest.writeString(adresse);
        dest.writeString(numero);
        dest.writeString(createAt);
        dest.writeString(place_id);
    }
}
