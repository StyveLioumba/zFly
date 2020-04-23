package cg.essengogroup.zfly.model;

public class Slider {
    private String slider_id,image,designation,description,lienRedirection;

    public Slider() {
    }

    public Slider(String image, String designation, String description, String lienRedirection) {
        this.image = image;
        this.designation = designation;
        this.description = description;
        this.lienRedirection = lienRedirection;
    }

    public Slider(String slider_id, String image, String designation, String description, String lienRedirection) {
        this.slider_id = slider_id;
        this.image = image;
        this.designation = designation;
        this.description = description;
        this.lienRedirection = lienRedirection;
    }

    public String getSlider_id() {
        return slider_id;
    }

    public void setSlider_id(String slider_id) {
        this.slider_id = slider_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLienRedirection() {
        return lienRedirection;
    }

    public void setLienRedirection(String lienRedirection) {
        this.lienRedirection = lienRedirection;
    }
}
