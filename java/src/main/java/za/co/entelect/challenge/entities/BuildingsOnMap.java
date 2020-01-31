package za.co.entelect.challenge.entities;

import za.co.entelect.challenge.enums.BuildingType;
import za.co.entelect.challenge.enums.PlayerType;

import java.util.List;

public class BuildingsOnMap {
    public Type building[][];
    public Type total[];
    private GameState gameState;

    public BuildingsOnMap(GameState gameState,PlayerType P){
        building = new Type[2][8];
        total = new Type[2];

        for(int i = 0; i < 2; i++){
            for(int j = 0; j < 8; j++){
                building[i][j] = new Type();
            }
        }

        for(int i = 0; i < 2; i++){
            total[i] = new Type();
        }

        this.gameState = gameState;
        gameState.getGameMap();

        for(int i = 0; i < gameState.gameDetails.mapHeight; i++){
            for(int j = 0; j < gameState.gameDetails.mapWidth; j++){
                List<Building> a = gameState.gameMap[i][j].getBuildings();
                if(!a.isEmpty()){
                    if(gameState.gameMap[i][j].cellOwner==P){
                        if(a.contains(BuildingType.ATTACK)){
                            building[0][i].a++;
                        }else if(a.contains(BuildingType.DEFENSE)){
                            building[0][i].d++;
                        }else{
                            building[0][i].e++;
                        }
                    }else{
                        if(a.contains(BuildingType.ATTACK)){
                            building[1][i].a++;
                        }else if(a.contains(BuildingType.DEFENSE)){
                            building[1][i].d++;
                        }else{
                            building[1][i].e++;
                        }
                    }
                }
                total[0].a += building[0][i].a;
                total[0].d += building[0][i].d;
                total[0].e += building[0][i].e;
                total[1].a += building[1][i].a;
                total[1].d += building[1][i].d;
                total[1].e += building[1][i].e;
            }
        }
    }
}
