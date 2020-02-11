package za.co.entelect.challenge;

import za.co.entelect.challenge.entities.Building;
import za.co.entelect.challenge.entities.CellStateContainer;
import za.co.entelect.challenge.entities.GameState;
import za.co.entelect.challenge.entities.BuildingsOnMap;
import za.co.entelect.challenge.enums.BuildingType;
import za.co.entelect.challenge.enums.PlayerType;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Bot {

    private GameState gameState;
    private BuildingsOnMap counterBuilding;

    
    public Bot(GameState gameState) {
        this.gameState = gameState;
        gameState.getGameMap();
        this.counterBuilding = new BuildingsOnMap(gameState, PlayerType.A);
    }

    public String run() {
        String command = "";

        if(canAffordBuilding(BuildingType.DEFENSE)) {
            for (int i = 0; i < gameState.gameDetails.mapHeight; i++) {
                if (counterBuilding.building[1][i].a > 3 && counterBuilding.building[0][i].d < 1) {
                    command = placeBuildingInRowFromFront(BuildingType.DEFENSE, i);
                    break;
                }
            }
        }

        if(canAffordBuilding(BuildingType.ATTACK) && command == ""){
            for (int i=0;i<gameState.gameDetails.mapHeight;i++){
                if(counterBuilding.building[1][i].d == 0){
                    if(counterBuilding.building[0][i].a + counterBuilding.building[0][i].d + counterBuilding.building[0][i].e < 8){
                        command = placeBuildingInRowFromFront(BuildingType.ATTACK, i);
                    }
                    break;
                }
            }
        }

        if(canAffordBuilding(BuildingType.ATTACK) && command == ""){
            for(int i = 0; i < gameState.gameDetails.mapHeight;i++){
                if(counterBuilding.building[0][i].a + counterBuilding.building[0][i].d + counterBuilding.building[0][i].e < 8){
                    command = placeBuildingInRowFromFront(BuildingType.ATTACK, i);
                }
                break;
            }
        }

        if(canAffordBuilding(BuildingType.ENERGY) && command == ""){
            for(int i = 0; i < gameState.gameDetails.mapHeight; i++){
                if(counterBuilding.building[0][i].e == 0 && counterBuilding.building[1][i].a == 0){
                    command = placeBuildingInRowFromBack(BuildingType.ENERGY, i);
                    break;
                }
            }
        }

        return command;
    }

    private String placeBuildingInRowFromFront(BuildingType buildingType, int y) {
        for (int i = (gameState.gameDetails.mapWidth / 2) - 1; i >= 0; i--) {
            if (isCellEmpty(i, y)) {
                return buildCommand(i, y, buildingType);
            }
        }
        return "";
    }

    private String placeBuildingInRowFromBack(BuildingType buildingType, int y) {
        for (int i = 0; i < gameState.gameDetails.mapWidth / 2; i++) {
            if (isCellEmpty(i, y)) {
                return buildCommand(i, y, buildingType);
            }
        }
        return "";
    }

    private String buildCommand(int x, int y, BuildingType buildingType) {
        return String.format("%s,%d,%s", String.valueOf(x), y, buildingType.getCommandCode());
    }

    private boolean isCellEmpty(int x, int y) {
        Optional<CellStateContainer> cellOptional = gameState.getGameMap().stream()
                .filter(c -> c.x == x && c.y == y)
                .findFirst();

        if (cellOptional.isPresent()) {
            CellStateContainer cell = cellOptional.get();
            return cell.getBuildings().size() <= 0;
        } else {
            System.out.println("Invalid cell selected");
        }
        return true;
    }

    private boolean canAffordBuilding(BuildingType buildingType) {
        return getEnergy(PlayerType.A) >= getPriceForBuilding(buildingType);
    }

    private int getEnergy(PlayerType playerType) {
        return gameState.getPlayers().stream()
                .filter(p -> p.playerType == playerType)
                .mapToInt(p -> p.energy)
                .sum();
    }

    private int getPriceForBuilding(BuildingType buildingType) {
        return gameState.gameDetails.buildingsStats.get(buildingType).price;
    }
}
