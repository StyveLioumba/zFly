package cg.essengogroup.zfly.controller.interfaces;

import java.util.ArrayList;

import cg.essengogroup.zfly.model.Music;

public interface MorceauInterface {

    void onScucces(ArrayList<Music>musics);
    void onError(String errorMessage);
}
