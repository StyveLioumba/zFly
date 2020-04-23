package cg.essengogroup.zfly.model;

public class Place {
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
}
