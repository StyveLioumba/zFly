package cg.essengogroup.zfly.model;

public class Gallerie {
    private String image,racine;
    private long createAt;

    public Gallerie() {
    }

    public Gallerie(String image, String racine, long createAt) {
        this.image = image;
        this.racine = racine;
        this.createAt = createAt;
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

    public String getRacine() {
        return racine;
    }

    public void setRacine(String racine) {
        this.racine = racine;
    }
}
