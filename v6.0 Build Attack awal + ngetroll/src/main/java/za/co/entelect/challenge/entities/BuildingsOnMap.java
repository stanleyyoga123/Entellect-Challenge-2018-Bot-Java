package za.co.entelect.challenge.entities;

import za.co.entelect.challenge.enums.BuildingType;
import za.co.entelect.challenge.enums.PlayerType;

import java.util.List;

public class BuildingsOnMap {
    public Type[][] building;
    public Type[] total;
    private GameState gameState;

    public BuildingsOnMap(GameState gameState,PlayerType P){
        this.building = new Type[2][8];
        this.total = new Type[2];

        for(int i = 0; i < 2; i++){
            for(int j = 0; j < 8; j++){
                this.building[i][j] = new Type();
            }
        }

        for(int i = 0; i < 2; i++){
            this.total[i] = new Type();
        }

        this.gameState = gameState;
        gameState.getGameMap();

        for(int i = 0; i < gameState.gameDetails.mapHeight; i++){
            for(int j = 0; j < gameState.gameDetails.mapWidth; j++){
                List<Building> a = gameState.gameMap[i][j].getBuildings();
                if(!a.isEmpty()){
                    if(gameState.gameMap[i][j].cellOwner==P){
                        for(int k = 0; k < a.size(); k++){
                            if(a.get(k).buildingType == (BuildingType.ATTACK)){
                                this.building[0][i].a+=1;
                            }else if(a.get(k).buildingType == (BuildingType.DEFENSE)){
                                this.building[0][i].d+=1;
                            }else if(a.get(k).buildingType == (BuildingType.ENERGY)){
                                this.building[0][i].e+=1;
                            }
                        }
                    }else{
                        for(int k = 0; k < a.size(); k++){
                            if(a.get(k).buildingType == (BuildingType.ATTACK)){
                                this.building[1][i].a+=1;
                            }else if(a.get(k).buildingType == (BuildingType.DEFENSE)){
                                this.building[1][i].d+=1;
                            }else if(a.get(k).buildingType == (BuildingType.ENERGY)){
                                this.building[1][i].e+=1;
                            }
                        }
                    }
                }
                this.total[0].a += building[0][i].a;
                this.total[0].d += building[0][i].d;
                this.total[0].e += building[0][i].e;
                this.total[1].a += building[1][i].a;
                this.total[1].d += building[1][i].d;
                this.total[1].e += building[1][i].e;
            }
        }
    }
}
