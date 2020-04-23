package cg.essengogroup.zfly.model;

public class Message {

    private String envoyeur,receveur,message,heure;
    private boolean isVu,isNew=false;

    public Message() {
    }

    public Message(String envoyeur, String receveur, String message, String heure, boolean isVu, boolean isNew) {
        this.envoyeur = envoyeur;
        this.receveur = receveur;
        this.message = message;
        this.heure = heure;
        this.isVu = isVu;
        this.isNew = isNew;
    }

    public String getEnvoyeur() {
        return envoyeur;
    }

    public void setEnvoyeur(String envoyeur) {
        this.envoyeur = envoyeur;
    }

    public String getReceveur() {
        return receveur;
    }

    public void setReceveur(String receveur) {
        this.receveur = receveur;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getHeure() {
        return heure;
    }

    public void setHeure(String heure) {
        this.heure = heure;
    }

    public boolean isVu() {
        return isVu;
    }

    public void setVu(boolean vu) {
        isVu = vu;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }


}
